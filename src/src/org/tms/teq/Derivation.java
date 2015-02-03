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

import org.tms.api.BaseElement;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.Subset;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.InvalidExpressionException;
import org.tms.api.exceptions.ReadOnlyException;

public class Derivation  
{
    public static final int sf_DEFAULT_PRECISION = 15;
    
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
            
            // create an evaluator
            PostfixStackEvaluator pfe = new PostfixStackEvaluator(deriv);
            deriv.m_pfe = pfe;
            
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
    
    public static void recalculateAffected(TableElement element)
    {
        List<Derivable> derivedElements = calculateDependencies(element);
        if (derivedElements == null || derivedElements.isEmpty()) return;
        
        // remove current element from recalc plan
        derivedElements.remove(element);
        
        // recalculate impacted elements
        Table parentTable = element.getTable();
        if (parentTable != null)
            parentTable.pushCurrent();
        try {
        	DerivationContext dc = new DerivationContext();
        	
            for (Derivable d : derivedElements) {
                Derivation deriv = d.getDerivation();
                deriv.recalculateTarget(element, dc);
            }
        }
        finally {
            if (parentTable != null)
                parentTable.popCurrent();
        }       
    }
    
    public static List<Derivable> calculateDependencies(TableElement modifiedElement)
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
        
        for (Derivable d : globalAffected) {
            if (!resolved.contains(d))
                resolveDependencies(d, resolved, unresolved);
        }
               
        List<Derivable> orderedDerivables = new ArrayList<Derivable>(resolved);        
        return orderedDerivables;
    }


    public static List<Derivable> calculateDependencies(Collection<Derivable> derived)
    {
        assert derived != null : "Set<Derived> required";
        
        int numAffected = derived.size();

        Set<Derivable> resolved = new LinkedHashSet<Derivable>(numAffected);
        Set<Derivable> unresolved = new HashSet<Derivable>(numAffected);
        
        for (Derivable d : derived) {
            if (!resolved.contains(d))
                resolveDependencies(d, resolved, unresolved);
        }
               
        List<Derivable> orderedDerivables = new ArrayList<Derivable>(resolved);        
        return orderedDerivables;
    }
    
    private static void resolveDependencies(Derivable d, Set<Derivable> resolved, Set<Derivable> unresolved)
    {
        List<TableElement> affectedBy = d.getAffectedBy();
        if (affectedBy != null) {
            unresolved.add(d);
            for (TableElement te : affectedBy) {
                if (!(te instanceof Derivable)) continue;
                
                Derivable ted = (Derivable)te;
                if (!ted.isDerived()) continue;
                
                if (!resolved.contains(ted))
                    resolveDependencies(ted, resolved, unresolved);
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

    private String m_asEntered;
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private PostfixStackEvaluator m_pfe;
    private Set<TableElement> m_affectedBy;
    private boolean m_parsed;
    private boolean m_converted;
    private Derivable m_target;
    private MathContext m_precision;
    
    private Derivation()
    {
        m_affectedBy = new LinkedHashSet<TableElement>();
        m_precision = new MathContext(sf_DEFAULT_PRECISION);
    }
    
    public Table getTable()
    {
        if (m_target != null)
            return m_target.getTable();
        else
            return null;
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
    }
    
    private void recalculateTarget(TableElement modifiedElement, DerivationContext dc)
    {
        if (m_target.isReadOnly())
            throw new ReadOnlyException((BaseElement)m_target, TableProperty.Derivation);
        
        Table t = m_target.getTable();
        t.pushCurrent();
        
        try {
            // currently support derived rows, columns, and cells, 
            // dispatch to correct handler
            if (m_target instanceof Column)
                recalculateTargetColumn(modifiedElement, dc);
            else if (m_target instanceof Row)
                recalculateTargetRow(modifiedElement, dc);
            else if (m_target instanceof Cell)
                recalculateTargetCell(modifiedElement, dc);
        }
        finally {      
            t.popCurrent();  
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
        
        Token t = m_pfe.evaluate(row, col, dc);
        if (t.isNumeric()) 
            t.setValue(applyPrecision(t.getNumericValue()));
        boolean modified = tbl.setCellValue(row, col, t);
        if (modified && dc != null) {
        	dc.remove(row);
        	dc.remove(col);
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
        	
            Token t = m_pfe.evaluate(row, col, dc);
            if (t.isNumeric() )
                t.setValue(applyPrecision(t.getNumericValue()));
            
            boolean modified = tbl.setCellValue(row, col, t);
            if (modified && dc != null) {
            	dc.remove(col);
            	anyModified = true;
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
        	
            Token t = m_pfe.evaluate(row, col, dc);
            if (t.isNumeric())
                t.setValue(applyPrecision(t.getNumericValue()));
            boolean modified = tbl.setCellValue(row, col, t);
            if (modified && dc != null) {
            	dc.remove(row);
            	anyModified = true;
            }
        }  
        
        if (anyModified && dc != null)
        	dc.remove(col);
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

    public void destroy()
    {
        // TODO Auto-generated method stub
        
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
    
    public String toString()
    {
        return String.format("%s [%s %s]", 
                this.getAsEnteredExpression(),
                isParsed() ? "parsed" : "", isConverted() ? "converted" : "");
    }
    
    protected  static class DerivationContext 
    {
    	private Map<TableElement, SingleVariableStatEngine> m_cachedSVSEs;
    	private boolean m_cachedAny = false;
    	
    	public DerivationContext()
    	{
    		m_cachedSVSEs = new HashMap<TableElement, SingleVariableStatEngine>();
    	}
    	
    	/**
    	 * As the TableElement has been modified, remove from the cache any statistics
    	 * cached that involve the modified element
    	 * 
    	 * @param tse the modified TableElement 
    	 */
    	public void remove(TableElement tse) 
    	{
    		if (!m_cachedAny)
    			return;
    		
    		assert tse != null : "TableElement required";
    		
    		m_cachedSVSEs.remove(tse);
    		for (Subset r : tse.getSubsets())
    		    remove(r);
		}

		public void cacheSVSE(TableElement d, SingleVariableStatEngine se) 
    	{
    		assert d != null : "TableElement required";
    		assert se != null : "SingleVariableStatEngine required";
    		
    		m_cachedSVSEs.put(d, se);
    		m_cachedAny = true;
    	}
    	
    	public SingleVariableStatEngine getCachedSVSE(TableElement d)
    	{
    		assert d != null : "TableElement required";
    		
    		return m_cachedSVSEs.get(d);
    	}
    }
}
