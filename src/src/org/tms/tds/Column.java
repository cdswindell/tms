package org.tms.tds;

import java.util.ArrayList;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Column extends TableElementSlice
{
    public Column(Table parentTable)
    {
        super(ElementType.Column, parentTable);
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
                    throw new IllegalStateException("No initialization available for Column Property: " + tp);                       
            }
        }
    }

    @Override
    protected Column insertSlice(int insertAt, boolean addCells)
    {
        // sanity check, insertAt must be >= 0 (indexes are 0-based)
        assert insertAt >= 0;
        
        // sanity check, table must exist
        Table parent = getTable();
        assert parent != null;
        
        // sanity check, columns list must exist
        ArrayList<Column> cols = parent.getColumns();
        assert cols != null;
        
        /*
         *  insertAt represents the position in the cols array where this new col will be inserted, 
         *  if this position is beyond the current last col, we need to extend the Cols array
         *  to this position
         */
        int nCols = parent.getNumColumns();
        int capacity = parent.getColumnsCapacity();
        this.setIndex(insertAt + 1);;
        if (insertAt >= nCols) {
            // does capacity exist in the array for this element? If not, create it
            if (insertAt >= capacity) {
                capacity = parent.calcColumnsCapacity(insertAt + 1);
                parent.setColumnsCapacity(capacity);
            }
            
            // fill the array from the last element to the new position with nulls
            for (int i = nCols; i < insertAt; i++) {
                cols.add(null);
            }
            
            // finally, set the target column
            cols.add(this);
        }
        else {
            /*
             * Insert the col at position insertAt, moving all columns from 
             * that position on down by one, and then reindex
             */
            
            // first, handle edge case where capacity == nCols
            if (nCols >= capacity) {
                capacity = parent.calcColumnsCapacity(nCols + 1);
                parent.setColumnsCapacity(capacity);
            }
            
            // insert the new column
            cols.add(insertAt, this);
            
            // reindex from insertAt + 1 to the end of the array use
            // the new-fangled JDK 1.8 lambda functionality to reindex
            cols.listIterator(insertAt + 1).forEachRemaining(e -> {if (e != null) e.setIndex(e.getIndex()+1);});
        }       
        
        // mark this column as current
        setCurrent();
        
        return this;
    }

    @Override
    protected Column setCurrent()
    {
        Column prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentColumn(this);
        
        return prevCurrent;
    }  
}
