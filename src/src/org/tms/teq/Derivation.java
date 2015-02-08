package org.tms.teq;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.tms.api.BaseElement;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.DerivableThreadPool;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.InvalidExpressionException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.api.factories.TableContextFactory;
import org.tms.util.Tuple;

public class Derivation
{
    public static final int sf_DEFAULT_PRECISION = 15;
    
    private static final ThreadLocal<UUID> sf_GUID_CACHE = new ThreadLocal<UUID>();
    private static final Map<UUID, PendingState> sf_UUID_PENDING_STATE_MAP = new ConcurrentHashMap<UUID, PendingState>();
    private static final Map<Cell, PendingState> sf_CELL_PENDING_STATE_MAP = new ConcurrentHashMap<Cell, PendingState>();
    private static final Map<Long, UUID> sf_PROCESS_ID_UUID_MAP = new ConcurrentHashMap<Long, UUID>();
    private static PendingDerivationExecutor sf_PENDING_EXECUTOR = null;
    
    public static Derivation create(String expr, Derivable elem)
    {
        // create the derivation structure and save the as-entered expression
        Derivation deriv = new Derivation();
        deriv.m_asEntered = new String(expr);
        deriv.m_target = elem;
        
        // parse the expression
        Table t = elem.getTable();
        if (t != null) {
            t.pushCurrent();
            
            if (t.hasProperty(TableProperty.Precision)) {
                int precision = t.getPropertyInt(TableProperty.Precision);
                if (precision < 0)
                    deriv.m_precision = null;
                else 
                    deriv.m_precision = new MathContext(precision);
            }
        }            
        
        try {
            InfixExpressionParser ifParser = new InfixExpressionParser(expr, t);            
            ParseResult pr = ifParser.parseInfixExpression(elem);
            if (pr != null && pr.isFailure())
                throw new InvalidExpressionException(pr);
            
            // otherwise, harvest the infix stack
            deriv.m_ifs = ifParser.getInfixStack();
            deriv.m_parsed = true;
            
            // convert infix to postfix
            PostfixStackGenerator pfg = new PostfixStackGenerator(deriv.m_ifs, t);
            pr =  pfg.convertInfixToPostfix();
            if (pr != null && pr.isFailure())
                throw new InvalidExpressionException(pr);
            
            // harvest the postfix stack
            deriv.m_pfs = pfg.getPostfixStack();
            deriv.m_converted = true;
            
            // note cols/rows that affect this derivation
            Iterator<Token> iter = deriv.m_pfs.iterator();
            while(iter != null && iter.hasNext()) {
                Token tk = iter.next();
                TokenType tt = tk.getTokenType();
                
                switch (tt) {
                    case RowRef:
                    case ColumnRef:
                    case CellRef:
                    case SubsetRef:
                    case TableRef:
                        if (tk.getTableElementValue() != null) 
                            deriv.m_affectedBy.add(tk.getTableElementValue());
                        break;
                        
                    default:
                        break;
                }            
            }
            
            // check for circular reference (e.g., c1<==c2<==c3<==c1)
            if (deriv.isCircularReference()) {
                pr = new ParseResult(ParserStatusCode.CircularReference, String.format("Expression contains circular reference to %s", elem));
                throw new InvalidExpressionException(pr);
            }
    
            // finally, return the derivation
            return deriv;
        }
        finally {
            if (t != null)
                t.popCurrent();
        }
    }
    
    private static boolean checkCircularReference(Derivable target, List<TableElement> affectedBy)
    {
        if (affectedBy == null)
            return false;
        
        for (TableElement d : affectedBy) {
            if (target == d || (d instanceof Derivable && checkCircularReference(target, ((Derivable)d).getAffectedBy())))
                return true;
        }
        
        return false;
    }

    public static void recalculateAffected(TableElement element)
    {
        if (element == null || element.isInvalid())
            return;
        
        Table parent = element.getTable();
        List<Derivable> orderedDerivables = null;        
        switch (element.getElementType()) {
            case Row:
            case Column:
            case Cell:
                orderedDerivables = calculateDependencies(element);
                break;
                
            case Table:
            case Subset:
                orderedDerivables = calculateDependencies(element.getDerivedElements());
                break;
                
            default:
                throw new UnsupportedImplementationException(element, "recalculateAffected");
        }
        
        if (orderedDerivables == null || orderedDerivables.isEmpty())
            return;
        
        // iterate over ordered list and recalculate 
        DerivationContext dc = new DerivationContext();
        dc.setRecalculateAffected(false);
        
        if (parent != null) parent.pushCurrent();
        try {
            for (Derivable derivable : orderedDerivables) {
                if (derivable == null) continue;
                Derivation d = derivable.getDerivation();
                d.recalculateTarget(element, dc);
            }
            
            // start background threads
            dc.processPendings();
        }
        finally {
            if (parent != null) parent.pushCurrent();
        }
    }

