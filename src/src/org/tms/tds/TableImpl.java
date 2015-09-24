package org.tms.tds;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.tms.api.Access;
import org.tms.api.BaseElement;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Precisionable;
import org.tms.api.derivables.Token;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.events.TableElementListeners;
import org.tms.api.events.exceptions.BlockedRequestException;
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.InvalidParentException;
import org.tms.api.exceptions.NotUniqueException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.io.TableWriter;
import org.tms.io.options.IOOptions;
import org.tms.teq.DerivationImpl;
import org.tms.util.JustInTimeSet;

public class TableImpl extends TableCellsElementImpl implements Table, Precisionable
{
    /**
     * We allow each thread to maintain it's own current row/current column state for every table, this 
     * prevents pending calculations, that will iterate over table cells, to modify the current table state
     * for the "primary" thread.
     * 
     * We use ThreadLocal storage for this
     */
    private static ThreadLocal<Map<TableImpl,CellReference>> 
            sf_CURRENT_CELL = new ThreadLocal<Map<TableImpl, CellReference>>() {
        @Override
        protected Map<TableImpl, CellReference> initialValue ()
        {
            return new WeakHashMap<TableImpl, CellReference>();
        }
    };
    
    private static ThreadLocal<Map<TableImpl,Deque<CellReference>>> sf_CURRENT_CELL_STACK = new ThreadLocal<Map<TableImpl, Deque<CellReference>>>() {
        @Override
        protected Map<TableImpl, Deque<CellReference>> initialValue ()
        {
            return new WeakHashMap<TableImpl, Deque<CellReference>>();
        }
    };
    
    static Map<TableImpl, CellReference> getStaticCurrentCell()
    {
        return sf_CURRENT_CELL.get();
    }
    
    static Map<TableImpl,Deque<CellReference>> getStaticCurrentCellStack()
    {
        return sf_CURRENT_CELL_STACK.get();
    }
    
	public static final TableImpl createTable() 
	{
		return new TableImpl();
	}
	
	public static final TableImpl createTable(int nRows, int nCols) 
	{
		return new TableImpl(nRows, nCols);
	}
	
    public static final TableImpl createTable(ContextImpl c) 
    {
        return new TableImpl(ContextImpl.getPropertyInt(c, TableProperty.RowCapacityIncr), 
                             ContextImpl.getPropertyInt(c, TableProperty.ColumnCapacityIncr), c);
    }
    
    public static final TableImpl createTable(TableImpl t) 
    {
        ContextImpl tc = t.getTableContext();
        return new TableImpl(ContextImpl.getPropertyInt(tc, TableProperty.RowCapacityIncr), 
                             ContextImpl.getPropertyInt(tc, TableProperty.ColumnCapacityIncr), t);
    }
    
    public static final TableImpl createTable(int nRows, int nCols, ContextImpl c) 
    {
        return new TableImpl(nRows, nCols, c);
    }
    
    public static final TableImpl createTable(int nRows, int nCols, TableImpl t) 
    {
        return new TableImpl(nRows, nCols, t);
    }
    
    private ArrayList<RowImpl> m_rows;
    private ArrayList<ColumnImpl> m_cols;
    
    private Map<String, TableElementImpl> m_rowLabelIndex;
    private Map<String, TableElementImpl> m_colLabelIndex;
    private Map<String, TableElementImpl> m_subsetLabelIndex;
    private Map<String, TableElementImpl> m_cellLabelIndex;
    
    private CellReference m_currentCell;
    private Deque<CellReference> m_currentCellStack;
    private WeakReference<Thread> m_tableCreationThread;
    
    private int m_nextCellOffset;
    private Queue<Integer> m_unusedCellOffsets;
    private Map<Integer, RowImpl> m_cellOffsetRowMap;
    
    private Map<CellImpl, Set<SubsetImpl>> m_subsetedCells;
    private Map<CellImpl, DerivationImpl> m_derivedCells;
    private Map<CellImpl, Set<Derivable>> m_cellAffects;
    private Map<CellImpl, TableElementListeners> m_cellListeners;
    private Map<CellImpl, Map<String, Object>> m_cellElemProperties;
    
    private JustInTimeSet<SubsetImpl> m_subsets;
    
    private ContextImpl m_context;
    
    private int m_rowsCapacity;
    private int m_colsCapacity;
    
    //Initialized from context or source table
    private int m_rowCapacityIncr;
    private int m_colCapacityIncr;
    private int m_precision;
    private double m_freeSpaceThreshold;
    
    protected TableImpl()
    {
        this(ContextImpl.getPropertyInt(null, TableProperty.RowCapacityIncr),
             ContextImpl.getPropertyInt(null, TableProperty.ColumnCapacityIncr));
    }
    
    protected TableImpl(int nRows, int nCols)
    {
        this(nRows, nCols, ContextImpl.getDefaultContext());
    }

    protected TableImpl(int nRows, int nCols, ContextImpl c)
    {
        super(null);
        setTable(this);
        setContext(c);        
        
        initialize(nRows, nCols, null);
    }

    protected TableImpl(int nRows, int nCols, Table t)
    {
        super(null);
        setTable(this);
        setContext(t != null ? t.getTableContext() : ContextImpl.getDefaultContext());        
        
        if (t != null && !(t instanceof TableImpl))
        	throw new UnsupportedImplementationException(t);
        
        initialize(nRows, nCols, (TableImpl)t);
    }
    
    public ElementType getElementType()
    {
        return ElementType.Table;
    }
    
    private void initialize(int nRows, int nCols, TableImpl t)
    {
        // need to be initialized before calling initialize properties
        m_tableCreationThread = new WeakReference<Thread>(Thread.currentThread());
        
        m_rowLabelIndex = new HashMap<String, TableElementImpl>();
        m_colLabelIndex = new HashMap<String, TableElementImpl>();
        m_cellLabelIndex = new HashMap<String, TableElementImpl>();
        m_subsetLabelIndex = new HashMap<String, TableElementImpl>();
        
        // initialize values from context/defaults
        initializeProperties(t);
        
        // allocate base memory for rows and columns
        m_rows = new ArrayList<RowImpl>(m_rowsCapacity);
        m_cols = new ArrayList<ColumnImpl>(m_colsCapacity);
                
        setRowsCapacity(calcRowsCapacity(nRows));
        setColumnsCapacity(calcColumnsCapacity(nCols));
        
        // make sure a current cell is created for this table
        getCurrentCellReference();
        getCurrentCellStack();
        
        m_nextCellOffset = 0;
        
        // set all other arrays/sets/maps to null/JustInTime
        m_subsets = new JustInTimeSet<SubsetImpl>();
        m_unusedCellOffsets = new ArrayDeque<Integer>();
        m_cellOffsetRowMap = new HashMap<Integer, RowImpl>(getRowsCapacity());
        
        int expectedNoOfDerivedCells = m_rowsCapacity * m_colsCapacity / 5; // assume 20%
        m_derivedCells = new HashMap<CellImpl, DerivationImpl>(expectedNoOfDerivedCells);
        m_cellAffects = new HashMap<CellImpl, Set<Derivable>>(expectedNoOfDerivedCells);
        m_cellListeners = new ConcurrentHashMap<CellImpl, TableElementListeners>();
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, false);
        
        m_cellElemProperties = new ConcurrentHashMap<CellImpl, Map<String, Object>>();
        m_subsetedCells = new HashMap<CellImpl, Set<SubsetImpl>>();
        
