package org.tms.tds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.InvalidParentException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.teq.Derivation;
import org.tms.teq.Token;
import org.tms.util.JustInTimeSet;

public class TableImpl extends TableCellsElementImpl implements Table
{
	public static final Table createTable() 
	{
		return new TableImpl();
	}
	
	public static final Table createTable(int nRows, int nCols) 
	{
		return new TableImpl(nRows, nCols);
	}
	
    public static final Table createTable(TableContext c) 
    {
        return new TableImpl(c);
    }
    
    public static final Table createTable(int nRows, int nCols, TableContext c) 
    {
        return new TableImpl(nRows, nCols, c);
    }
    
    public static final Table createTable(int nRows, int nCols, Table t) 
    {
        return new TableImpl(nRows, nCols, t);
    }
    
    private ArrayList<RowImpl> m_rows;
    private ArrayList<ColumnImpl> m_cols;
    
    private int m_nextCellOffset;
    private Queue<Integer> m_unusedCellOffsets;
    private Map<Integer, RowImpl> m_cellOffsetRowMap;
    
    private Deque<CellReference> m_currentCellStack;
    
    private Map<CellImpl, Set<SubsetImpl>> m_subsetedCells;
    private Map<CellImpl, Derivation> m_derivedCells;
    private Map<CellImpl, Set<Derivable>> m_cellAffects;
    
    private RowImpl m_curRow;
    private ColumnImpl m_curCol;
    
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
    
    protected TableImpl(TableContext c)
    {
        this(ContextImpl.getPropertyInt(null, TableProperty.RowCapacityIncr),
                ContextImpl.getPropertyInt(null, TableProperty.ColumnCapacityIncr),
                c);
   }

    protected TableImpl(int nRows, int nCols)
    {
        this(nRows, nCols, ContextImpl.getDefaultContext());
    }

    protected TableImpl(int nRows, int nCols, TableContext c)
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
        initializeProperties(t);
        
        // allocate base memory for rows and columns
        m_rows = new ArrayList<RowImpl>(m_rowsCapacity);
        m_cols = new ArrayList<ColumnImpl>(m_colsCapacity);
                
        setRowsCapacity(calcRowsCapacity(nRows));
        setColumnsCapacity(calcColumnsCapacity(nCols));
        
        m_curRow = null;
        m_curCol = null;
        
        m_nextCellOffset = 0;
        
        // set all other arrays/sets/maps to null/JustInTime
        m_subsets = new JustInTimeSet<SubsetImpl>();
        m_unusedCellOffsets = new ArrayDeque<Integer>();
        m_currentCellStack = new ArrayDeque<CellReference>();
        m_cellOffsetRowMap = new HashMap<Integer, RowImpl>(getRowsCapacity());
        