   static List<Derivable> calculateDependencies(TableElement modifiedElement)
    {
        assert modifiedElement != null : "TableElement required";
        
        List<Derivable> affected = modifiedElement.getAffects();
        if (affected == null || affected.isEmpty())
            return null;
        
        // harvest all of the elements affected 
        Set<Derivable> visited = new HashSet<Derivable>();
        Set<Derivable> globalAffected = new HashSet<Derivable>(affected.size());
        
        calculateGlobalAffected(globalAffected, visited, affected);        
        int numAffected = globalAffected.size();
        
        Set<Derivable> resolved = new LinkedHashSet<Derivable>(numAffected);
        Set<Derivable> unresolved = new HashSet<Derivable>(numAffected);
        
        Derivable med = modifiedElement instanceof Derivable ? (Derivable)modifiedElement : null;
        for (Derivable d : globalAffected) {
            if (!resolved.contains(d))
                resolveDependencies(d, resolved, unresolved, med);
        }
        
        // remove specified element from set, as it has already been changed
        resolved.remove(modifiedElement);
        
        List<Derivable> orderedDerivables = new ArrayList<Derivable>(resolved);        
        return orderedDerivables;
    }

    private static List<Derivable> calculateDependencies(Collection<Derivable> derived)
    {
        assert derived != null : "Set<Derived> required";

        int numAffected = derived.size();

        Set<Derivable> resolved = new LinkedHashSet<Derivable>(numAffected);
        Set<Derivable> unresolved = new HashSet<Derivable>(numAffected);

        for (Derivable d : derived) {
           if (!resolved.contains(d))
               resolveDependencies(d, resolved, unresolved, null);
        }

        List<Derivable> orderedDerivables = new ArrayList<Derivable>(resolved);        
        return orderedDerivables;
    }
   
    private static void resolveDependencies(Derivable d, Set<Derivable> resolved, Set<Derivable> unresolved, Derivable omit)
    {
        List<TableElement> affectedBy = d.getAffectedBy();
        if (affectedBy != null) {
            unresolved.add(d);
            for (TableElement te : affectedBy) {
                if (!(te instanceof Derivable)) continue;
                
                Derivable ted = (Derivable)te;
                if (!ted.isDerived()) continue;
                if (ted == omit) continue;
                
                if (!resolved.contains(ted))
                    resolveDependencies(ted, resolved, unresolved, omit);
            }
            
            unresolved.remove(d); 
        }
        
        resolved.add(d);
    }

    /**
     * Recursive method to determine complete set of Derivables to recalculate
     * @param globalAffected
     * @param visited
     * @param affected
     */
    private static void calculateGlobalAffected(Set<Derivable> globalAffected, Set<Derivable> visited, List<Derivable> affected)
    {
        if (affected == null || affected.isEmpty())
            return;
        
        for (Derivable d : affected) {
            if (visited.contains(d)) continue;
            visited.add(d);
            globalAffected.add(d);
            calculateGlobalAffected(globalAffected, visited, d.getAffects());
        }        
    }

    public static final UUID getTransactionID()
    {
        return sf_GUID_CACHE.get();
    }
    
    public static final UUID assignTransactionID()
    {
        // assign a unique GUID to the recalculation session
        // pending calculations will use it to connect back to
        // the correct derivation state
        UUID transId = UUID.randomUUID();
        sf_GUID_CACHE.set(transId);
        return transId;
    }
    
    public static void postResult(Object value)
    {
        UUID transactId = sf_PROCESS_ID_UUID_MAP.remove(Thread.currentThread().getId());
        if (transactId != null)
            postResult(transactId, value);
        else
            System.out.println("Orphan pending thread: " + Thread.currentThread().getId());
    }

