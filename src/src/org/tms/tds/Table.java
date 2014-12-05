package org.tms.tds;

import java.util.ArrayList;
import java.util.List;

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
    
    private ArrayList<Cell> m_cells;
    private ArrayList<Row> m_rows;
    private ArrayList<Column> m_cols;
    
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
        
        // set all other arrays/sets/maps to null/JustInTime
        m_cells = null;
        m_ranges = new JustInTimeSet<Range>();
        
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
                
            default:
                return super.getProperty(key);
        }
    }
    
    private Context setContext(Context c)
    {
        if (c == null)
            c = Context.getDefaultContext();
        
        m_context = c.register(this);
        
        return m_context;
    }
    
    void clearContext() 
    {
        if (getContext() != null)
            getContext().unregister(this);
        m_context = null;
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
    
    protected boolean add(Range r)
    {
        vetParent(r);
        boolean processed = m_ranges.add(r);
        if (processed) setDirty(true);
        return processed;
    }
    
    protected boolean remove(Range r)
    {
        vetParent(r);
        boolean processed = m_ranges.remove(r);
        if (processed) markDirty();
        return processed;
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
        return addRow(mode, null);
    }
    
    protected Row addRow(Access mode, Object md)
    {
        return (Row)add(new Row(this), mode, md);
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
    
    protected Column addColumn(Access mode)
    {
        return addColumn(mode, null);
    }
    
    protected Column addColumn(Access mode, Object md)
    {
        return (Column)add(new Column(this), mode, md);
    }
    
    /*
     * general methods that operate on Table Element Slices (rows or columns
     */
    protected void setCurrent(TableElementSlice r)
    {
        if (r instanceof Row)
            m_curRow = (Row)r;
        else if (r instanceof Column)
            m_curCol = (Column)r;
    }

    synchronized protected TableElementSlice add(TableElementSlice r, Access mode, Object md)
    {
        // calculate the index where the row will go
        ElementType sliceType = r.getElementType();
        int idx = calcIndex(sliceType, mode, true, md);
        if (idx <= -1)
            throw new InvalidAccessException(ElementType.Table, sliceType, mode, true, md);
        
        // insert row into data structure at correct index, adding cells as/if required
        if (r.insertSlice(idx, true) != null)
            setCurrent(r);
        
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
        
        if (et == ElementType.Row) {
            numSlices = getNumRows();
            curSlice = getCurrentRow();
        }
        else if (et == ElementType.Column) {
            numSlices = getNumColumns();
            curSlice = getCurrentColumn();
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
                
                idx = curSlice.getIndex();

                // If adding a row, return the current row's index
                if (isAdding)
                  return idx;
                
                // if at the first row, there can be no previous
                else if (idx == 0)
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
                
                return curSlice.getIndex();
                
            case Next:
                // special case for adding to an empty table
                if (isAdding && numSlices == 0)
                    return 0;
                
                // if there is no current row, this can't be done
                if (curSlice == null)
                    return -1;
                
                idx = curSlice.getIndex();
                if (isAdding || idx < numSlices - 1)
                    return idx + 1;
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
                return ((TableElementSlice)md).getIndex();
            }
                
            case ByLabel:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableElement target = find(et, TableProperty.Label, md.toString());
                if (target != null)
                    return target.getIndex();
            }
            
            case ByDescription:
            {
                Object md = mda != null && mda.length > 0 ? mda[0] : null;
                if (isAdding || md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (md == null ? "<null>" : md.toString())));  
                TableElement target = find(et, TableProperty.Description, md.toString());
                if (target != null)
                    return target.getIndex();
            }
            
            case ByProperty:
            {
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (isAdding || key == null || !(key instanceof String) || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", et, mode, (key == null ? "<null>" : key.toString())));  
                TableElement target = find(et, (String)key, value);
                if (target != null)
                    return target.getIndex();
            }
        }
        
        // if we get here, return the default, which indicates an error
        return -1;
    }
    
    protected TableElement find(ElementType et, TableProperty key, String value)
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected TableElement find(ElementType et, String key, Object value)
    {
        // TODO Auto-generated method stub
        return null;
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
    
    /**
     * Empty tables contain no defined (set) cells
     */
    @Override
    protected boolean isEmpty()
    {
        if (m_cells == null)
            return true;
        else
            return false;
    }  
}
