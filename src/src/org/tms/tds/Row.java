package org.tms.tds;

import java.util.ArrayList;
import java.util.List;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Row extends TableElementSlice
{
    protected Row(Table parentTable)
    {
        super(ElementType.Row, parentTable);
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
    }

    @Override
    protected Row insertSlice(int insertAt, boolean addCells)
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
        
        // Add cells, if requested
        if (addCells)
            ensureCellCapacity();
        
        return this;
    }

    @Override
    void ensureCellCapacity()
    {
        Table table = getTable();
        assert table != null : "Parent table required";
        
        // iterate over all non-null columns
        List<Column> cols = table.getColumns();
        if (cols != null)
            cols.forEach(c -> { if (c != null) c.ensureCellCapacity();});
    }

    @Override
    protected Row setCurrent()
    {
        Row prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentRow(this);
        
        return prevCurrent;
    }  
}