    public static void postResult(UUID transactId, Object value)
    {
        // could be null if derivation is being cleared while pending 
        // calculations are being processed
        if (transactId == null) 
            return;
        
        PendingState ps = sf_UUID_PENDING_STATE_MAP.remove(transactId);
        if (ps != null) { 
            Derivation psDeriv = null;
            
            // need exclusive access to this process state, otherwise,
            // token could become null
            ps.lock();
            try {
                // could be null if derivation is being cleared while pending 
                // calculations are being processed
                psDeriv = ps.getDerivation();
                Token t = ps.getPendingToken();
                if (!ps.isValid() || t == null || psDeriv == null) {
                    ps.resetPendingState();
                    return;
                }
                
                t.setValue(value);
                t.setTokenType(TokenType.Operand);
                t.setOperator(BuiltinOperator.NOP);
            }
            finally {
                ps.unlock();
            }
            
            DerivationContext dc = new DerivationContext();
            try
            {
                // blocks on access to ps
                ps.reevaluate(dc);
            }
            catch (PendingDerivationException pc)
            {
                // if derivation is being cleared, it could go away,
                // get exclusive access while we perform cache
                ps.lock();
                try {
                    if (ps.isValid()) {
                        PendingState newPs = pc.getPendingState();
                        newPs.registerBlockedDerivations(ps);
                        psDeriv.cacheDeferredCalculation(newPs, dc);
                    }
                    else {
                        ps.resetPendingState();
                        dc.clearPendings();
                    }
                }
                finally {
                    ps.unlock();
                }
                
                dc.processPendings();
            }
            catch (BlockedDerivationException e)
            {
                // noop;
            }
            finally {
                psDeriv.pendingStateProcessed(ps);
            }
        }
    }

    protected static void removePendingCellFromCache(Cell cell)
    {
        if (cell != null)
            sf_CELL_PENDING_STATE_MAP.remove(cell);
    }
    
    public static void associateTransactionID(long id, UUID transactId)
    {
        sf_PROCESS_ID_UUID_MAP.put(id, transactId);        
    }  
    
    /*
     * Instance fields
     */
    private String m_asEntered;
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private Set<TableElement> m_affectedBy;
    private boolean m_parsed;
    private boolean m_converted;
    private Derivable m_target;
    private MathContext m_precision;
    private DerivableThreadPool m_threadPool;
    private Set<PendingState> m_cachedPendingStates;
    private Set<PendingState> m_pendingStatesProcessed;
    
    public boolean m_beingDestroyed;
    
    protected Derivation()
    {
        m_beingDestroyed = false;
        
        m_affectedBy = new LinkedHashSet<TableElement>();
        m_precision = new MathContext(sf_DEFAULT_PRECISION);
        m_cachedPendingStates = Collections.synchronizedSet(new HashSet<PendingState>());
        m_pendingStatesProcessed = Collections.synchronizedSet(new HashSet<PendingState>());
    }
    
    synchronized public void destroy()
    {
        m_beingDestroyed = true;        
        m_cachedPendingStates.removeAll(m_pendingStatesProcessed);
        for (PendingState ps : m_cachedPendingStates) {
            // don't reprocess invalidated pending states
            if (!ps.isValid())
                continue;
            
            // we need exclusive access
            ps.lock();
            try {
                if (ps.getTransactionID() != null)
                    sf_UUID_PENDING_STATE_MAP.remove(ps.getTransactionID());
                
                if (m_threadPool != null)
                    m_threadPool.remove(ps.getPendingRunnable());
                
                Cell cell = ps.getPendingCell();
                if (cell != null) {
                    PendingState curPs = sf_CELL_PENDING_STATE_MAP.remove(cell);
                    if (curPs != null && curPs != ps) {
                        System.out.println(ps);
                        // TODO
                    }
                }
                
                ps.resetPendingState();
            }
            finally {
                ps.unlock();
            }
        }
        
        // release all pending states
        m_cachedPendingStates.clear();
        m_pendingStatesProcessed.clear();
    }
    
    boolean isBeingDestroyed()
    {
        return m_beingDestroyed;
    }
    
    public void delete()
    {
        if (this.getTarget() != null)
            getTarget().clearDerivation();
        else {
            destroy();
        }
    }
    
    synchronized protected void registerPendingState(PendingState ps)
    {
        if (ps != null) { 
            if (ps.getDerivation() != this)
                throw new IllegalTableStateException("PendingState must be associated with this Derivation");
            
            // there is a chance that a different thread may be clearing the derivation
            // while another is still caching pending states; the check below throws away
            // such caches if the derivation is being destroyed
            if (m_beingDestroyed) 
                return;
            
            m_cachedPendingStates.add(ps);
            
            Cell cell = ps.getPendingCell();
            if (cell != null)
                sf_CELL_PENDING_STATE_MAP.put(cell, ps);
        }
    }
    
