package org.tms.tds;

import java.util.ArrayList;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Row extends TableElementSlice
{
    private int m_cellOffset;
    
    protected Row(Table parentTable)
    {
        super(ElementType.Row, parentTable);
    }

    /*
     * Field getters/setters
     */

    protected int getCellOffset()
    {
        return m_cellOffset;
    }
    
    void setCellOffset(int offset)
    {
        m_cellOffset = offset;
    }
    
    /*
     * Class-specific methods
     */
    
    protected Cell getCell(Column col)
    {
        return getCellInternal(col, true);
    }
    
    Cell getCellInternal(Column col, boolean createIfSparse)
    {
        assert col != null : "Column required";
        assert this.getTable() != null: "Table required";
        assert this.getTable() == col.getTable() : "Column not in same table";
        
        synchronized(getTable()) {
            return col.getCellInternal(this, createIfSparse);
        }
    }

    /*
     * Overridden Methods
     */
    @Override
    protected Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case CellOffset:
                return getCellOffset();
                
            case numCells:
                return getNumCells();
                
            default:
                return super.getProperty(key);
        }
    }
    
    @Override
    protected void initialize(TableElement e)
    {
        super.initialize(e);
        
        BaseElement source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {
                default:
                    throw new IllegalStateException("No initialization available for Row Property: " + tp);                       
            }
        }
        
        m_cellOffset = -1;
    }
    
    @Override
    protected Row insertSlice(int insertAt)
    {
        // sanity check, insertAt must be >= 0 (indexes are 0-based)
        assert insertAt >= 0 : insertAt;
        
        // sanity check, table must exist
        Table parent = getTable();
        assert parent != null : "Parent Table Null";
        
        // sanity check, rows list must exist
        ArrayList<Row> rows = parent.getRows();
        assert rows != null;
        
        /*
         *  insertAt represents the position in the rows array where this new row will be inserted, 
         *  if this position is beyond the current last row, we need to extend the Rows array
         *  to this position
         */
        int nRows = parent.getNumRows();
        int capacity = parent.getRowsCapacity();
        this.setIndex(insertAt + 1);;
        if (insertAt >= nRows) {
            // does capacity exist in the array for this element? If not, create it
            if (insertAt >= capacity) {
                capacity = parent.calcRowsCapacity(insertAt + 1);
                parent.setRowsCapacity(capacity);
            }
            
            // fill the array from the last element to the new position with nulls
            for (int i = nRows; i < insertAt; i++) {
                rows.add(null);
            }
            
            // finally, set the target row
            rows.add(this);
        }
        else {
            /*
             * Insert the row at position insertAt, moving all rows from 
             * that position on down by one, and then reindex
             */
            
            // first, handle edge case where capacity == nRows
            if (nRows >= capacity) {
                capacity = parent.calcRowsCapacity(nRows + 1);
                parent.setRowsCapacity(capacity);
            }
            
            // insert the new row
            rows.add(insertAt, this);
            
            // reindex from insertAt + 1 to the end of the array
            // use the new-fangled JDK 1.8 lambda functionality to reindex
            rows.listIterator(insertAt + 1).forEachRemaining(e -> {if (e != null) e.setIndex(e.getIndex()+1);});
            
// uncomment the following lines to compile in JDK 1.7 and earlier
//            for (int i = insertAt + 1; i < nRows; i++) {
//                Row r = rows.get(i);
//                if (r != null)
//                    r.setIndex(i + 1);
//            }
        }
        
        // mark this row as current
        setCurrent();
        
        return this;
    }

    @Override
    protected Row setCurrent()
    {
        Row prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentRow(this);
        
        return prevCurrent;
    } 
    
    @Override
    /**
     * Returns the count of the number of allocated cells that exist for this row
     */
    protected int getNumCells()
    {
        // if the offset isn't set, there can be no cells
        int cellOffset = getCellOffset();
        if (cellOffset < 0)
            return 0;
        
        Table parent = getTable();
        assert parent != null : "Parent Table Null";

        int numCells = 0;
        ArrayList<Column> cols = parent.getColumns();
        if (cols != null) {
            for (Column c : cols) {
                if (c == null) continue;
                int numColCells = c.getNumCells();
                if (cellOffset < numColCells) {
                    if (c.getCellInternal(this, false) != null)
                        numCells++;
                }
            }
        }
        
        return numCells;
    }   
}