        int expectedNoOfDerivedCells = m_rowsCapacity * m_colsCapacity / 5; // assume 20%
        m_derivedCells = new HashMap<CellImpl, Derivation>(expectedNoOfDerivedCells);
        m_cellAffects = new LinkedHashMap<CellImpl, Set<Derivable>>(expectedNoOfDerivedCells);
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, false);
        
        m_subsetedCells = new HashMap<CellImpl, Set<SubsetImpl>>();
        
        // clear dirty flag, as table is empty
        markClean();
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
                        value = Derivation.sf_DEFAULT_PRECISION;
                    setPrecision((int)value);
                    break;

                case isAutoRecalculate:
                    if (!isValidPropertyValueBoolean(value))
                        value = ContextImpl.sf_AUTO_RECALCULATE_DEFAULT;
                    setAutoRecalculate((boolean)value);
                    break;

                default:
                    throw new IllegalStateException("No initialization available for Table Property: " + tp);                       
            }
        }
    }
    
    /*
     * Methods defined by interface Table; mostly adapters
     */
    
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
        
        if (c != null)
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
    synchronized protected void delete(boolean compress)
    {
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
        reclaimColumnSpace();
        reclaimRowSpace();
        
    	this.m_subsets.clear();
    	this.m_affects.clear();
    	this.m_cellAffects.clear();
    	this.m_cellOffsetRowMap.clear();
    	this.m_currentCellStack.clear();
    	this.m_derivedCells.clear();
    	this.m_subsetedCells.clear();
    	this.m_unusedCellOffsets.clear();
    	this.m_cols.clear();
    	this.m_rows.clear();
    	
    	if (getTableContext() != null)
    		getTableContext().deregister(this);
    	
    	invalidate();
    }    
    
    public boolean isAutoRecalculateEnabled()
    {
        return isAutoRecalculate() && !isSet(sf_AUTO_RECALCULATE_DISABLED_FLAG);
    }
    
    public boolean isAutoRecalculate()
    {
        return isSet(sf_AUTO_RECALCULATE_FLAG);
    }
    
    protected void setAutoRecalculate(boolean value)
    {
        set(sf_AUTO_RECALCULATE_FLAG, value);
    }

    public void deactivateAutoRecalculate()
    {
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, true);
    }

    public void activateAutoRecalculate()
    {
        set(sf_AUTO_RECALCULATE_DISABLED_FLAG, false);
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
        vetElement(r);
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
                return (SubsetImpl)find(m_subsets, mode == Access.ByLabel ? TableProperty.Label : TableProperty.Description, md);
                
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
        
        Object md = null;
        Object md2 = null;
        if (mda != null && mda.length > 0) {
            md = mda[0];
            
            if (mda.length > 1)
                md2 = mda[1];
        }
        
        SubsetImpl subset = null;
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
    
    protected double getFreeSpaceThreshold()
    {
        if (m_freeSpaceThreshold < 0)
            m_freeSpaceThreshold = ContextImpl.getPropertyDouble(getTableContext(), TableProperty.FreeSpaceThreshold);

        return m_freeSpaceThreshold;
    }
    
    protected void setFreeSpaceThreshold(double value)
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
    void setRowsCapacity(int capacity)
    {
        if (m_rows != null) {
            m_rows.ensureCapacity(capacity);
            m_rowsCapacity = capacity;
        }
    }
    
    int calcRowsCapacity(int nRows)
    {
        int capacity = getRowCapacityIncr();
        if (nRows > 0) {
            int remainder = nRows % capacity;
            capacity = nRows + (remainder > 0 ? capacity - remainder : 0);
        }
        
        return capacity;
    }
    
    protected int getRowCapacityIncr()
    {
        if (m_rowCapacityIncr <= 0) 
            m_rowCapacityIncr = ContextImpl.getPropertyInt(getTableContext(), TableProperty.RowCapacityIncr);
        
        return m_rowCapacityIncr;
    }

    protected void setRowCapacityIncr(int rowCapacityIncr)
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
        return m_curRow;
    }
    
    protected RowImpl setCurrentRow(RowImpl row)
    {
        vetParent(row);
        RowImpl prevCurrent = getCurrentRow();
        m_curRow = row;
        
        return prevCurrent;
    }  
    
    public RowImpl addRow()
    {
        return addRow(Access.Last);
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
        return getRowInternal(true, mode, mda);
    }
    
    @Override
    public RowImpl getRow()
    {
        return getRow(Access.Current);
    }
    
    private RowImpl getRowInternal(boolean createIfNull, Access mode, Object...mda)
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
        
        if (r != null)
        	r.setCurrent();
        
        return r;
    }
    
    /**
     * As the rows array is created sparse, where individual rows are created 
     * only when accessed, this method is needed to build out all rows when
     * in the case where they are to be iterated over
     */
    void ensureRowsExist()
    {
        pushCurrent();
        try {
            for (int i = 1; i <= this.getNumRows(); i++) {
                RowImpl r = this.getRowInternal(true, Access.ByIndex, i);
                assert r != null;
            }
        }
        finally {        
            popCurrent();
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
    protected int getColumnCapacityIncr()
    {
        if (m_colCapacityIncr <= 0) 
            m_colCapacityIncr = ContextImpl.getPropertyInt(getTableContext(), TableProperty.ColumnCapacityIncr);
        
        return m_colCapacityIncr;
    }

    protected void setColumnCapacityIncr(int colCapacityIncr)
    {
        if (colCapacityIncr <= 0) {
            // force a reset of the row capacity value
            colCapacityIncr = 0;
            getColumnCapacityIncr();
        }
        else
            m_colCapacityIncr = colCapacityIncr;
    }
    
    int calcColumnsCapacity(int nCols)
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

    void setColumnsCapacity(int capacity)
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
        return m_curCol;
    }
    
    synchronized protected ColumnImpl setCurrentColumn(ColumnImpl col)
    {
        vetParent(col);
        ColumnImpl prevCurrent = getCurrentColumn();
        m_curCol = col;
        
        return prevCurrent;
    }  
    
    public ColumnImpl addColumn()
    {
        return addColumn(Access.Last);
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
        return getColumnInternal(true, mode, mda);
    }
        
    @Override
    public ColumnImpl getColumn()
    {
        return getColumn(Access.Current);
    }
    
    private ColumnImpl getColumnInternal(boolean createIfNull, Access mode, Object...mda)
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
        
        if (r != null)
        	r.setCurrent();
        
        return r;
    }

    void ensureColumnsExist()
    {
        pushCurrent();
        try {
            for (int i = 1; i <= this.getNumColumns(); i++) {
                ColumnImpl c = this.getColumnInternal(true, Access.ByIndex, i);
                assert c != null;
            }
        }
        finally {        
            popCurrent();
        }
    }
    
    public int getPrecision()
    {
        if (m_precision == Integer.MAX_VALUE || m_precision == Integer.MIN_VALUE) 
            m_precision = ContextImpl.getPropertyInt(getTableContext(), TableProperty.Precision);
        
        return m_precision;
    }

    public void setPrecision(int precision)
    {
        m_precision = precision;
    }
    
    synchronized protected TableSliceElementImpl add(TableSliceElementImpl r, Access mode, Object... md)
    {
        vetElement();

        // calculate the index where the row will go
        ElementType sliceType = r.getElementType();
        int idx = calcIndex(sliceType, mode, true, md);
        if (idx <= -1)
            throw new InvalidAccessException(ElementType.Table, sliceType, mode, true, md);
        
        // insert row into data structure at correct index
        if (r.insertSlice(idx) != null)
            r.setCurrent();
        
        return r;
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
                TableSliceElementImpl target = (TableSliceElementImpl)find(slices, TableProperty.Label, md);
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
                TableSliceElementImpl target = (TableSliceElementImpl)find(slices, TableProperty.Description, md);
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
                    target = (TableSliceElementImpl)find(slices, (TableProperty)key, value);
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
    
    synchronized protected CellImpl getCell(RowImpl row, ColumnImpl col)
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
        
        row.setCurrent();
        col.setCurrent();
        
        return col.getCell(row);
    }
    
    synchronized protected boolean setCellValue(RowImpl row, ColumnImpl col, Object o) 
    {
        CellImpl cell = getCell(row, col);
        if (cell != null) {
            if (o instanceof Token)
                return cell.setDerivedCellValue((Token)o);
            else
                return cell.setCellValue(o);
        }
        else
        	return false;
    }
    
    synchronized protected Object getCellValue(RowImpl row, ColumnImpl col) 
    {
        CellImpl cell = getCell(row, col);
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
    protected boolean isWriteProtected()
    {
        return isReadOnly() ||
               (getTableContext() != null ? getTableContext().isReadOnly() : false);
    }
    
    @Override
    synchronized public void fill(Object o) 
    {
        vetElement();
        pushCurrent();
        deactivateAutoRecalculate();
        try {
            ColumnImpl c = getColumn(Access.First);
            while (c != null) {
                c.fill(o);
                c = getColumn(Access.Next);
            }
        }
        finally {  
            activateAutoRecalculate();
            popCurrent();
        }
        
        // no need to recalc table, as filling all cells
        // clears all derivations
    }  
    
    @Override
    public void clear() 
    {
        fill(null);
    }  
	
	synchronized public void popCurrent() 
    {
		if (m_currentCellStack != null && !m_currentCellStack.isEmpty()) {
			CellReference cr = m_currentCellStack.pollFirst();
			if (cr != null) {
				setCurrentRow(cr.getRow());
				setCurrentColumn(cr.getColumn());
			}
		}		
	}

	synchronized public void pushCurrent() 
	{
		CellReference cr = new CellReference(getCurrentRow(), getCurrentColumn());
		m_currentCellStack.push(cr);
	}

	synchronized public void purgeCurrentStack(TableSliceElementImpl slice)
    {
        if (m_currentCellStack  != null) 
            m_currentCellStack.removeIf(new CellStackPurger(slice));
    }
    
	@Override
	synchronized public void recalculate()
	{
        vetElement();
        
        pushCurrent();        
        try {
            Derivation.recalculateAffected(this);
        }
        finally {
            popCurrent();
        }
	}
	
    protected Derivation getCellDerivation(CellImpl cell)
    {
        return m_derivedCells.get(cell);
    }
    
    protected Derivation registerDerivedCell(CellImpl cell, Derivation d)
    {
        if (cell != null && d != null)
            return m_derivedCells.put(cell, d);
        else
            return null;
    }
    
    protected Derivation deregisterDerivedCell(CellImpl cell)
    {
        if (cell != null)
            return m_derivedCells.remove(cell);
        else
            return null;
    }    

    protected int getNumDerivedCells()
    {
        return m_derivedCells.size();
    }
    
    @Override
    public List<Derivable> getDerivedElements()
    {
        vetElement();
        Set<Derivable> derived = new LinkedHashSet<Derivable>();
        
        pushCurrent();
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
            popCurrent();
        }       
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
        }
        
        affected.add(d);
    }
    
    protected void deregisterAffects(CellImpl cell, Derivable d)
    {
        assert cell != null : "Cell required";
        assert d != null : "Derivable required";
        
        Set<Derivable> affected = m_cellAffects.get(cell);
        if (affected != null) {
            affected.remove(d);
            
            if (affected.isEmpty())
                m_cellAffects.remove(cell);
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
    ArrayList<RowImpl> getRowsInternal()
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
            
            Cell c = col.getCell(row);
            
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
    
    protected class TableSliceElementComparator implements Comparator<TableSliceElementImpl>
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
	
    private class CellReference 
    {
    	private RowImpl m_row;
    	private ColumnImpl m_col;
    	
    	public CellReference(RowImpl r, ColumnImpl c)
    	{
    		if (r != null && c != null)
    			assert r.getTable() == c.getTable() : "Parent tables must match";
    			
    		m_row = r;
    		m_col = c;
    	}
    	
    	public RowImpl getRow()
    	{
    		return m_row;
    	}
    	
    	public ColumnImpl getColumn()
    	{
    		return m_col;
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
            if (t.getColumn() == m_slice || t.getRow() == m_slice)
                return true;
            else
                return false;
        }       
    }
}
