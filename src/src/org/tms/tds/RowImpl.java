package org.tms.tds;

import java.util.ArrayList;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.TableProperty;

public class RowImpl extends TableSliceElement implements Row
{
    private int m_cellOffset;
    
    protected RowImpl(TableImpl parentTable)
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
        if (offset >= 0 && getTable() != null)
            getTable().mapCellOffsetToRow(this, offset);
    }
    
    /*
     * Class-specific methods
     */
    
    protected CellImpl getCell(ColumnImpl col)
    {
        return getCellInternal(col, true);
    }
    
    CellImpl getCellInternal(ColumnImpl col, boolean createIfSparse)
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
    public Object getProperty(TableProperty key)
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
    protected void initialize(TableElementImpl e)
    {
        super.initialize(e);
        
        BaseElementImpl source = getInitializationSource(e);        
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
    protected RowImpl insertSlice(int insertAt)
    {
        // sanity check, insertAt must be >= 0 (indexes are 0-based)
        assert insertAt >= 0 : insertAt;
        
        // sanity check, table must exist
        TableImpl parent = getTable();
        assert parent != null : "Parent Table Null";
        
        // sanity check, rows list must exist
        ArrayList<RowImpl> rows = parent.getRows();
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
    protected RowImpl setCurrent()
    {
        RowImpl prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentRow(this);
        
        return prevCurrent;
    } 
       
    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else
            return this.isEnforceDataType();
    }

    @Override
    public void delete()
    {
    	// remove element from ranges that contain it
    	removeFromAllRanges();
    	
    	// now, remove from the parent table, if it is defined
    	TableImpl parent = getTable();
    	if (parent != null) {
            // sanity check, columns list must exist
            ArrayList<RowImpl> rows = parent.getRows();
            assert rows != null;
            
            int idx = getIndex() - 1;
            assert idx >= 0 : "Invalid row index";
            
            TableSliceElement rc = rows.remove(idx);
            assert rc == this : "Removed row mismatch";
            
            // reindex remaining rows
            int nRows = parent.getNumRows();
            if (idx < nRows)
            	rows.listIterator(idx).forEachRemaining(c -> {if (c != null) c.setIndex(c.getIndex() - 1);});
            
            // sanity check
            nRows = nRows--;
            assert nRows == rows.size() : "Rows array size mismatch";
            
            // compact memory, if there are too many free Rows
            int capacity = parent.getColumnsCapacity();
            compactIfNeeded(rows, capacity);    
            
            // cache the cell offset so it can be reused, 
            parent.cacheCellOffset(this.getCellOffset(), true);
            
            parent.setCurrentRow(null);
    	}
    	
    	// Mark the column not in in use
    	setCellOffset(-1);
    	setIndex(-1);
    	setInUse(false);   	
    }
    
    @Override
    /**
     * Returns the count of the number of allocated cells that exist for this row
     */
    public int getNumCells()
    {
        // if the offset isn't set, there can be no cells
        int cellOffset = getCellOffset();
        if (cellOffset < 0)
            return 0;
        
        TableImpl parent = getTable();
        assert parent != null : "Parent Table Null";

        int numCells = 0;
        ArrayList<ColumnImpl> cols = parent.getColumns();
        if (cols != null) {
            for (ColumnImpl c : cols) {
                if (c == null) continue;
                int numColCells = c.getCellsSize();
                if (cellOffset < numColCells) {
                    if (c.getCellInternal(this, false) != null)
                        numCells++;
                }
            }
        }
        
        return numCells;
    }
    
	@Override
	public void fill(Object o) 
	{
		TableImpl parent = getTable();
		assert parent != null : "Parent table required";
		
		pushCurrent();
		ColumnImpl col = parent.getColumn(Access.First);
		if (col != null) {
			while(col != null) {
				CellImpl c = getCell(col);
				c.setCellValue(o);
				col.setInUse(true);
				col = parent.getColumn(Access.Next);
			}
			
			this.setInUse(true);			
		}
		
		popCurrent();
	}
}
