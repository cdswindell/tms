package org.tms.tds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.util.JustInTimeSet;

public class Table extends TableElement
{
    private boolean m_dirty;
    
    private ArrayList<Row> m_rows;
    private ArrayList<Column> m_cols;
    private int m_nextCellOffset;
    private Queue<Integer> m_unusedCellOffsets;
    
    private Row m_curRow;
    private Column m_curCol;
    
    private JustInTimeSet<Range> m_ranges;
    
    private Context m_context;
    
    private int m_rowsCapacity;
    private int m_colsCapacity;
    
    //Initialized from context or source table
    private int m_rowCapacityIncr;
    private int m_colCapacityIncr;
    
    public Table(int nRows, int nCols)
    {
        this(nRows, nCols, Context.getDefaultContext());
    }

    protected Table(int nRows, int nCols, Context c)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(c);        
        
        initialize(nRows, nCols, null);
    }

    protected Table(int nRows, int nCols, Table t)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(t != null ? t.getContext() : null);        
        
        initialize(nRows, nCols, t);
    }
    
    private void initialize(int nRows, int nCols, Table t)
    {
        initializeProperties(t);
        
        // allocate base memory for rows and columns
        m_rows = new ArrayList<Row>(m_rowsCapacity);
        m_cols = new ArrayList<Column>(m_colsCapacity);
                
        setRowsCapacity(calcRowsCapacity(nRows));
        setColumnsCapacity(calcColumnsCapacity(nCols));
        
        m_curRow = null;
        m_curCol = null;
        
        m_nextCellOffset = 0;
        
        // set all other arrays/sets/maps to null/JustInTime
        m_ranges = new JustInTimeSet<Range>();
        m_unusedCellOffsets = new ArrayDeque<Integer>();
        
        // clear dirty flag, as table is empty
        markClean();
    }

    @Override
    protected void initialize(TableElement e) 
    {
        // noop for tables, initialization happens slightly differently
    }
    
    protected void initializeProperties(Table e)
    {
        BaseElement source = getInitializationSource(e);
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {
                case RowCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = Context.sf_ROW_CAPACITY_INCR_DEFAULT;
                    setRowCapacityIncr((int)value);
                    break;
                    
                case ColumnCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = Context.sf_COLUMN_CAPACITY_INCR_DEFAULT;
                    setColumnCapacityIncr((int)value);
                    break;

                default:
                    throw new IllegalStateException("No initialization available for Table Property: " + tp);                       
            }
        }
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
            	return this.getNextCellOffset();
                
            default:
                return super.getProperty(key);
        }
    }
    
    private Context setContext(Context c)
    {
        if (c == null)
            c = Context.getDefaultContext();
        
        if (c != null)
        	c.unregister(this);
        
        m_context = c.register(this);
        
        return m_context;
    }

    @Override
    protected Context getContext()
    {
        return m_context;
    }

    @Override
    protected Table getTable()
    {
        return this;
    }

    protected List<Range>getRanges()
    {
        return new ArrayList<Range>(m_ranges.clone());
    }
    
    protected void delete()
    {
    	m_ranges.clear();
    	if (m_cols != null)
    		m_cols.clear();
    	
    	if (m_rows != null)
    		m_rows.clear();
    	
    	if (getContext() != null)
    		getContext().unregister(this);;   	
    }
    
    protected boolean add(Range r)
    {
        vetParent(r);
        boolean processed = m_ranges.add(r);
        if (processed) setDirty(true);
        return processed;
    }
    
    /*
     * Delete TableELement methods
     */
    protected void remove(Range r)
    {
        vetParent(r);
        boolean processed = m_ranges.remove(r);
        
        if (processed) markDirty();
    }
 
    protected void delete(Row r)
    {
        vetParent(r);
        boolean processed = false;
        
        if (processed) markDirty();
    }
 
    protected void delete(Column c)
    {
        vetParent(c);
        boolean processed = false;
        
        if (processed) markDirty();
    }
 
    
    protected boolean isDirty()
    {
        return m_dirty;
    }
    
    void markDirty() { setDirty(true); }
    void markClean() { setDirty(false); };
    
    void setDirty(boolean dirty)
    {
        m_dirty = dirty;
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
            m_rowCapacityIncr = Context.getPropertyInt(getContext(), TableProperty.RowCapacityIncr);
        
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
    
    protected int getNumRows()
    {
        return m_rows == null ? 0 : m_rows.size();
    }
    
    protected Row getCurrentRow()
    {
        return m_curRow;
    }
    
    protected Row setCurrentRow(Row row)
    {
        Row prevCurrent = getCurrentRow();
        m_curRow = row;
        
        return prevCurrent;
    }  
    
    /**
     * Return the raw rows arraylist. Allows Row class to insert a row into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;Row&gt;
     */
    ArrayList<Row> getRows()
    {
        return m_rows;
    }

    protected Row addRow(Access mode)
    {
        return addRow(mode, (Object [])null);
    }
    
    protected Row addRow(Access mode, Object... mda)
    {
        return (Row)add(new Row(this), mode, mda);
    }
    
    protected Row getRow(Access mode, Object...mda)
    {
        return getRowInternal(true, mode, mda);
    }
    
    private Row getRowInternal(boolean createIfNull, Access mode, Object...mda)
    {
        Row r = null;
        
        // calculate row index
        int rowIdx = this.calcIndex(ElementType.Row, mode, false, mda);
        if (rowIdx < 0)
            return r;
        
        // retrieve the row, now that we have a valid reference
        r = getRows().get(rowIdx);
        
        // if the row is null, create it
        if (r == null && createIfNull) {
            r = new Row(this);
            r.setIndex(rowIdx + 1);
            
            // add the row to the Rows array at the correct index
            getRows().set(rowIdx,  r);
        }
        
        if (r != null)
        	r.setCurrent();
        
        return r;
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
		
		// if so-requested, free the cells in the component columns array
		for (Column c : getColumns()) {
			if (c != null) 
				c.clearCell(cellOffset);
		}
	}

    /*
     * Column manipulation methods
     */
    protected int getColumnCapacityIncr()
    {
        if (m_colCapacityIncr <= 0) 
            m_colCapacityIncr = Context.getPropertyInt(getContext(), TableProperty.ColumnCapacityIncr);
        
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

    protected int getNumColumns()
    {
        return m_cols == null ? 0 : m_cols.size();
    }
    
    /**
     * Return the raw rows arraylist. Allows Row class to insert a column into the table.
     * Note: <b>for systems use only!</b>
     * @return ArrayList&lt;Column&gt;
     */
    ArrayList<Column> getColumns()
    {
        return m_cols;
    }

    protected Column getCurrentColumn()
    {
        return m_curCol;
    }
    
    protected Column setCurrentColumn(Column col)
    {
        Column prevCurrent = getCurrentColumn();
        m_curCol = col;
        
        return prevCurrent;
    }  
    
    protected Column addColumn(Access mode)
    {
        return addColumn(mode, null);
    }
    
    protected Column addColumn(Access mode, Object md)
    {
        return (Column)add(new Column(this), mode, md);
    }
    
    protected Column getColumn(Access mode, Object...mda)
    {
        return getColumnInternal(true, mode, mda);
    }
    
    private Column getColumnInternal(boolean createIfNull, Access mode, Object...mda)
    {
        Column r = null;
        
        // calculate column index
        int colIdx = this.calcIndex(ElementType.Column, mode, false, mda);
        if (colIdx < 0)
            return r;
        
        // retrieve the column, now that we have a valid reference
        r = getColumns().get(colIdx);
        
        // if the column is null, create it
        if (r == null && createIfNull) {
            r = new Column(this);
            r.setIndex(colIdx + 1);
            
            // add the column to the Columns array at the correct index
            getColumns().set(colIdx,  r);
        }
        
        if (r != null)
        	r.setCurrent();
        
        return r;
    }

    synchronized protected TableElementSlice add(TableElementSlice r, Access mode, Object... md)
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
        TableElementSlice curSlice = null;
        ArrayList<? extends TableElementSlice> slices;
        
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
                if (isAdding || md == null || !(md instanceof TableElementSlice) || (((TableElementSlice)md).getElementType() != et))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));               
                // indexes are 1-based; element arrays are 0-based
                return ((TableElementSlice)md).getIndex() - 1;
            }
                
            case ByLabel:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableElement target = find(slices, TableProperty.Label, md);
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
                TableElement target = find(slices, TableProperty.Description, md);
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
                TableElement target;
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
    
    protected TableElement find(ArrayList<? extends TableElementSlice> slices, TableProperty key, Object value)
    {
        assert key != null : "TableProperty required (enum)";
        TableElement foundElement = null;
        if (slices != null && value != null) {
            for (TableElementSlice tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value))
                        return tes;
                }
            }
        }
        
        return foundElement;
    }

    protected TableElement find(ArrayList<? extends TableElementSlice> slices, String key, Object value)
    {
        assert key != null : "TableProperty required (String)";
        TableElement foundElement = null;
        if (slices != null && value != null) {
            for (TableElementSlice tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value))
                        return tes;
                }
            }
        }
        
        return foundElement;
    }

    /**
     * Returns an available cell offset value
     * @return
     */
	synchronized int getNextCellOffset() 
	{
		Integer availableOffset = this.m_unusedCellOffsets.poll();
		if (availableOffset != null) {
			assert availableOffset >= 0 : "Invalid Cell Offset Value";
			return availableOffset;
		}
		
		// otherwise, just return the next available offset
		return m_nextCellOffset++;
	}
	
    /**
     * Reindex table elements, useful after a sort or manual manipulation
     * @param et
     */
    @SuppressWarnings("unchecked")
    protected void reindex(ElementType et)
    {
        Object a = null;
        int nElems = 0;
        
        switch(et)
        {
            case Row:
                a = m_rows;
                nElems = getNumRows();
                break;
                
            case Column:
                a = m_cols;
                nElems = getNumColumns();
                break;
                
            default:
                throw new UnimplementedException(et, "reindex not supported");
        }
        
        if (a != null && nElems > 0) {
            for (int i = 0; i < nElems; i++) {
                TableElement e = ((List<TableElement>)a).get(i);
                if (e != null) e.setIndex(i);
            }
        }
    }
     
    @Override
    protected int getNumCells()
    {
        int numCells = 0;
        for (Column c : columnIterable()) {
            if (c != null)
                numCells += c.getNumCells();
        }
        
        return numCells;
    }
    
    protected Cell getCell(Row row, Column col)
    {
        assert row != null : "Row required";
        assert col != null : "Column required";
        assert this == row.getTable() && this == col.getTable(): "Row/Column table mismatch";
        
        return col.getCell(row);
    }
    
    /**
     * Empty tables contain no rows or columns
     */
    @Override
    protected boolean isEmpty()
    {
        return getNumRows() == 0 || getNumColumns() == 0 || getNumCells() == 0;
    }
    
    protected Iterable<Row> rowIterable()
    {
        return new TableIterator<Row>(getRows());
    }
    
    protected Iterable<Column> columnIterable()
    {
        return new TableIterator<Column>(getColumns());
    }
    
    protected Iterable<Range> rangeIterable()
    {
        return new TableIterator<Range>(m_ranges);
    }
    
    protected Iterator<Column> columnIterator()
    {
        return new TableIterator<Column>(getColumns());
    }
    
    private class TableIterator<E extends TableElement> implements Iterator<E>, Iterable<E>
    {
        private Iterator<E> m_iter;
        
        @SuppressWarnings("unchecked")
        public TableIterator(Collection<? extends TableElement> elems)
        {
            if (elems != null)
                m_iter = (Iterator<E>) elems.iterator();
            else
                m_iter = null;
        }

        @Override
        public boolean hasNext()
        {
           if (m_iter != null)
               return m_iter.hasNext();
           else
               return false;
        }

        @Override
        public E next()
        {
            if (m_iter != null)
                return m_iter.next();
            
            return null;
        }

        @Override
        public Iterator<E> iterator()
        {
            return m_iter;
        }        
    }
}