    public Table getTable()
    {
        if (m_target != null)
            return m_target.getTable();
        else
            return null;
    }
    
    public TableContext getTableContext()
    {
        Table parentTable = getTable();
        TableContext tc = null;
        if (parentTable != null)
            tc = parentTable.getTableContext();
        
        return tc != null ? tc : TableContextFactory.fetchDefaultTableContext();
    }
    
    public boolean isParsed()
    {
        return m_parsed;
    }

    public boolean isConverted()
    {
        return m_converted;
    }
    
    public String getInfixExpression()
    {
        if (m_ifs != null)
            return m_ifs.toExpression();
        else
            return null;
    }
    
    public String getPostfixExpression()
    {
        if (m_pfs != null)
            return m_pfs.toExpression();
        else
            return null;
    }
    
    public String getAsEnteredExpression()
    {
        if (m_asEntered != null)
            return m_asEntered;
        else if (m_ifs != null)
            return m_ifs.toExpression();
        else
            return null;
    }

    protected EquationStack getInfixStackInternal()
    {
        return m_ifs;
    }
    
    protected Collection<Token> getInfixStack()
    {
        return Collections.unmodifiableCollection(m_ifs);
    }
    
    protected EquationStack getPostfixStackInternal()
    {
        return m_pfs;
    }
    
    protected Collection<Token> getPostfixStack()
    {
        return Collections.unmodifiableCollection(m_pfs);
    }
    
    protected MathContext getPrecision()
    {
        return m_precision;
    }
    
    public void recalculateTarget()
    {
    	DerivationContext dc = new DerivationContext();
        recalculateTarget(null, dc);
        dc.processPendings();
    }
    
    private void recalculateTarget(TableElement element, DerivationContext dc)
    {
        if (m_target.isReadOnly())
            throw new ReadOnlyException((BaseElement)m_target, TableProperty.Derivation);
        
        Table t = m_target.getTable();
        t.pushCurrent();
        
        try {
            // currently support derived rows, columns, and cells, 
            // dispatch to correct handler
            if (m_target instanceof Column)
                recalculateTargetColumn(element, dc);
            else if (m_target instanceof Row)
                recalculateTargetRow(element, dc);
            else if (m_target instanceof Cell)
                recalculateTargetCell(element, dc);
            
            // we only want to process affected cells up to 1 time,
            // as in so doing, we recalculate all children
            if (dc.isRecalculateAffected()) {
                dc.setRecalculateAffected(false);
                recalculateAffectedElements(dc);
            }
        }
        finally {      
            t.popCurrent();  
        }
    }
    
    private void recalculateAffectedElements(DerivationContext dc)
    {
        List<Derivable> affected = calculateDependencies(m_target);
        if (affected == null ) return;
        
        // remove current element from recalc plan
        affected.remove(m_target);
        if (affected.isEmpty()) return;
        
        // recalculate impacted elements
        Table parentTable = m_target.getTable();
        if (parentTable != null)
            parentTable.pushCurrent();
        try {
            for (Derivable d : affected) {
                Derivation deriv = d.getDerivation();
                deriv.recalculateTarget(m_target, dc);
            }
        }
        finally {
            if (parentTable != null)
                parentTable.popCurrent();
        }       
    }
    
    private void recalculateTargetCell(TableElement modifiedElement, DerivationContext dc) 
    {
        Cell cell = (Cell)m_target;
        Row row = cell.getRow();
        Column col = cell.getColumn();
        
        if (row == null || col == null)
            throw new IllegalTableStateException("Row/Column required");
        
        Table tbl = row.getTable();
        if (tbl == null)
            throw new IllegalTableStateException("Table required");
        
        try {
            PostfixStackEvaluator pfe = new PostfixStackEvaluator(this);        
            Token t = pfe.evaluate(row, col, dc);
            if (t.isNumeric()) 
                t.setValue(applyPrecision(t.getNumericValue()));
            boolean modified = tbl.setCellValue(row, col, t);
            if (modified && dc != null) {
            	dc.remove(row);
            	dc.remove(col);
            }
        }
        catch (PendingDerivationException pc) {
            cacheDeferredCalculation(pc.getPendingState(), dc);
        }
        catch (BlockedDerivationException e)
        {
            // noop;
        }
    }