        initializeSpecialized(t);
        // clear dirty flag, as table is empty
        markClean();
    }

    /**
     * Override in subclasses to perform subclass-specific initialization
     * @param t
     */
    protected void initializeSpecialized(TableImpl t) 
    {
	}

	@Override
    protected void initialize(TableElementImpl e) 
    {
        super.initialize(e);
    }
    
    protected void initializeProperties(TableImpl e)
    {
        BaseElementImpl source = getInitializationSource(e);
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {
                case RowCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = ContextImpl.sf_ROW_CAPACITY_INCR_DEFAULT;
                    setRowCapacityIncr((int)value);
                    break;
                    
                case ColumnCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = ContextImpl.sf_COLUMN_CAPACITY_INCR_DEFAULT;
                    setColumnCapacityIncr((int)value);
                    break;

                case FreeSpaceThreshold:
                    if (!isValidPropertyValueDouble(value))
                        value = ContextImpl.sf_FREE_SPACE_THRESHOLD_DEFAULT;
                    setFreeSpaceThreshold((double)value);
                    break;
                    
                case Precision:
                    if (!isValidPropertyValueInt(value))
                        value = DerivationImpl.sf_DEFAULT_PRECISION;
                    setPrecision((int)value);
                    break;

                case isAutoRecalculate:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_AUTO_RECALCULATE_DEFAULT;
                    setAutoRecalculate((boolean)value);
                    break;

                case isRowLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_ROW_LABELS_INDEXED_DEFAULT;
                    setRowLabelsIndexed((boolean)value);
                    break;
                    
                case isColumnLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_COLUMN_LABELS_INDEXED_DEFAULT;
                    setColumnLabelsIndexed((boolean)value);
                    break;
                    
                case isCellLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_CELL_LABELS_INDEXED_DEFAULT;
                    setCellLabelsIndexed((boolean)value);
                    break;
                    
                case isSubsetLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_SUBSET_LABELS_INDEXED_DEFAULT;
                    setSubsetLabelsIndexed((boolean)value);
                    break;
                    
                case isPersistant:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_TABLE_PERSISTANCE_DEFAULT;
                    setPersistant((boolean)value);
                    break;
                    
                case DisplayFormat:
                    if (!isValidPropertyValueString(value))
                        value = null;
                    setDisplayFormat((String)value);
                    break;
                    
                default:
                	if (initializeSpecializedProperty(tp, value))
                		break;
                	else if (!tp.isOptional())
                        throw new IllegalStateException("No initialization available for Table Property: " + tp);                       
            }
        }
    }
    
    boolean initializeSpecializedProperty(TableProperty tp, Object value) 
    {
    	return false;
    }
    
    /*
     * Methods defined by interface Table; mostly adapters
     */
    
    @Override
    public void export(String fileName, IOOptions options) 
    throws IOException
    {
        TableWriter writer = new TableWriter(this, fileName, options);
        writer.export();
    }

    @Override 
    synchronized public void delete(TableElement... elements)
    {
        if (elements != null) {
            boolean deletedAny = false;
            for (TableElement te : elements) {
                if (te == null)
                    continue;
                else if (te instanceof TableElementImpl) {
                    TableElementImpl tei = (TableElementImpl)te;
                    // don't redelete deleted items
                    if (tei.isInvalid())
                        continue;
                    if (te.getTable() == this) {
                        ((TableElementImpl)te).delete(false);
                        deletedAny = true;
                    }
                    else 
                        throw new InvalidParentException(te, this);
                }
                else
                    throw new UnsupportedImplementationException(te, "delete");
            }
            
            // compress data structures
            if (deletedAny) {
                reclaimColumnSpace();
                reclaimRowSpace();
            }
        }
    }
    /**
     * Reclaim free space in columns list. Free space is calculated as the total array 
     * capacity - the number of active columns. When this value divided by the column
     * expansion increment (ColumnCapacityIncr) equals or exceeds the limit specified
     * by FreeSpaceThreshold, free space is removed and returned to the system heap.
     * <p/>
     * If FreeSpaceThreshold is set to 0, free space is not removed. 
     */
    protected void reclaimColumnSpace()
    {
        // if the column count is 0, reset all row cell offsets
        int numCols = m_cols.size();       
        if (numCols == 0) {
            m_cellOffsetRowMap.clear();
            m_unusedCellOffsets.clear();
            
            if (m_nextCellOffset > 0)
                m_rows.forEach(r-> { if (r != null) r.setCellOffset(-1); });
            m_nextCellOffset = 0;
        }
        
        // if the threshold is 0, don't reclaim space
        double threshold = this.getFreeSpaceThreshold();        
        if (threshold <= 0.0) 
            return;
        
        // we want room for at least ColumnCapacityIncr columns
        int capacity = this.getColumnsCapacity();     
        int freeCols = capacity - numCols;      
        
        // Compute the ratio of total free columns to the
        // column capacity increment unit
        // if it is above the free space threshold, compress
        int incr = getColumnCapacityIncr();
        double ratio = (double)freeCols/(double)incr;
        
        if (ratio > threshold || numCols == 0) {
            // free the unused space in the cells array
            m_cols.trimToSize();
            if (numCols == 0) {
                numCols = incr;
            }
            
            setColumnsCapacity(numCols);
        }
    }
    
    /**
     * Reclaim free space in rows list. Free space is calculated as the total array 
     * capacity - the number of active rows. When this value divided by the row
     * expansion increment (RowCapacityIncr) equals or exceeds the limit specified
     * by FreeSpaceThreshold, free space is removed and returned to the system heap.
     * <p/>
     * If FreeSpaceThreshold is set to 0, free space is not removed. 
     */
    protected void reclaimRowSpace()
    {
        int numRows = m_rows.size();
        if (numRows == 0) {
            m_cellOffsetRowMap.clear();
            m_unusedCellOffsets.clear();
            m_nextCellOffset = 0;
            
            if (m_nextCellOffset > 0)
                m_cols.forEach(c-> { if (c != null) c.reclaimCellSpace(m_rows, 0); });            
        }
        
        // if the threshold is 0, don't reclaim space
        double threshold = this.getFreeSpaceThreshold();        
        if (threshold <= 0.0) 
            return;
        
        // we want room for at least ColumnCapacityIncr columns
        int capacity = this.getRowsCapacity();     
        int freeRows = capacity - numRows;      
        
        // Compute the ratio of total free rows to the
        // row capacity increment unit
        // if it is above the free space threshold, compress
        int incr = getRowCapacityIncr();
        double ratio = (double)freeRows/(double)incr;
        
        if (ratio >= threshold || numRows == 0) {
            // reclaim the cell space in each column
            reclaimCellSpace(numRows);
            
            // free the unused space in the cells array
            m_rows.trimToSize();
            if (numRows == 0)
                numRows = incr;
            setRowsCapacity(numRows);           
        }
    }
    
    /**
     * Reclaim free space in each column cell array, and reset all cell offsets in each row.
     * As a result of calling this method, the column cell arrays are all reordered in strict 
     * row order.
     * @param numRows
     */
    private void reclaimCellSpace(int numRows)
    {
        assert numRows == m_rows.size() : "Table rows inconsistent";       
        
        // if there are columns, and if the next cell offset > numRows, 
        // reclaim the unused space in the column cell arrays. If 
        // m_nextCellOffset > numRows, then we know that there are holes
        // in the cells array that can be compressed
        int numCols = m_cols.size();
        if (numCols > 0) {           
            m_cols.forEach(c -> { if (c != null) c.reclaimCellSpace(m_rows, numRows); } );
            if (m_nextCellOffset > numRows) {
                m_cellOffsetRowMap.clear();
                int cellOffset = 0;
                if (numRows > 0) {
                    for (RowImpl row : m_rows) {
                        if (row != null && row.getCellOffset() >= 0)
                            row.setCellOffset(numCols > 0 ? cellOffset++ : -1);
                    }
                }
        
                m_unusedCellOffsets.clear();
                m_nextCellOffset = cellOffset;
            }
        }
    }

    @Override
    public CellImpl getCell(Row row, Column col)
    {
        return getCell((RowImpl)row, (ColumnImpl)col);
    }    
    
    @Override
    public Object getCellValue(Row row, Column col)
    {
        return getCellValue((RowImpl)row, (ColumnImpl)col);        
    }
    
    @Override
    public String getFormattedCellValue(Row row, Column col)
    {
        CellImpl c = getCell((RowImpl)row, (ColumnImpl)col);
        if (c != null)
            return c.getFormattedCellValue();
        else
            return null;
    }
    
    @Override
    public boolean setCellValue(Row row, Column col, Object o)
    {
        return setCellValue((RowImpl)row, (ColumnImpl)col, o);        
    }

    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case RowCapacityIncr:
                return getRowCapacityIncr();
                
            case ColumnCapacityIncr:
                return getColumnCapacityIncr();
                
            case numRowsCapacity:
                return getRowsCapacity();
                
            case numColumnsCapacity:
                return getColumnsCapacity();
                
            case FreeSpaceThreshold:
                return getFreeSpaceThreshold();
                
            case numSubsets:
                return getNumSubsets();
                
            case Subsets:
                return getSubsets(); 
                
            case numRows:
                return getNumRows(); 
                
            case Rows:
                return getRows(); 
                
            case numColumns:
                return getNumColumns(); 
                
            case Columns:
                return getColumns(); 
                
            case numCells:
                return getNumCells(); 
                
            case NextCellOffset:
                return getNextCellOffset();
                
            case Precision:
                return getPrecision();
                
            case isAutoRecalculate:
                return isAutoRecalculate();               
                
            case isRowLabelsIndexed:
                return isRowLabelsIndexed();
                
            case isColumnLabelsIndexed:
                return isColumnLabelsIndexed();
                
            case isCellLabelsIndexed:
                return isCellLabelsIndexed();
                
            case isSubsetLabelsIndexed:
                return isSubsetLabelsIndexed();
                
            case isPersistant:
                return isPersistant();
                
            default:
                return super.getProperty(key);
        }
    }
    
    private TableContext setContext(TableContext c)
    {
    	if (c == null || c instanceof ContextImpl)
    		return setContext((ContextImpl)c);  	
    	else 
    		throw new UnsupportedImplementationException(c);
    }
    
    private TableContext setContext(ContextImpl c)
    {
        if (c == null)
            c = ContextImpl.getDefaultContext();
        
        c.deregister(this);        
        m_context = c.register(this);
        
        return m_context;
    }

    @Override
    public ContextImpl getTableContext()
    {
        return m_context;
    }

    @Override
    public TableImpl getTable()
    {
        return this;
    }

    @Override
    synchronized public List<Subset>getSubsets()
    {
        vetElement();
        return Collections.unmodifiableList(new ArrayList<Subset>(m_subsets.clone()));
    }

    @Override
    public int getNumSubsets()
    {
        return m_subsets.size();
    }
    
    @Override 
    synchronized public List<TableElementListener> removeAllListeners(TableElementEventType... evTs )
    {
        List<TableElementListener> tblListeners = super.removeAllListeners(evTs);
        
        // remove listeners from all rows and columns
        getColumnsInternal().forEach(c -> {if (c != null) c.removeAllListeners(evTs); });
        getRowsInternal().forEach(r -> {if (r != null) r.removeAllListeners(false, evTs); }); // don't reprocess cells
        getSubsetsInternal().forEach(s -> {if (s != null) s.removeAllListeners(evTs); });
        
        m_cellListeners.clear();
        
        // mark table as having no listeners
        TableElementListeners.deregisterTable(this);
        
        // return listeners on the this table
        return tblListeners;
    }
    
    void fireCellEvents(CellImpl cell, TableElementEventType evT, Object... args)
    {
        if (cell != null && evT != null && hasAnyListeners(evT)) {
            TableElementListeners listeners = m_cellListeners.get(cell);
            if (listeners != null)
                listeners.fireEvents(cell, evT, args);
            else if (evT.isAlertContainer())
                fireContainerEvents(cell, evT, args);
        }        
    }
    
    boolean addCellListener(CellImpl cell, TableElementEventType evT, TableElementListener[] tels)
    {
        if (cell != null && tels != null && tels.length > 0) {
            synchronized (m_cellListeners) {
                TableElementListeners listeners = m_cellListeners.get(cell);
                if (listeners == null) {
                    listeners = new TableElementListeners(cell, getTableContext().isEventsNotifyInSameThread());
                    m_cellListeners.put(cell, listeners);
                }
                
                return listeners.addListeners(evT, tels);
            }
        }
        
        return false;
    }

    boolean removeCellListener(CellImpl cell, TableElementEventType evT, TableElementListener[] tels)
    {
        if (cell != null) {
            TableElementListeners listeners = m_cellListeners.get(cell);
            if (listeners != null) {
                synchronized (m_cellListeners) {
                    boolean removedAny = listeners.removeListeners(evT, tels);                    
                    if (!listeners.hasListeners())
                        m_cellListeners.remove(cell);
                    
                    return removedAny;
                }
            }
        }
        
        return false;
    }

    List<TableElementListener> getCellListeners(CellImpl cell, TableElementEventType[] evTs)
    {
        if (cell != null) {
            TableElementListeners listeners = m_cellListeners.get(cell);
            if (listeners != null)
                return listeners.getListeners(evTs);
        }
        
        return Collections.emptyList();
    }

    List<TableElementListener> removeAllCellListeners(CellImpl cell, TableElementEventType... evTs)
    {
        if (cell != null) {
            Set<SubsetImpl> subsets =  getCellSubsets(cell);
            if (subsets != null) 
                subsets.forEach(s -> {if (s != null) s.removeAllListeners(evTs); });
            
            TableElementListeners listeners = m_cellListeners.remove(cell);
            if (listeners != null)
                return listeners.removeAllListeners(evTs);
        }
        
        return Collections.emptyList();
    }

    boolean hasCellListeners(CellImpl cell, TableElementEventType... evTs)
    {
        if (cell != null) {
            TableElementListeners listeners = m_cellListeners.get(cell);
            if (listeners != null)
                return listeners.hasListeners(evTs);
        }
        
        return false;
    }
    
    boolean hasAnyListeners(TableElementEventType... evTs)
    {
        return TableElementListeners.hasAnyListeners(this, evTs) ;
    }
    
    @Override
    synchronized protected void delete(boolean compress)
    {
        // handle onBeforeDelete processing
        try {
            super.delete(compress); // handle on before delete processing
        }
        catch (BlockedRequestException e) {
            return;
        }        
        
        try {
            // delete all rows and columns, explicitly
            // we have to iterate over the indexes as iterating
            // over the ArrayLists themselves throws a 
            // ConcurrentModificaton exception when items are deleted
            int numCols = m_cols.size();
            for (int i = numCols - 1; i >= 0; i--) {
                ColumnImpl col = m_cols.get(i);
                if (col != null)
                    col.delete(false);
            }
            
            int numRows = m_rows.size();
            for (int i = numRows - 1; i >= 0; i--) {
                RowImpl row = m_rows.get(i);
                if (row != null)
                    row.delete(false);
            }
    
            // getSubsets returns a copy of m_subsets, so it is safe to delete individual elements
            getSubsets().forEach(s -> { if (s != null) ((SubsetImpl)s).delete(false); } );
            
            // compress data structures
            if (compress) {
                reclaimColumnSpace();
                reclaimRowSpace();
            }
            
        	this.m_subsets.clear();
        	this.m_affects.clear();
        	this.m_cellAffects.clear();
        	this.m_cellOffsetRowMap.clear();
        	this.m_derivedCells.clear();
        	this.m_subsetedCells.clear();
        	this.m_unusedCellOffsets.clear();
        	this.m_cols.clear();
        	this.m_rows.clear();
        }
        finally {
            invalidate();
            
            sf_CURRENT_CELL.get().remove(this);
            sf_CURRENT_CELL_STACK.get().remove(this);
            
            m_currentCell = null;
            if (m_currentCellStack != null)
                m_currentCellStack.clear();
            m_currentCellStack = null;
            
            TableElementListeners.deregisterTable(this);
            if (getTableContext() != null)
                getTableContext().deregister(this);     
        }
    }    
    
    @Override
    public void finalize()
    {
        delete(false);
    }
    
    public boolean isAutoRecalculateEnabled()
    {
        return isAutoRecalculate() && !isSet(sf_AUTO_RECALCULATE_DISABLED_FLAG);
    }
    
    public boolean isAutoRecalculate()
    {
        return isSet(sf_AUTO_RECALCULATE_FLAG);
    }
    
    public void setAutoRecalculate(boolean value)
    {
        vetElement();
        set(sf_AUTO_RECALCULATE_FLAG, value);
    }

    public void deactivateAutoRecalculate()
    {
        vetElement();
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, true);
    }

    public void activateAutoRecalculate()
    {
        vetElement();
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, false);
    }
       
    public boolean isRowLabelsIndexed()
    {
        return isSet(sf_ROW_LABELS_INDEXED_FLAG);
    }

    public void setRowLabelsIndexed(boolean rowLabelsIndexed)
    {
        vetElement();
        if (!rowLabelsIndexed)
            m_rowLabelIndex.clear();
        else 
            indexLabels(m_rows, m_rowLabelIndex, sf_ROW_LABELS_INDEXED_FLAG);
        
        set(sf_ROW_LABELS_INDEXED_FLAG, rowLabelsIndexed);
    }

    public boolean isColumnLabelsIndexed()
    {
        return isSet(sf_COLUMN_LABELS_INDEXED_FLAG);
    }

    public void setColumnLabelsIndexed(boolean colLabelsIndexed)
    {
        vetElement();
        if (!colLabelsIndexed)
            m_colLabelIndex.clear();
        else 
            indexLabels(m_cols, m_colLabelIndex, sf_COLUMN_LABELS_INDEXED_FLAG);
        
        set(sf_COLUMN_LABELS_INDEXED_FLAG, colLabelsIndexed);
    }

    public boolean isSubsetLabelsIndexed()
    {
        return isSet(sf_SUBSET_LABELS_INDEXED_FLAG);
    }
    
    public void setSubsetLabelsIndexed(boolean subsetLabelsIndexed)
    {
        vetElement();
        if (!subsetLabelsIndexed)
            m_colLabelIndex.clear();
        else 
            indexLabels(m_subsets, m_subsetLabelIndex, sf_SUBSET_LABELS_INDEXED_FLAG);
        
        set(sf_SUBSET_LABELS_INDEXED_FLAG, subsetLabelsIndexed);
    }

    public boolean isCellLabelsIndexed()
    {
        return isSet(sf_CELL_LABELS_INDEXED_FLAG);
    }

    public void setCellLabelsIndexed(boolean cellLabelsIndexed)
    {
        vetElement();
        if (!cellLabelsIndexed)
            m_cellLabelIndex.clear();
        else 
            indexLabels(m_cellElemProperties.keySet(), m_cellLabelIndex, sf_CELL_LABELS_INDEXED_FLAG);
        
        set(sf_CELL_LABELS_INDEXED_FLAG, cellLabelsIndexed);
    }

    @Override
    public boolean isLabelIndexed()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    synchronized private void indexLabels(Collection<? extends TableElementImpl> elems, 
                                          Map<String, TableElementImpl> labelIndex, 
                                          int flagBit)
    {
        labelIndex.clear();
        
        if (elems == null) {
            set(flagBit, false);
            return;
        }
        
        for (TableElementImpl s : elems) {
            if (s == null)
                continue;
            
            String label = s.getLabel();
            if (label == null)
                continue;
            
            String key = label.toLowerCase();
            if (labelIndex.put(key, s) != null) {
                // found a duplicate label, can't index
                // reset map and clear flag
                labelIndex.clear();
                set(flagBit, false);
                
                throw new NotUniqueException(s, TableProperty.Label, label);
            }
        }        
    }

    Map<String, TableElementImpl> getElementIndex(ElementType et)
    {
        switch (et) {
            case Row:
                return m_rowLabelIndex;
                
            case Column:
                return m_colLabelIndex;
                
            case Cell:
                return m_cellLabelIndex;
                
            case Subset:
                return m_subsetLabelIndex;
                
            default:
                throw new UnsupportedImplementationException(et, "Label Index");
        }       
    }
    
    public boolean isPersistant()
    {
        return isSet(sf_IS_TABLE_PERSISTANT_FLAG);
    }
    
    public void setPersistant(boolean persistant)
    {
        vetElement();
        ContextImpl tc = null;
        if ((tc = getTableContext()) == null)
            persistant = false;
        else {
            if (persistant != isPersistant()) {
                if (persistant)
                    tc.registerPersistant(this);
                else
                    tc.registerNonpersistant(this);
            }
        }
        
        set(sf_IS_TABLE_PERSISTANT_FLAG, persistant);
    }
    
    @Override
    protected boolean isDataTypeEnforced()
    {
        return this.isEnforceDataType() || 
                (getTableContext() != null ? getTableContext().isEnforceDataType() : false);
    }

    @Override
    public boolean isNullsSupported()
    {
        return (getTableContext() != null ? getTableContext().isSupportsNull() : false)  && isSupportsNull();
    }
    
    synchronized protected boolean add(SubsetImpl r)
    {
        vetElement();
        vetParent(r);
        
        boolean processed = m_subsets.add(r);
        if (processed) setDirty(true);
        return processed;
    }
    
    /*
     * Delete TableElement methods
     */
    synchronized protected boolean remove(SubsetImpl r)
    {
        vetParent(r);
        return m_subsets.remove(r);
    }
 
    protected boolean isDirty()
    {
        return isSet(sf_IS_DIRTY_FLAG);
    }
    
    void setDirty(boolean dirty)
    {
        set(sf_IS_DIRTY_FLAG, dirty);
    }

    void markDirty() { setDirty(true); }
    void markClean() { setDirty(false); };
    
    protected TableImpl getTable(Access mode, Object...mda)
    {
        if (getTableContext() != null)
            return getTableContext().getTable(mode, mda);
        else
            return null;
    }
    
    /*
     * Cell manipulation routines
     */
    @Override
    synchronized public CellImpl getCell(Access mode, Object... mda)
    {
        vetElement();
        
        Object md = null;
        switch (mode) {
            case ByLabel:
            case ByDescription:
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Cell, mode, (md == null ? "<null>" : md.toString())));
                return (CellImpl)findCells(mode == Access.ByLabel ? TableProperty.Label : TableProperty.Description, md);
                
            case ByProperty:
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (key == null || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Cell, mode, (key == null ? "<null>" : key.toString()))); 
                
                // key must either be a table property or a string
                if (key instanceof TableProperty) 
                    return (CellImpl)findCells((TableProperty)key, value);
                else if (key instanceof String) 
                    return (CellImpl)findCells((String)key, value);
                else
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Cell, mode, (key == null ? "<null>" : key.toString())));  
                
            case ByReference:
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md != null && md instanceof CellImpl && ((CellImpl)md).getTable() == this)
                    return (CellImpl)md;
                else
                    return null;
                
            case Current:
                if (getCurrentRow() != null && getCurrentColumn() != null)
                    return getCell(getCurrentRow(), getCurrentColumn());
                else
                    return null;
                
            case First:
                if (getRow(Access.First) != null && getColumn(Access.First) != null)
                    return getCell(getRow(Access.First), getColumn(Access.First));
                else
                    return null;
                
            case Last:
                if (getRow(Access.Last) != null && getColumn(Access.Last) != null)
                    return getCell(getRow(Access.Last), getColumn(Access.Last));
                else
                    return null;
                
            default:
                throw new InvalidAccessException(ElementType.Table, ElementType.Cell, mode, false, mda);                
        }
    }
    
    private CellImpl findCells(String key, Object query)
    {
        for (ColumnImpl col : m_cols) {
            if (col != null) {
                for (CellImpl c : col.cellsInternal()) {
                    if (c != null) {
                        Object value = c.getProperty(key);
                        if (query.equals(value))
                            return c;
                    }
                }
            }
        }
        
        return null;
    }

    private CellImpl findCells(TableProperty key,  Object query)
    {
        if (key == TableProperty.Label && isCellLabelsIndexed())
            return (CellImpl)find(ElementType.Cell, null, key, query);
        
        for (ColumnImpl col : m_cols) {
            if (col != null) {
                for (CellImpl c : col.cellsInternal()) {
                    if (c != null) {
                        Object value = c.getProperty(key);
                        if (query.equals(value))
                            return c;
                    }
                }
            }
        }
        
        return null;
    }

    /*
     * Subset manipulation routines
     */
    @Override
    synchronized public SubsetImpl getSubset(Access mode, Object... mda)
    {
        vetElement();
        
        Object md = null;
        switch (mode) {
            case ByLabel:
            case ByDescription:
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Subset, mode, (md == null ? "<null>" : md.toString())));
                return (SubsetImpl)find(ElementType.Subset, m_subsets, mode == Access.ByLabel ? TableProperty.Label : TableProperty.Description, md);
                
            case ByProperty:
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (key == null || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Subset, mode, (key == null ? "<null>" : key.toString()))); 
                
                // key must either be a table property or a string
                if (key instanceof TableProperty) 
                    return (SubsetImpl)find(m_subsets, (TableProperty)key, value);
                else if (key instanceof String) 
                    return (SubsetImpl)find(m_subsets, (String)key, value);
                else
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Subset, mode, (key == null ? "<null>" : key.toString())));                 

            case ByReference:
            {
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof SubsetImpl) || (((SubsetImpl)md).getElementType() != ElementType.Subset))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Subset, mode, (md == null ? "<null>" : md.toString())));               
                return (SubsetImpl)md;
            }

            default:
                throw new InvalidAccessException(ElementType.Table, ElementType.Subset, mode, false, mda);                
        }
    }

    protected Set<SubsetImpl> getSubsetsInternal()
    {
        return m_subsets;
    }
    
    public SubsetImpl addSubset()
    {
        return addSubset(Access.First);
    }
    
    @Override
    synchronized public SubsetImpl addSubset(Access mode, Object... mda)
    {
        vetElement();
        
        // handle onBeforeCreate processing
        try {
            fireEvents(this, TableElementEventType.OnBeforeCreate, ElementType.Subset);
        }
        catch (BlockedRequestException e) {
            return null;
        } 
        
        Object md = null;
        Object md2 = null;
        if (mda != null && mda.length > 0) {
            md = mda[0];
            
            if (mda.length > 1)
                md2 = mda[1];
        }
        
        SubsetImpl subset = null;
        try {
            switch (mode) {
                case ByLabel:
                    subset = new SubsetImpl(this);
                    if (md != null && md instanceof String)
                        subset.setLabel((String)md);
                    break;
                    
                case ByDescription:
                    subset = new SubsetImpl(this);
                    if (md != null && md instanceof String)
                        subset.setDescription((String)md);
                    break;
                    
                case ByProperty:
                    subset = new SubsetImpl(this);
                    if (md != null && md2 != null) {
                        if (md instanceof TableProperty)
                            subset.setProperty((TableProperty)md, md2);
                        else if (md instanceof String)
                            subset.setProperty((String)md, md2);
                    }
                    break;
                    
                case First:
                case Last:
                case Next:
                case Previous:
                case Current:
                    subset = new SubsetImpl(this);
                    break;
                    
                default:
                    throw new InvalidAccessException(ElementType.Table, ElementType.Subset, mode, true, mda);                
            }
            
            return subset;
        }
        finally {
            if (subset != null)
                fireEvents(subset, TableElementEventType.OnCreate);
        }
    }
    
    public double getFreeSpaceThreshold()
    {
        if (m_freeSpaceThreshold < 0)
            m_freeSpaceThreshold = ContextImpl.getPropertyDouble(getTableContext(), TableProperty.FreeSpaceThreshold);

        return m_freeSpaceThreshold;
    }
    
    public void setFreeSpaceThreshold(double value)
    {
        if (value < 0.0) {
            m_rowCapacityIncr = -1;
            getFreeSpaceThreshold();
        }
        else
            m_freeSpaceThreshold = value;
    }

    /*
     * Row manipulation methods
     */
    
    /**
     * Return the capacity of the Rows list. This is not the same as the actual number
     * of created rows, see {@link #getNumRows()}
     * @return
     */
    protected int getRowsCapacity()
    {
        return m_rowsCapacity;
    }
    
    /**
     * Sets the maximum capacity of the Rows data structure, (re)allocating space as needed
     * @param capacity
     */
    protected void setRowsCapacity(int capacity)
    {
        if (m_rows != null) {
            m_rows.ensureCapacity(capacity);
            m_rowsCapacity = capacity;
        }
    }
    
    protected int calcRowsCapacity(int nRows)
    {
        int capacity = getRowCapacityIncr();
        if (nRows > 0) {
            int remainder = nRows % capacity;
            capacity = nRows + (remainder > 0 ? capacity - remainder : 0);
        }
        
        return capacity;
    }
    
    public int getRowCapacityIncr()
    {
        if (m_rowCapacityIncr <= 0) 
            m_rowCapacityIncr = ContextImpl.getPropertyInt(getTableContext(), TableProperty.RowCapacityIncr);
        
        return m_rowCapacityIncr;
    }

    public void setRowCapacityIncr(int rowCapacityIncr)
    {
        if (rowCapacityIncr <= 0) {
            // force a reset of the row capacity value
            m_rowCapacityIncr = 0;
            getRowCapacityIncr();
        }
        else
            m_rowCapacityIncr = rowCapacityIncr;
    }
    
    public int getNumRows()
    {
        return m_rows == null ? 0 : m_rows.size();
    }
    
    public RowImpl getCurrentRow()
    {
        CellReference cr = getCurrentCellReference();
        return cr.getCurrentRow();
    }
    
    protected RowImpl setCurrentRow(RowImpl row)
    {
        CellReference cr = getCurrentCellReference();
        RowImpl prevCurrent = cr.getCurrentRow();
        cr.setCurrentRow(row);
        
        return prevCurrent;
    }  
    
    @Override
    public RowImpl addRow()
    {
        return addRow(Access.Last);
    }
    
    @Override
    public RowImpl addRow(int idx)
    {
        return addRow(Access.ByIndex, idx);
    }
    
    protected RowImpl addRow(Access mode)
    {
        return addRow(mode, (Object [])null);
    }
    
    @Override
    synchronized public RowImpl addRow(Access mode, Object... mda)
    {
        vetElement();
        return (RowImpl)add(new RowImpl(this), mode, mda);
    }
    
    @Override
    synchronized public RowImpl getRow(Access mode, Object...mda)
    {
        vetElement();
        return getRowInternal(true, true, mode, mda);
    }
    
    @Override
    public RowImpl getRow()
    {
        return getCurrentRow();
    }
    
    @Override
    public RowImpl getRow(int idx)
    {
        return getRow(Access.ByIndex, idx);
    }
    
    @Override
    public RowImpl getRow(String label)
    {
        return getRow(Access.ByLabel, label);
    }
    
    protected RowImpl getRowInternal(boolean createIfNull, boolean setCurrent, Access mode, Object...mda)
    {
        RowImpl r = null;
        
        // calculate row index
        int rowIdx = this.calcIndex(ElementType.Row, mode, false, mda);
        if (rowIdx < 0)
            return r;
        
        // retrieve the row, now that we have a valid reference
        r = getRowsInternal().get(rowIdx);
        
        // if the row is null, create it
        if (r == null && createIfNull) {
            r = new RowImpl(this);
            r.setIndex(rowIdx + 1);
            
            // add the row to the Rows array at the correct index
            getRowsInternal().set(rowIdx,  r);
        }
        
        if (setCurrent && r != null)
        	r.setCurrent();
        
        return r;
    }
    
    /**
     * As the rows array is created sparse, where individual rows are created 
     * only when accessed, this method is needed to build out all rows when
     * in the case where they are to be iterated over
     */
    synchronized void ensureRowsExist()
    {
        int nRows = getNumRows();
        for (int i = 1; i <= nRows; i++) {
            if (m_rows.get(i - 1) == null)
                getRowInternal(true, false, Access.ByIndex, i);
        }
    }
    
    /**
     * Returns an available cell offset value
     * @return
     */
    synchronized int calcNextAvailableCellOffset() 
    {
        Integer availableOffset = this.m_unusedCellOffsets.poll();
        if (availableOffset != null) {
            assert availableOffset >= 0 : "Invalid CellImpl Offset Value";
            return availableOffset;
        }
        
        // otherwise, just return the next available offset
        return m_nextCellOffset++;
    }
         
    protected int getNextCellOffset()
    {
        return m_nextCellOffset;
    }
    
    /**
     * Cache the cell offset from the deleted row, allowing it to be reused for a 
     * new row at a later date
     * @param cellOffset
     */
	synchronized void cacheCellOffset(int cellOffset, boolean freeCellsNow) 
	{
	    if (cellOffset < 0)
	        return;
	    
		assert cellOffset >= 0 : "Invalid value";
		
		// add the cellOffset value to the queue of available/freed offset values
		// these represent positions in the Column.m_cells array that are available
		// for reuse
        m_cellOffsetRowMap.remove(cellOffset);
		m_unusedCellOffsets.offer(cellOffset);
		
		// if so-requested, free the cells in the component columns array
		for (ColumnImpl c : getColumnsInternal()) {
			if (c != null) 
				c.invalidateCell(cellOffset);
		}
	}

	protected RowImpl getRowByCellOffset(int cellOffset) 
	{
		if (cellOffset >= 0) {
		    RowImpl r = m_cellOffsetRowMap.get(cellOffset);
		    if (r != null)
		        return r;
		    
		    // as a last resort, do a sequential search
			for (RowImpl row : getRowsInternal())
				if (row != null && row.getCellOffset() == cellOffset) {
				    if (m_cellOffsetRowMap != null)
				        m_cellOffsetRowMap.put(cellOffset, row);
					return row;
				}
		}
		
		return null;
	}
	
    void mapCellOffsetToRow(RowImpl row, int offset)
    {
        if (row != null && offset >= 0)
            m_cellOffsetRowMap.put(offset, row);
    }
    
    /*
     * Column manipulation methods
     */
    @Override
    public int getColumnCapacityIncr()
    {
        if (m_colCapacityIncr <= 0) 
            m_colCapacityIncr = ContextImpl.getPropertyInt(getTableContext(), TableProperty.ColumnCapacityIncr);
        
        return m_colCapacityIncr;
    }

    @Override
    public void setColumnCapacityIncr(int colCapacityIncr)
    {
        if (colCapacityIncr <= 0) {
            // force a reset of the row capacity value
            colCapacityIncr = 0;
            getColumnCapacityIncr();
        }
        else
            m_colCapacityIncr = colCapacityIncr;
    }
    
    protected int calcColumnsCapacity(int nCols)
    {
        int capacity = getColumnCapacityIncr();
        if (nCols > 0) {
            int remainder = nCols % capacity;
            capacity = nCols + (remainder > 0 ? capacity - remainder : 0);
        }
        
        return capacity;
    }

    protected int getColumnsCapacity()
    {
        return m_colsCapacity;
    }

    protected void setColumnsCapacity(int capacity)
    {
        if (m_cols != null) {
            m_cols.ensureCapacity(capacity);
            m_colsCapacity = capacity;
        }
    }

    public int getNumColumns()
    {
        return m_cols == null ? 0 : m_cols.size();
    }   
    
    /**
     * Return the raw columns arraylist. Allows Column class to insert a column into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;ColumnImpl&gt;
     */
    ArrayList<ColumnImpl> getColumnsInternal()
    {
        return m_cols;
    }

    @Override
    synchronized public List<Column> getColumns()
    {
        vetElement();
        ensureColumnsExist();
        return Collections.unmodifiableList(new ArrayList<Column>(getColumnsInternal()));
    }
    
    public ColumnImpl getCurrentColumn()
    {
        CellReference cr = getCurrentCellReference();
        return cr.getCurrentColumn();
    }
    
    synchronized protected ColumnImpl setCurrentColumn(ColumnImpl col)
    {
        vetParent(col);
        CellReference cr = getCurrentCellReference();
        ColumnImpl prevCurrent = cr.getCurrentColumn();
        cr.setCurrentColumn(col);
        
        return prevCurrent;
    }  
    
    @Override
    public ColumnImpl addColumn()
    {
        return addColumn(Access.Last);
    }
    
    @Override
    public ColumnImpl addColumn(int idx)
    {
        return addColumn(Access.ByIndex, idx);
    }
    
    protected ColumnImpl addColumn(Access mode)
    {
        return addColumn(mode, (Object [])null);
    }
    
    @Override
    synchronized public ColumnImpl addColumn(Access mode, Object... md)
    {
        vetElement();
        return (ColumnImpl)add(new ColumnImpl(this), mode, md);
    }
    
    @Override
    synchronized public ColumnImpl getColumn(Access mode, Object...mda)
    {
        vetElement();
        return getColumnInternal(true, true, mode, mda);
    }
        
    @Override
    public ColumnImpl getColumn()
    {
        return getCurrentColumn();
    }
    
    @Override
    public ColumnImpl getColumn(int idx)
    {
        return getColumn(Access.ByIndex, idx);
    }
    
    @Override
    public ColumnImpl getColumn(String label)
    {
        return getColumn(Access.ByLabel, label);
    }
    
    protected ColumnImpl getColumnInternal(boolean createIfNull, boolean setCurrent, Access mode, Object...mda)
    {
        ColumnImpl r = null;
        
        // calculate column index
        int colIdx = this.calcIndex(ElementType.Column, mode, false, mda);
        if (colIdx < 0)
            return r;
        
        // retrieve the column, now that we have a valid reference
        r = getColumnsInternal().get(colIdx);
        
        // if the column is null, create it
        if (r == null && createIfNull) {
            r = new ColumnImpl(this);
            r.setIndex(colIdx + 1);
            
            // add the column to the Columns array at the correct index
            getColumnsInternal().set(colIdx,  r);
        }
        
        if (setCurrent && r != null)
            r.setCurrent();
        
        return r;
    }

    synchronized void ensureColumnsExist()
    {
        int nCols = getNumColumns();
        for (int i = 1; i <= nCols; i++) {
            if (m_cols.get(i - 1) == null)
                getColumnInternal(true, false, Access.ByIndex, i);
        }
    }
    
    @Override
    public int getPrecision()
    {
        if (m_precision == Integer.MAX_VALUE || m_precision == Integer.MIN_VALUE) 
            m_precision = ContextImpl.getPropertyInt(getTableContext(), TableProperty.Precision);
        
        return m_precision;
    }

    @Override
    public void setPrecision(int precision)
    {
        m_precision = precision;
    }
    
    public String getDisplayFormat()
    {
        return (String)getProperty(TableProperty.DisplayFormat);
    }
    
    public void setDisplayFormat(String value)
    {
        if (value != null && (value = value.trim()).length() > 0)
            setProperty(TableProperty.DisplayFormat, value);
        else
            this.clearProperty(TableProperty.DisplayFormat);        
    }

    protected TableSliceElementImpl add(TableSliceElementImpl tse, Access mode, Object... md)
    {
        return add(tse, true, true, mode, md);
    }
    
    synchronized protected TableSliceElementImpl add(TableSliceElementImpl tse, 
                                                     boolean setCurrent, boolean fireEvents,
                                                     Access mode, Object... md)
    {
        vetElement();

        // handle onBeforeDelete processing
        try {
            if (fireEvents)
                fireEvents(this, TableElementEventType.OnBeforeCreate, tse.getElementType(), mode, md);
        }
        catch (BlockedRequestException e) {
            return null;
        }        
        
        boolean successfullyCreated = false;
        try {
            // calculate the index where the row will go
            ElementType sliceType = tse.getElementType();
            int idx = calcIndex(sliceType, mode, true, md);
            if (idx <= -1)
                throw new InvalidAccessException(ElementType.Table, sliceType, mode, true, md);
            
            // insert row into data structure at correct index
            if (tse.insertSlice(idx) != null && setCurrent)
                tse.setCurrent();
            
            successfullyCreated = true;
            return tse;
        }
        finally {
            if (fireEvents && successfullyCreated)
                fireEvents(tse, TableElementEventType.OnCreate);
        }
    }
    
    protected int calcIndex(ElementType et, Access mode)
    {
        return calcIndex(et, mode, false);
    }
    
    protected int calcIndex(ElementType et, Access mode, boolean isAdding)
    {
        return calcIndex(et, mode, isAdding, (Object [])null);
    }
    
    protected int calcIndex(ElementType et, Access mode, boolean isAdding, Object... mda)
    {
        int numSlices = -1;
        TableSliceElementImpl curSlice = null;
        ArrayList<? extends TableSliceElementImpl> slices;
        
        if (et == ElementType.Row) {
            numSlices = getNumRows();
            curSlice = getCurrentRow();
            slices = getRowsInternal();
        }
        else if (et == ElementType.Column) {
            numSlices = getNumColumns();
            curSlice = getCurrentColumn();
            slices = getColumnsInternal();
        }
        else
            throw new UnimplementedException(et, "calcIndex not supported");
        
        // if we are doing a retrieval (not adding), and there are no slices, we're done
        if (!isAdding && numSlices == 0)
            return -1;
        
        int idx = -1;
        switch (mode)
        {
            case First:
                return 0;
                
            case Last:
                if (isAdding)
                    return numSlices == 0 ? 0 : numSlices;
                else
                    return numSlices == 0 ? -1 : numSlices - 1;
                
            case Previous:
                // special case for adding to an empty table
                if (isAdding && numSlices == 0)
                    return 0;
                
                // if there is no current row, this can't be done
                if (curSlice == null)
                    return -1;
                
                // if we are adding, insert the cell at the current position (e.g., the same as Access.Current)
                // if we are retrieving, then retrieve the cell before the current one
                idx = curSlice.getIndex() - 1;                

                // If adding a row, return the current element's array index
                if (isAdding)
                  return idx;
                
                // if at the first row, there can be no previous
                else if (idx <= 0)
                    return -1;
                else 
                    return (idx - 1);
                
            case Current:
                // special case for adding to an empty table
                if (isAdding && numSlices == 0)
                    return 0;
                
                // if there is no current row, this can't be done
                if (curSlice == null)
                    return -1;
                
                // indexes are 1-based; element arrays are 0-based
                return curSlice.getIndex() - 1;
                
            case Next:
                // special case for adding to an empty table
                if (isAdding && numSlices == 0)
                    return 0;
                
                // if there is no current row, this can't be done
                if (curSlice == null)
                    return -1;
                
                idx = curSlice.getIndex();
                if (idx < numSlices)
                	return idx;
                else if (isAdding && idx == numSlices)
                	return idx;
                else
                    return -1;
                
            case ByIndex:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof Integer))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));
                
                idx = ((int)md) - 1;               
                if (idx < 0)
                    return -1;
                else if (isAdding || idx < numSlices)
                    return idx;
                else 
                    return -1;
            }
                
            case ByReference:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof TableSliceElementImpl) || (((TableSliceElementImpl)md).getElementType() != et))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));               
                // indexes are 1-based; element arrays are 0-based
                return ((TableSliceElementImpl)md).getIndex() - 1;
            }
                
            case ByLabel:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableSliceElementImpl target = (TableSliceElementImpl)find(et, slices, TableProperty.Label, md);
                // indexes are 1-based; element arrays are 0-based
                if (target != null)
                    return target.getIndex() - 1;
                break;
            }
            
            case ByDescription:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableSliceElementImpl target = (TableSliceElementImpl)find(et, slices, TableProperty.Description, md);
                // indexes are 1-based; element arrays are 0-based
                if (target != null)
                    return target.getIndex() - 1;
                break;
            }
            
            case ByProperty:
            {
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (isAdding || key == null || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (key == null ? "<null>" : key.toString()))); 
                
                // key must either be a table property or a string
                TableSliceElementImpl target;
                if (key instanceof TableProperty) 
                    target = (TableSliceElementImpl)find(et, slices, (TableProperty)key, value);
                else if (key instanceof String) 
                    target = (TableSliceElementImpl)find(slices, (String)key, value);
                else
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (key == null ? "<null>" : key.toString()))); 
                    
                // indexes are 1-based; element arrays are 0-based
                if (target != null)
                    return target.getIndex() - 1;
                break;
            }
        }
        
        // if we get here, return the default, which indicates an error
        return -1;
    }
    
    protected BaseElement find(ElementType et, Collection<? extends BaseElement> slices, TableProperty key, Object value)
    {
        if (key == TableProperty.Label) {
            switch (et) {
                case Row:
                    if (isRowLabelsIndexed())
                        return findIndexedElement(m_rowLabelIndex, value);
                    break;
                    
                case Column:
                    if (isColumnLabelsIndexed())
                        return findIndexedElement(m_colLabelIndex, value);
                    break;
                    
                case Cell:
                    if (isCellLabelsIndexed())
                        return findIndexedElement(m_cellLabelIndex, value);
                    break;
                                        
                case Subset:
                    if (isSubsetLabelsIndexed())
                        return findIndexedElement(m_subsetLabelIndex, value);
                    break;
                    
                default:
                    break;
            }
        }
        
        return find(slices, key, value);
    }
    
    private BaseElement findIndexedElement(Map<String, TableElementImpl> elemIndex, Object keyValue)
    {
        String key;
        if (elemIndex == null || keyValue == null || 
                (key = keyValue.toString().trim().toLowerCase()).length() == 0)
            return null;
        
        return elemIndex.get(key);
    }

    
    @Override
    synchronized public int getNumCells()
    {
        vetElement();
        int numCells = 0;
        for (Column c : columns()) {
            if (c != null) {
                ColumnImpl col = (ColumnImpl)c;
                numCells += col.getNumCells();
            }
        }
        
        return numCells;
    }
    
    protected CellImpl getCell(RowImpl row, ColumnImpl col)
    {
        return getCell(row, col, true);
    }
    
    synchronized protected CellImpl getCell(RowImpl row, ColumnImpl col, boolean createIfNull)
    {
        vetElement();
        if (row == null || col == null)
            return null;
        
        vetElement(row);
        vetElement(col);
        
        if (this != row.getTable())
            throw new InvalidParentException(row, this);        
        if (this != col.getTable())
            throw new InvalidParentException(col, this);
        
        return col.getCellInternal(row, createIfNull, true);
    }
    
    synchronized protected boolean setCellValue(RowImpl row, ColumnImpl col, Object o) 
    {
        CellImpl cell = getCell(row, col, o != null);
        if (cell != null) {
            if (o instanceof Token)
                return cell.postResult((Token)o);
            else
                return cell.setCellValue(o);
        }
        else
        	return false;
    }
    
    synchronized protected Object getCellValue(RowImpl row, ColumnImpl col) 
    {
        CellImpl cell = getCell(row, col, false);
        if (cell != null) 
            return cell.getCellValue();
        else
            return null;
    }
    
    /**
     * Empty tables contain no rows or columns
     */
    @Override
    public boolean isNull()
    {
        return getNumRows() == 0 || getNumColumns() == 0 || getNumCells() == 0;
    }
    
    @Override
    public boolean isWriteProtected()
    {
        return isReadOnly() ||
               (getTableContext() != null ? getTableContext().isReadOnly() : false);
    }
    
    @Override
    synchronized public boolean fill(Object o) 
    {
        vetElement();
        
        boolean setSome = false;
        CellReference cr = getCurrent();
        deactivateAutoRecalculate();
        try {
            ColumnImpl c = getColumn(Access.First);
            while (c != null) {
                if (c.fill(o, false, false, false))
                    setSome = true;
                c = getColumn(Access.Next);
            }
        }
        finally {  
            activateAutoRecalculate();
            cr.setCurrentCellReference(this);
        }
        
        if (setSome)
            fireEvents(this, TableElementEventType.OnNewValue, o);
        
        // no need to recalc table, as filling all cells
        // clears all derivations
        
        return setSome;
    }  
    
    @Override
    public boolean clear() 
    {
        return fill(null);
    }  
	
    @Override
    public void popCurrent() 
    {
        Deque<CellReference> currentCellStack = getCurrentCellStack();
		if (currentCellStack != null && !currentCellStack.isEmpty()) {
			CellReference cr = currentCellStack.pollFirst();
			if (cr != null) 
			    cr.setCurrentCellReference(this);
		}		
	}

    @Override
    public void pushCurrent() 
    {
        vetElement();
        Deque<CellReference> currentCellStack = getCurrentCellStack();
        CellReference cr = getCurrent();
        currentCellStack.push(cr);
    }

    public void purgeCurrentStack(TableSliceElementImpl slice)
    {
        Deque<CellReference> currentCellStack = getCurrentCellStack();
        if (currentCellStack  != null) 
            currentCellStack.removeIf(new CellStackPurger(slice));
    }

    private CellReference getCurrentCellReference()
    {
        CellReference cr = null;
        if (Thread.currentThread() == m_tableCreationThread.get()) {
            if (m_currentCell == null)
                m_currentCell = new CellReference();
            cr = m_currentCell;
        }
        else {
            Map<TableImpl,CellReference> currentCellMap = sf_CURRENT_CELL.get();
            cr = currentCellMap.get(this);
            if (cr == null) {
                cr = new CellReference();
                currentCellMap.put(this, new CellReference(cr));
            }
        }
        
        return cr;
    }
    
    private Deque<CellReference> getCurrentCellStack()
    {
        Deque<CellReference> currentCellStack = null;
        if (Thread.currentThread() == m_tableCreationThread.get()) {
            if (m_currentCellStack == null)
                m_currentCellStack = new ArrayDeque<CellReference>(); 
            currentCellStack = m_currentCellStack;
        }
        else {
            Map<TableImpl, Deque<CellReference>> currentCellStackMap = sf_CURRENT_CELL_STACK.get();
            currentCellStack = currentCellStackMap.get(this);
            if (currentCellStack == null) {
                currentCellStack = new ArrayDeque<CellReference>();
                currentCellStackMap.put(this, currentCellStack);
            }
        }

        return currentCellStack;
    }
    
    CellReference getCurrent() 
    {
        CellReference cr = new CellReference(getCurrentCellReference());
        return cr;
    }
    
	@Override
	synchronized public void recalculate()
	{
        vetElement();        
        CellReference cr = getCurrent();
        try {
            DerivationImpl.recalculateAffected(this);
            
            fireEvents(this, TableElementEventType.OnRecalculate);
        }
        finally {
            cr.setCurrentCellReference(this);
        }
	}
	
    DerivationImpl getCellDerivation(CellImpl cell)
    {
        return m_derivedCells.get(cell);
    }
    
    DerivationImpl registerDerivedCell(CellImpl cell, DerivationImpl d)
    {
        if (cell != null && d != null) {
            cell.set(sf_IS_DERIVED_CELL_FLAG, true);
            return m_derivedCells.put(cell, d);
        }
        else
            return null;
    }
    
    DerivationImpl deregisterDerivedCell(CellImpl cell)
    {
        if (cell != null) {
            cell.set(sf_IS_DERIVED_CELL_FLAG, false);
            return m_derivedCells.remove(cell);
        }
        else
            return null;
    }    

    int getNumDerivedCells()
    {
        return m_derivedCells.size();
    }
    
    @Override
    public List<Derivable> getDerivedElements()
    {
        vetElement();
        Set<Derivable> derived = new LinkedHashSet<Derivable>();
        
        CellReference cr = getCurrent();
        try {
            for (ColumnImpl col : getColumnsInternal()) {
                if (col != null && col.isDerived())
                    derived.add(col);
            }
            
            for (RowImpl row : getRowsInternal()) {
                if (row != null && row.isDerived())
                    derived.add(row);
            }
            
            for (CellImpl cell : derivedCells())
                derived.add(cell);
        
            return Collections.unmodifiableList(new ArrayList<Derivable>(derived));
        }
        finally {
            cr.setCurrentCellReference(this);
        }       
    }    

    Map<String, Object> getCellElemProperties(CellImpl cell, boolean createIfEmpty)
    {
        if (cell != null) {
            synchronized(cell) {
                Map<String, Object> cellElemProperties = m_cellElemProperties.get(cell);
                if (createIfEmpty && cellElemProperties == null) {
                    cellElemProperties = new HashMap<String, Object>();
                    m_cellElemProperties.put(cell, cellElemProperties);
                }
            
                return cellElemProperties;
            }
        }

        return null;
    }
    
    void resetCellElemProperties(CellImpl cell)
    {
        if (cell != null) 
            m_cellElemProperties.remove(cell);
    }
    
    /**
     * Registers the {@code Derivable} as being affected by changes to the
     * value of the specified cell
     * @param cell that effects {@code Derivable}
     * @param d the {@code Derivable} affected
     */
    protected void registerAffects(CellImpl cell, Derivable d)
    {
        assert cell != null : "Cell required";
        assert d != null : "Derivable required";
        
        Set<Derivable> affected = null;
        synchronized (m_cellAffects) {
            affected = m_cellAffects.get(cell);
            if (affected == null) {
                affected = new HashSet<Derivable>();
                m_cellAffects.put(cell, affected);
            }
            
            affected.add(d);
        }
    }
    
    protected void deregisterAffects(CellImpl cell, Derivable d)
    {
        assert cell != null : "Cell required";
        assert d != null : "Derivable required";
        
        synchronized (m_cellAffects) {
            Set<Derivable> affected = m_cellAffects.get(cell);
            if (affected != null) {
                affected.remove(d);
                
                if (affected.isEmpty())
                    m_cellAffects.remove(cell);
            }
        }
    }
    
    protected List<Derivable> getCellAffects(CellImpl cell, boolean includeIndirects)
    {
        assert cell != null : "Cell required";
        
        Set<Derivable> affected = m_cellAffects.get(cell);
        
        int numAffects = 0;
        Set<Derivable> affects = new HashSet<Derivable>(affected != null ? (numAffects = affected.size()) : 0);
        if (numAffects > 0)
            affects.addAll(affected);
        
        if (includeIndirects) {
            // also add parent column and row affects
            ColumnImpl col = cell.getColumn();
            if (col != null)
                affects.addAll(col.getAffects());
            
            RowImpl row = cell.getRow();
            if (row != null)
                affects.addAll(row.getAffects());
        }
        
        // remove this element to avoid cycles
        affects.remove(cell);
        
        return new ArrayList<Derivable>(affects);
    }
    
    protected int getNumDerivedCellsAffects()
    {
        return m_cellAffects.size();
    }
    
    protected Iterable<CellImpl> derivedCells()
    {
        return new BaseElementIterableInternal<CellImpl>(m_derivedCells.keySet());
    }
    
    protected boolean registerSubsetCell(CellImpl cell, SubsetImpl r)
    {
        Set<SubsetImpl> subsets = m_subsetedCells.get(cell);
        if (subsets == null) {
            subsets = new HashSet<SubsetImpl>();
            m_subsetedCells.put(cell, subsets);
        }
        
        return subsets.add(r);
    }

    protected boolean deregisterSubsetCell(CellImpl cell, SubsetImpl r)
    {
        Set<SubsetImpl> subsets = m_subsetedCells.get(cell);
        if (subsets != null) {
            boolean removed = subsets.remove(r);
            
            // purge the cell from the subset set, if no subsets remain
            // otherwise, the cell may linger once deleted
            if (subsets.isEmpty())
                m_subsetedCells.remove(cell);
            
            return removed;
        }
        else
            return false;
    }
    
    protected Set<SubsetImpl> getCellSubsets(CellImpl cell)
    {
        return m_subsetedCells.get(cell);
    }
    
    protected List<Derivable> getAffects(TableElementImpl te)
    {
        if (te == null) return null;
        else if (te instanceof TableCellsElementImpl)
            return ((TableCellsElementImpl)te).getAffects();
        else if (te instanceof CellImpl) {
            return getCellAffects((CellImpl)te, true);
        }
            
        return null;
    }
    
	protected void sort(TableSliceElementImpl tse) 
	{
	    if (tse instanceof ColumnImpl) {
	        TableSliceElementComparator rowSorter = new TableSliceElementComparator((ColumnImpl)tse);
    		Collections.sort(m_rows, rowSorter);
            reindex(m_rows);
	    }
	    else if (tse instanceof RowImpl) {
	        TableSliceElementComparator colSorter = new TableSliceElementComparator((RowImpl)tse);
            Collections.sort(m_cols, colSorter);
            reindex(m_cols);
	    }
	}

	protected void sort(TableSliceElementImpl tse, Comparator<Cell> cellSorter)
	{
        if (tse instanceof ColumnImpl) {
            TableSliceElementComparator rowSorter = new TableSliceElementComparator((ColumnImpl)tse);
            Collections.sort(m_rows, rowSorter);
            reindex(m_rows);
        }
	}
	
	@Override
	public void sort(ElementType et, TableProperty tp, TableRowColumnElement... others)
	{
	    vetElement();
	    if (et == null || (tp == null && (others == null || others.length == 0)))
	        throw new IllegalArgumentException("Element type and Table Property and/or other Sort Elements required");
	        
	    ElementType otherType;
	    switch (et) {
	        case Row:
	        case Column:
	            otherType = et == ElementType.Row ? ElementType.Column : ElementType.Row;
	            if (others != null) {
	                for (TableRowColumnElement e : others) {
	                    if (e == null)
	                        throw new NullPointerException("TableRowColumnElement");
                        else if (e.getElementType() != otherType)
                            throw new InvalidException("All elements must be of the same type: " + otherType);
                        else if (!(e instanceof TableSliceElementImpl))
                            throw new UnsupportedImplementationException(e);
                        else if (e.getTable() != this)
                            throw new InvalidParentException(e, this);
	                    
	                    ((TableSliceElementImpl)e).vetElement();
	                }
	            }
	            break;
	            
	        default:
	            throw new InvalidException("Can only sort Rows or Columns");
	    }
	    
	    TablePropertySorter tpSorter = new TablePropertySorter(tp, others);
	    if (et == ElementType.Row) {
            Collections.sort(m_rows, tpSorter);
            reindex(m_rows);
	    }
	    else {
	        Collections.sort(m_cols, tpSorter);
	        reindex(m_cols);
	    }
	}
	
	private void reindex(ArrayList<? extends TableSliceElementImpl> slices) 
	{
		if (slices != null) {
		    int idx = 1;
			for (TableSliceElementImpl r : slices) {
				if (r != null) r.setIndex(idx);
				idx++;
			}
		}
	}

    @Override
    synchronized public Iterable<Row> rows()
    {
        vetElement();
        ensureRowsExist();
        return new BaseElementIterable<Row>(getRowsInternal());
    }
    
    @Override
    synchronized public List<Row> getRows()
    {
        vetElement();
        ensureRowsExist();
        return Collections.unmodifiableList(new ArrayList<Row>(getRowsInternal()));
    }
    
    /**
     * Return the raw rows arraylist. Allows Row class to insert a row into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;RowImpl&gt;
     */
    protected ArrayList<RowImpl> getRowsInternal()
    {
        return m_rows;
    }

    @Override
    synchronized public Iterable<Column> columns()
    {
        vetElement();
        ensureColumnsExist();
        return new BaseElementIterable<Column>(getColumnsInternal());
    }
    
	@Override
	synchronized public Iterable<Subset> subsets()
    {
	    vetElement();
        return new BaseElementIterable<Subset>(m_subsets);
    }
    
	@Override
	synchronized public Iterable<Cell> cells()
	{
	    vetElement();
	    return new TableCellIterable();
	}
	
	protected class TableCellIterable implements Iterator<Cell>, Iterable<Cell>
	{
        private TableImpl m_table;
        private int m_rowIndex;
        private int m_colIndex;
        private int m_numRows;
        private int m_numCols;
        
        private List<RowImpl> m_rows;
        private List<ColumnImpl> m_cols;

        public TableCellIterable()
        {
            m_table = TableImpl.this;
            m_rowIndex = m_colIndex = 1;
            
            m_numRows = m_table.getNumRows();
            m_numCols = m_table.getNumColumns();
            
            m_table.ensureRowsExist();
            m_rows = new ArrayList<RowImpl>(m_table.getRowsInternal());
            
            m_table.ensureColumnsExist();
            m_cols = new ArrayList<ColumnImpl>(m_table.getColumnsInternal());
        }
        
        @Override
        public Iterator<Cell> iterator()
        {
            vetElement(m_table);
            return this;
        }

        @Override
        public boolean hasNext()
        {
            vetElement(m_table);
            return m_rowIndex <= m_numRows && m_colIndex <= m_numCols;
        }

        @Override
        public Cell next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            ColumnImpl col = m_cols.get(m_colIndex - 1);
            RowImpl row = m_rows.get(m_rowIndex - 1); 
            
            Cell c = col.getCell(row, false);
            
            // Iterate over cells one column at a time; once
            // all rows are visited, reset row index and
            // increment column index
            if (++m_rowIndex > m_numRows) {
                m_rowIndex = 1;
                m_colIndex++;
            }            
            
            // return the target cell
            return c;
        }	    
	}
    
    protected static class TablePropertySorter implements Comparator<TableSliceElementImpl>
    {
        private TableProperty m_tp;
        private String m_tpStr;
        private TableRowColumnElement[] m_others;
        
        protected TablePropertySorter(TableProperty tp, TableRowColumnElement... others) 
        {
            m_tp = tp;
            m_others = others;
        }

        protected TablePropertySorter(String tp, TableRowColumnElement... others) 
        {
            m_tpStr = tp != null ? tp.trim() : null;
            m_others = others;
        }

        @Override
        public int compare(TableSliceElementImpl tse1, TableSliceElementImpl tse2)
        {
            if (tse1 == tse2) return 0;
            
            // if a property was specified, sort on it first
            if (m_tp != null || m_tpStr != null) {
                Object tpv1 = tse1 != null ? m_tp != null ? tse1.getProperty(m_tp) : tse1.getProperty(m_tpStr) : null;
                Object tpv2 = tse2 != null ? m_tp != null ? tse2.getProperty(m_tp) : tse2.getProperty(m_tpStr) : null;
                
                int result = compareValue(tpv1, tpv2);
                if (result != 0)
                    return result;
            }
            
            // compare the cells in the specified slices
            if (m_others != null) {
                for (TableRowColumnElement s : m_others) {
                    CellImpl c1 = tse1 != null ? ((TableSliceElementImpl)s).getCellInternal(tse1) : null;
                    CellImpl c2 = tse2 != null ? ((TableSliceElementImpl)s).getCellInternal(tse2) : null;
                    
                    // if both cells are null, return equal
                    if ((c1 == null || c1.isNull()) && (c2 == null || c2.isNull())) return 0;
                    if (c1 == null || c1.isNull()) return 1;
                    if (c2 == null || c2.isNull()) return -1;
                    
                    int result = compareValue(c1.getCellValue(), c2.getCellValue());
                    if (result != 0)
                        return result;
                }
            }
            
            // if we get here, we have a match!
            return 0;
        }

        @SuppressWarnings("unchecked")
        private int compareValue(Object v1, Object v2)
        {
            if (v1 == v2)
                return 0;
            else if (v2 == null)
                return -1;
            else if (v1 == null)
                return 1;
            
            // nulls are dealt with, now handle values
            if ((v1 instanceof Comparable<?>) && (v1.getClass().isAssignableFrom(v2.getClass())))
                return ((Comparable<Comparable<?>>)v1).compareTo((Comparable<?>)v2);
            
            // if not directly comparable, compare string representations                        
            return v1.toString().compareTo(v2.toString());
        }
    }
    
    protected static class TableSliceElementComparator implements Comparator<TableSliceElementImpl>
    {
        private TableSliceElementImpl m_sortSlice;
        private Comparator<Cell> m_cellSorter;
        
        protected TableSliceElementComparator(TableSliceElementImpl sortSlice) 
        {
            m_sortSlice = sortSlice;
        }

        protected TableSliceElementComparator(TableSliceElementImpl sortSlice, Comparator<Cell> cellSorter) 
        {
            this(sortSlice);
            m_cellSorter = cellSorter;
        }

        @Override
        public int compare(TableSliceElementImpl tse1, TableSliceElementImpl tse2) 
        {
            if (tse1 == tse2) return 0;
            
            // as tables are sparse, we consider a null tse the same as a
            // null cell, in other words, they are not reordered
            CellImpl c1 = tse1 != null ? m_sortSlice.getCellInternal(tse1) : null;
            CellImpl c2 = tse2 != null ? m_sortSlice.getCellInternal(tse2) : null;
            
            // if both cells are null, return equal
            if ((c1 == null || c1.isNull()) && (c2 == null || c2.isNull())) return 0;
            if (c1 == null || c1.isNull()) return 1;
            if (c2 == null || c2.isNull()) return -1;
            
            if (m_cellSorter != null)
                return m_cellSorter.compare(c1, c2);
            else {
                // simple stuff is done; since cells can be of any type,
                // we will impose the rule that numeric cells are "less than"
                // other cells, and other cells will be compared as strings                
                if (c1.isNumericValue() && c2.isNumericValue()) {
                    double d1 = ((Number)c1.getCellValue()).doubleValue();
                    double d2 = ((Number)c2.getCellValue()).doubleValue();
                    
                    if (d1 < d2) return -1;
                    if (d1 > d2) return 1;
                    return 0;
                }
                
                return c1.getCellValue().toString().compareTo(c2.getCellValue().toString());
            }
        }        
    }
	
    /**
     * Class to maintain the current row/column in a given table
     */
    protected static class CellReference 
    {
    	private RowImpl m_row;
    	private ColumnImpl m_col;
    	
    	protected CellReference()
        {
            m_row = null;
            m_col = null;
        }

        protected CellReference(CellReference cr)
        {
            this();
            if (cr != null) {                
                m_row = cr.getCurrentRow();               
                m_col = cr.getCurrentColumn();
            }
        }

        public void setCurrentCellReference(TableImpl tbl) 
    	{
            CellReference cr = tbl.getCurrentCellReference();
            
            cr.setCurrentRow(m_row);
            cr.setCurrentColumn(m_col);
    	}

        public RowImpl getCurrentRow()
    	{
            return m_row;
    	}
    	
        protected void setCurrentRow(RowImpl row) 
        {
            if (row != null) 
                row.vetParent(row);
            
            m_row = row;
        }
        
        public ColumnImpl getCurrentColumn()
        {
            return m_col;
        }
        
        protected void setCurrentColumn(ColumnImpl col)
        {
            if (col != null) 
                col.vetParent(col);
            
            m_col = col;
        }
    }
    
    private class CellStackPurger implements Predicate<CellReference>
    {
        private TableSliceElementImpl m_slice;
        
        public CellStackPurger(TableSliceElementImpl slice)
        {
            m_slice = slice;
        }
        
        @Override
        public boolean test(CellReference t)
        {
            if (t.getCurrentColumn() == m_slice || t.getCurrentRow() == m_slice)
                return true;
            else
                return false;
        }       
    }
}
