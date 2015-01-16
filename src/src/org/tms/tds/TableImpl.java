package org.tms.tds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.exceptions.UnsupportedImplementationException;
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
	
	public static final Table createTable(int nRows, int nCols, TableContext c) 
	{
		return new TableImpl(nRows, nCols, c);
	}
	
	public static final Table createTable(int nRows, int nCols, Table t) 
	{
		return new TableImpl(nRows, nCols, t);
	}
	
    private boolean m_dirty;
    
    private ArrayList<RowImpl> m_rows;
    private ArrayList<ColumnImpl> m_cols;
    private int m_nextCellOffset;
    private Queue<Integer> m_unusedCellOffsets;
    private Deque<CellReference> m_currentCellStack;
    private Map<Integer, RowImpl> m_cellOffsetRowMap;
    
    private Set<CellImpl> m_derivedCells;
    
    private RowImpl m_curRow;
    private ColumnImpl m_curCol;
    
    private JustInTimeSet<RangeImpl> m_ranges;
    
    private ContextImpl m_context;
    
    private int m_rowsCapacity;
    private int m_colsCapacity;
    
    //Initialized from context or source table
    private int m_rowCapacityIncr;
    private int m_colCapacityIncr;

    private boolean m_autoRecalculate;
    
    protected TableImpl()
    {
        this(ContextImpl.getPropertyInt(null, TableProperty.RowCapacityIncr),
             ContextImpl.getPropertyInt(null, TableProperty.ColumnCapacityIncr));
    }
    
    protected TableImpl(int nRows, int nCols)
    {
        this(nRows, nCols, ContextImpl.getDefaultContext());
    }

    protected TableImpl(int nRows, int nCols, TableContext c)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(c);        
        
        initialize(nRows, nCols, null);
    }

    protected TableImpl(int nRows, int nCols, Table t)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(t != null ? t.getTableContext() : null);        
        
        if (t != null && !(t instanceof TableImpl))
        	throw new UnsupportedImplementationException(t);
        
        initialize(nRows, nCols, (TableImpl)t);
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
        m_ranges = new JustInTimeSet<RangeImpl>();
        m_unusedCellOffsets = new ArrayDeque<Integer>();
        m_currentCellStack = new ArrayDeque<CellReference>();
        m_cellOffsetRowMap = new HashMap<Integer, RowImpl>(getRowsCapacity());
        m_derivedCells = new LinkedHashSet<CellImpl>(m_rowsCapacity * m_colsCapacity / 4);
        
        // clear dirty flag, as table is empty
        markClean();
    }

    @Override
    protected void initialize(TableElementImpl e) 
    {
        // noop for tables, initialization happens slightly differently
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
    public void setCellValue(Row row, Column col, Object o)
    {
        setCellValue((RowImpl)row, (ColumnImpl)col, o);        
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
                
            case numRanges:
                return m_ranges.size();
                
            case Ranges:
                return getRanges(); 
                
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
        	c.unregister(this);
        
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

    protected List<RangeImpl>getRanges()
    {
        return new ArrayList<RangeImpl>(m_ranges.clone());
    }
    
    @Override
    public void delete()
    {
    	m_ranges.clear();
    	if (m_cols != null)
    		m_cols.clear();
    	
    	if (m_rows != null)
    		m_rows.clear();
    	
    	if (getTableContext() != null)
    		getTableContext().unregister(this);;   	
    }
    
    
    public boolean isAutoRecalculate()
    {
        return m_autoRecalculate;
    }
    
    protected void setAutoRecalculate(boolean value)
    {
        m_autoRecalculate = value;
    }

    @Override
    protected boolean isDataTypeEnforced()
    {
        return this.isEnforceDataType();
    }

    protected boolean add(RangeImpl r)
    {
        vetParent(r);
        boolean processed = m_ranges.add(r);
        if (processed) setDirty(true);
        return processed;
    }
    
    /*
     * Delete TableELement methods
     */
    protected void delete(RangeImpl r)
    {
        vetParent(r);
        boolean processed = m_ranges.remove(r);
        
        if (processed) markDirty();
    }
 
    protected void delete(RowImpl r)
    {
        vetParent(r);
        boolean processed = false;
        
        // TODO: Delete element
        
        if (processed) markDirty();
    }
 
    protected void delete(ColumnImpl c)
    {
        vetParent(c);
        boolean processed = false;
        
        // TODO: Delete element
        
        if (processed) markDirty();
    }
    
    protected boolean isDirty()
    {
        return m_dirty;
    }
    
    void setDirty(boolean dirty)
    {
        m_dirty = dirty;
    }

    void markDirty() { setDirty(true); }
    void markClean() { setDirty(false); };
    
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
    
    protected RowImpl getCurrentRow()
    {
        return m_curRow;
    }
    
    protected RowImpl setCurrentRow(RowImpl row)
    {
        RowImpl prevCurrent = getCurrentRow();
        m_curRow = row;
        
        return prevCurrent;
    }  
    
    /**
     * Return the raw rows arraylist. Allows Row class to insert a row into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;Row&gt;
     */
    ArrayList<RowImpl> getRows()
    {
        return m_rows;
    }

    protected RowImpl addRow(Access mode)
    {
        return addRow(mode, (Object [])null);
    }
    
    public RowImpl addRow(Access mode, Object... mda)
    {
        return (RowImpl)add(new RowImpl(this), mode, mda);
    }
    
    public RowImpl getRow(Access mode, Object...mda)
    {
        return getRowInternal(true, mode, mda);
    }
    
    private RowImpl getRowInternal(boolean createIfNull, Access mode, Object...mda)
    {
        RowImpl r = null;
        
        // calculate row index
        int rowIdx = this.calcIndex(ElementType.Row, mode, false, mda);
        if (rowIdx < 0)
            return r;
        
        // retrieve the row, now that we have a valid reference
        r = getRows().get(rowIdx);
        
        // if the row is null, create it
        if (r == null && createIfNull) {
            r = new RowImpl(this);
            r.setIndex(rowIdx + 1);
            
            // add the row to the Rows array at the correct index
            getRows().set(rowIdx,  r);
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
    synchronized void ensureRowsExist()
    {
        pushCurrent();
        
        for (int i = 1; i <= this.getNumRows(); i++) {
            RowImpl r = this.getRowInternal(true, Access.ByIndex, i);
            assert r != null;
        }
        
        popCurrent();
    }
    
    synchronized void ensureColumnsExist()
    {
        pushCurrent();
        
        for (int i = 1; i <= this.getNumColumns(); i++) {
            ColumnImpl c = this.getColumnInternal(true, Access.ByIndex, i);
            assert c != null;
        }
        
        popCurrent();
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
	void cacheCellOffset(int cellOffset, boolean freeCellsNow) 
	{
		assert cellOffset >= 0 : "Invalid value";
		
		// add the cellOffset value to the queue of available/freed offset values
		// these represent positions in the Column.m_cells array that are available
		// for reuse
		m_unusedCellOffsets.offer(cellOffset);
		m_cellOffsetRowMap.remove(cellOffset);
		
		// if so-requested, free the cells in the component columns array
		for (ColumnImpl c : getColumns()) {
			if (c != null) 
				c.clearCell(cellOffset);
		}
	}

	protected RowImpl getRowByCellOffset(int cellOffset) 
	{
		if (cellOffset >= 0) {
		    RowImpl r = m_cellOffsetRowMap.get(cellOffset);
		    if (r != null)
		        return r;
		    
		    // as a last resort, do a sequential search
			for (RowImpl row : getRows())
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
     * Return the raw rows arraylist. Allows Row class to insert a column into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;Column&gt;
     */
    ArrayList<ColumnImpl> getColumns()
    {
        return m_cols;
    }

    protected ColumnImpl getCurrentColumn()
    {
        return m_curCol;
    }
    
    protected ColumnImpl setCurrentColumn(ColumnImpl col)
    {
        ColumnImpl prevCurrent = getCurrentColumn();
        m_curCol = col;
        
        return prevCurrent;
    }  
    
    protected ColumnImpl addColumn(Access mode)
    {
        return addColumn(mode, (Object [])null);
    }
    
    public ColumnImpl addColumn(Access mode, Object... md)
    {
        return (ColumnImpl)add(new ColumnImpl(this), mode, md);
    }
    
    public ColumnImpl getColumn(Access mode, Object...mda)
    {
        return getColumnInternal(true, mode, mda);
    }
    
    private ColumnImpl getColumnInternal(boolean createIfNull, Access mode, Object...mda)
    {
        ColumnImpl r = null;
        
        // calculate column index
        int colIdx = this.calcIndex(ElementType.Column, mode, false, mda);
        if (colIdx < 0)
            return r;
        
        // retrieve the column, now that we have a valid reference
        r = getColumns().get(colIdx);
        
        // if the column is null, create it
        if (r == null && createIfNull) {
            r = new ColumnImpl(this);
            r.setIndex(colIdx + 1);
            
            // add the column to the Columns array at the correct index
            getColumns().set(colIdx,  r);
        }
        
        if (r != null)
        	r.setCurrent();
        
        return r;
    }

    synchronized protected TableSliceElement add(TableSliceElement r, Access mode, Object... md)
    {
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
        TableSliceElement curSlice = null;
        ArrayList<? extends TableSliceElement> slices;
        
        if (et == ElementType.Row) {
            numSlices = getNumRows();
            curSlice = getCurrentRow();
            slices = getRows();
        }
        else if (et == ElementType.Column) {
            numSlices = getNumColumns();
            curSlice = getCurrentColumn();
            slices = getColumns();
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
                if (isAdding || md == null || !(md instanceof TableSliceElement) || (((TableSliceElement)md).getElementType() != et))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));               
                // indexes are 1-based; element arrays are 0-based
                return ((TableSliceElement)md).getIndex() - 1;
            }
                
            case ByLabel:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableSliceElement target = find(slices, TableProperty.Label, md);
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
                TableSliceElement target = find(slices, TableProperty.Description, md);
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
                TableSliceElement target;
                if (key instanceof TableProperty) 
                    target = find(slices, (TableProperty)key, value);
                else if (key instanceof String) 
                    target = find(slices, (String)key, value);
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
    
    protected TableSliceElement find(ArrayList<? extends TableSliceElement> slices, TableProperty key, Object value)
    {
        assert key != null : "TableProperty required (enum)";
        assert value != null : "Value required";
        
        if (slices != null && value != null) {
            for (TableSliceElement tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value)) {
                    	tes.setCurrent();
                        return tes;
                    }
                }
            }
        }
        
        return null;
    }

    protected TableSliceElement find(ArrayList<? extends TableSliceElement> slices, String key, Object value)
    {
        assert key != null : "TableProperty required (String)";
        assert value != null : "Value required";
        
        if (slices != null && value != null) {
            for (TableSliceElement tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value)) {
                    	tes.setCurrent();
                        return tes;
                    }
                }
            }
        }
        
        return null;
    }

    @Override
    public int getNumCells()
    {
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
        assert row != null : "Row required";
        assert col != null : "Column required";
        assert this == row.getTable() && this == col.getTable(): "Row/Column table mismatch";
        
        row.setCurrent();
        col.setCurrent();
        return col.getCell(row);
    }
    
    protected void setCellValue(RowImpl row, ColumnImpl col, Object o) 
    {
        CellImpl cell = getCell(row, col);
        if (cell != null) {
            if (o instanceof Token)
                cell.setDerivedCellValue((Token)o);
            else
                cell.setCellValue(o);
        }
    }
    
    protected Object getCellValue(RowImpl row, ColumnImpl col) 
    {
        CellImpl cell = getCell(row, col);
        if (cell != null) {
            row.setCurrent();
            col.setCurrent();
            return cell.getCellValue();
        }
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
    public boolean isReadOnly()
    {
        return (getTableContext() != null ? getTableContext().isReadOnly() : false) || super.isReadOnly();
    }
    

    @Override
    public boolean isSupportsNull()
    {
        return (getTableContext() != null ? getTableContext().isSupportsNull() : false) || super.isSupportsNull();
    }
    
    @Override
    public void fill(Object o) 
    {
        deactivateAutoRecalculation();
        pushCurrent();
        ColumnImpl c = getColumn(Access.First);
        while (c != null) {
            c.fill(o);
            c = getColumn(Access.Next);
        }
        
        popCurrent();
        activateAutoRecalculation();
    }  
    
    @Override
    public void clear() 
    {
        pushCurrent();
        ColumnImpl c = getColumn(Access.First);
        while (c != null) {
            c.clear();
            c = getColumn(Access.Next);
        }
        
        popCurrent();
    }  
	
	synchronized public void popCurrent() 
    {
		if (!m_currentCellStack.isEmpty()) {
			CellReference cr = m_currentCellStack.pop();
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

    protected void deactivateAutoRecalculation()
    {
        // TODO Auto-generated method stub
        
    }

    protected void activateAutoRecalculation()
    {
        // TODO Auto-generated method stub
        
    }
    
	public void recalculate()
	{
	    // TODO: handle recalcuation of derived rows and cols
	    for (CellImpl cell : derivedCells())
	        cell.recalculate();
	}
	
    protected void recalculate(CellImpl modifiedCell)
    {
        recalculate();        
    }
    
    protected void registerDerivedCell(CellImpl cell)
    {
        if (cell != null)
            m_derivedCells.add(cell);
    }
    
    protected void deregisterDerivedCell(CellImpl cell)
    {
        if (cell != null)
            m_derivedCells.remove(cell);
    }
    
    protected Iterable<CellImpl> derivedCells()
    {
        return new BaseElementIterableInternal<CellImpl>(m_derivedCells);
    }
    
	public Iterable<Row> rows()
    {
	    ensureRowsExist();
        return new BaseElementIterable<Row>(getRows());
    }
    
	public Iterable<Column> columns()
    {
        ensureColumnsExist();
        return new BaseElementIterable<Column>(getColumns());
    }
    
	public Iterable<RangeImpl> ranges()
    {
        return new BaseElementIterable<RangeImpl>(m_ranges);
    }
    
	@Override
	public Iterable<Cell> cells()
	{
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
            m_rows = new ArrayList<RowImpl>(m_table.getRows());
            
            m_table.ensureColumnsExist();
            m_cols = new ArrayList<ColumnImpl>(m_table.getColumns());
        }
        
        @Override
        public Iterator<Cell> iterator()
        {
            return this;
        }

        @Override
        public boolean hasNext()
        {
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
}