    private void recalculateTargetRow(TableElement modifiedElement, DerivationContext dc) 
    {
        Row row = (Row)m_target;
        Table tbl = row.getTable();
        
        Iterable<Column> cols = null;
        if (modifiedElement != null && modifiedElement instanceof Cell) {
            Column col = ((Cell)modifiedElement).getColumn();
            assert col != null : "Column required";
            cols = Collections.singletonList(col);
        }
        else
            cols = tbl.columns();
        
        boolean anyModified = false;
        for (Column col : cols) {
            if (col == null)
                continue;
            
        	// derived cells have president over derived rows
        	Cell cell = tbl.getCell(row,  col);
        	if (cell != null && cell.isDerived()) continue;
        	
        	try {
                PostfixStackEvaluator pfe = new PostfixStackEvaluator(this);        
                Token t = pfe.evaluate(row, col, dc);
                if (t.isNumeric() )
                    t.setValue(applyPrecision(t.getNumericValue()));
                
                boolean modified = tbl.setCellValue(row, col, t);
                if (modified && dc != null) {
                	dc.remove(col);
                	anyModified = true;
                }
            }
            catch (PendingDerivationException pc) {
                cacheDeferredCalculation(pc.getPendingState(), dc);
            }
            catch (BlockedDerivationException e)
            {
                // noop;
            }
        }        
        
        if (anyModified && dc != null)
        	dc.remove(row);
    }

    private void recalculateTargetColumn(TableElement modifiedElement, DerivationContext dc) 
    {
        Column col = (Column)m_target;        
        Table tbl = col.getTable();
        
        Iterable<Row> rows = null;
        if (modifiedElement != null && modifiedElement instanceof Cell) {
            Row row = ((Cell)modifiedElement).getRow();
            assert row != null : "Row required";
            rows = Collections.singletonList(row);
        }
        else
            rows = tbl.rows();
        
        boolean anyModified = false;
        for (Row row : rows) {
            if (row == null)
                continue;
            
        	// derived rows have precedent
        	if (row.isDerived()) continue;
        	
        	Cell cell = tbl.getCell(row,  col);
        	if (cell != null && cell.isDerived()) continue;
        	
        	try {
                PostfixStackEvaluator pfe = new PostfixStackEvaluator(this);        
                Token t = pfe.evaluate(row, col, dc);
                if (t.isNumeric())
                    t.setValue(applyPrecision(t.getNumericValue()));
                boolean modified = tbl.setCellValue(row, col, t);
                if (modified && dc != null) {
                	dc.remove(row);
                	anyModified = true;
                }
            }
            catch (PendingDerivationException pc) {
                cacheDeferredCalculation(pc.getPendingState(), dc);
            }
            catch (BlockedDerivationException e)
            {
                // noop;
            }
        }  
        
        if (anyModified && dc != null)
        	dc.remove(col);
    }

    void cacheDeferredCalculation(PendingState pendingState, DerivationContext dc)
    {
        sf_UUID_PENDING_STATE_MAP.put(pendingState.getTransactionID(), pendingState);
        if (pendingState.isRunnable()) {
            if (dc != null)
                dc.cachePending(pendingState);
            else 
                submitCalculation(pendingState);
        }        
    }

    void submitCalculation(PendingState pendingState)
    {
        TableContext tc = getTableContext();
        if (tc instanceof DerivableThreadPool) {
            ((DerivableThreadPool)tc).submitCalculation(pendingState.getTransactionID(), pendingState.getPendingRunnable());
            if (m_threadPool != null)
                m_threadPool = (DerivableThreadPool)tc;
        }
        else {
            submitCalculation(pendingState.getTransactionID(), pendingState.getPendingRunnable());
            if (m_threadPool != null)
                m_threadPool = sf_PENDING_EXECUTOR;
        }
    }

    public List<TableElement> getAffectedBy()
    {
        List<TableElement> affectedBy = new ArrayList<TableElement>();       
        if (m_affectedBy != null) 
            affectedBy.addAll(m_affectedBy);
        
        return affectedBy;       
    }
    
    public Derivable getTarget()
    {
        return m_target;
    }
    
    private boolean isCircularReference()
    {
        if (m_ifs == null || m_ifs.isEmpty() || !isParsed())
            return false;
        
        return checkCircularReference(getTarget(), getAffectedBy());
    }

    synchronized void pendingStateProcessed(PendingState ps)
    {
        try {
            if (!m_beingDestroyed && ps != null && ps.isValid() && m_pendingStatesProcessed != null)
                m_pendingStatesProcessed.add(ps);     
        }
        catch (NullPointerException npe) {
            // NPE could happen in race condition if 
            System.out.println(npe);
        }
    }

    protected double applyPrecision(double value)
    {
        if (m_precision != null) {
            try {
                BigDecimal bd = BigDecimal.valueOf(value);
                value = bd.round(m_precision).doubleValue();
            }
            catch (NumberFormatException e) {
                // nop, return unprocessed value
            }           
        }
        
        return value;
    }
      
    public void submitCalculation(UUID transactionID, Runnable pendingRunnable)
    {
        synchronized(Derivation.class) {
            if (sf_PENDING_EXECUTOR == null)
                sf_PENDING_EXECUTOR = new PendingDerivationExecutor();
        }
        
        sf_PENDING_EXECUTOR.submitCalculation(transactionID, pendingRunnable);       
    }
    
    public void shutdown()
    {
        if (sf_PENDING_EXECUTOR != null)
            sf_PENDING_EXECUTOR.shutdown();
    }

    public String toString()
    {
        return String.format("%s [%s %s]", 
                this.getAsEnteredExpression(),
                isParsed() ? "parsed" : "", isConverted() ? "converted" : "");
    }
    
    protected static class DerivationContext 
    {
        private Map<TableElement, SingleVariableStatEngine> m_cachedSVSEs;
        private Map<Tuple<TableElement>, TwoVariableStatEngine> m_cachedTVSEs;
    	private boolean m_cachedAny = false;
    	private List<PendingState> m_pendings;
    	private boolean m_isRecalculateAffected;
    	
    	DerivationContext()
    	{
            m_cachedSVSEs = new HashMap<TableElement, SingleVariableStatEngine>();
            m_cachedTVSEs = new HashMap<Tuple<TableElement>, TwoVariableStatEngine>();
            m_pendings = new ArrayList<PendingState>();
            m_isRecalculateAffected = true;
    	}
    	
    	void clearPendings()
        {
    	    m_pendings.clear();
        }

        void cachePending(PendingState ps)
        {
            m_pendings.add(ps);
        }
        
        void remove(PendingState ps)
        {
            m_pendings.remove(ps);
        }
        
    	void processPendings()
    	{
    	    m_pendings.forEach(p -> p.submitCalculation());
    	    m_pendings.clear();
    	}
    	
        boolean isRecalculateAffected()
        {
            return m_isRecalculateAffected;
        }
        
        void setRecalculateAffected(boolean recalc)
        {
            m_isRecalculateAffected = recalc;
        }
        
    	/**
    	 * As the TableElement has been modified, remove from the cache any statistics
    	 * cached that involve the modified element
    	 * 
    	 * @param tse the modified TableElement 
    	 */
    	void remove(TableElement tse) 
    	{
    		if (!m_cachedAny)
    			return;
    		
    		assert tse != null : "TableElement required";
    		
    		m_cachedSVSEs.remove(tse);
    		removeStaleTVSEs(tse);
    		
    		// remove all subsets
    		for (Subset r : tse.getSubsets())
    		    remove(r);
		}

        private void removeStaleTVSEs(TableElement tse)
        {
            Set<Tuple<TableElement>> toRemove = new HashSet<Tuple<TableElement>>();
            for (Map.Entry<Tuple<TableElement>, TwoVariableStatEngine> e : m_cachedTVSEs.entrySet()) {
                Tuple<TableElement> key = e.getKey();
                if (tse == key.getFirstElement() || tse == key.getSecondElement())
                    toRemove.add(key);
            }
            
            m_cachedTVSEs.keySet().removeAll(toRemove);
        }

        public void cacheSVSE(TableElement d, SingleVariableStatEngine se) 
        {
            assert d != null : "TableElement required";
            assert se != null : "SingleVariableStatEngine required";
            
            m_cachedSVSEs.put(d, se);
            m_cachedAny = true;
        }
        
        public void cacheTVSE(TableElement e1, TableElement e2, TwoVariableStatEngine se) 
        {
            assert e1 != null && e2 != null: "TableElement required";
            assert se != null : "TwoVariableStatEngine required";
            
            m_cachedTVSEs.put(new Tuple<TableElement>(e1, e2), se);
            m_cachedAny = true;
        }
        
        public SingleVariableStatEngine getCachedSVSE(TableElement d)
        {
            assert d != null : "TableElement required";
            
            return m_cachedSVSEs.get(d);
        }
        
        public TwoVariableStatEngine getCachedTVSE(TableElement e1, TableElement e2)
        {
            assert e1 != null && e2 != null: "TableElement required";
            
            return m_cachedTVSEs.get(new Tuple<TableElement>(e1, e2));
        }       
    }
}
