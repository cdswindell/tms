package org.tms.tds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.events.BlockedRequestException;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.io.IOOption;
import org.tms.io.RowExportAdapter;
import org.tms.io.TableExportAdapter;
import org.tms.teq.MathUtil;

public class RowImpl extends TableSliceElementImpl implements Row
{
    private int m_cellOffset;
    
    protected RowImpl(TableImpl parentTable)
    {
        super(parentTable);
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
                    if (!tp.isOptional())
                        throw new IllegalStateException("No initialization available for Row Property: " + tp);                       
            }
        }
        
        m_cellOffset = -1;
    }
    
    /*
     * Field getters/setters
     */

    public ElementType getElementType()
    {
        return ElementType.Row;
    }
    
	@Override
	public int getNumSlices() 
	{
		TableImpl t = getTable();
		return t != null ? t.getNumColumns() : 0;
	}

	@Override
	public ElementType getSlicesType() 
	{
		return ElementType.Column;
	}
	
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
    @Override
    public void export(String fileName, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new RowExportAdapter(this, fileName, options);
        writer.export();
    }

    @Override
    public void export(OutputStream out, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new RowExportAdapter(this, out, options);
        writer.export();
    }

    @Override 
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs )
    {
        return removeAllListeners(true, evTs);
    }
    
    protected List<TableElementListener> removeAllListeners(boolean processCells, TableElementEventType... evTs )
    {
        // remove listeners from parent
        List<TableElementListener> tblListeners = super.removeAllListeners(evTs);
        
        // remove any listeners hanging off of this row's cells
        TableImpl t = getTable();
        if (t != null) {
            synchronized(t) {
                t.pushCurrent();
                try {
                    // remove listeners from all rows and columns
                    for (ColumnImpl col : getTable().getColumnsInternal()) {
                        if (col == null) continue;
                        
                        CellImpl c = getCellInternal(col, false, false);
                        if (c != null)
                            c.removeAllListeners(evTs);
                    }                   
                }
                finally {
                    t.popCurrent();
                }
            }
        }
        
        // return listeners on the this row
        return tblListeners;
    }
    
    protected CellImpl getCell(ColumnImpl col, boolean setCurrent)
    {
        vetElement();
        return getCellInternal(col, true, setCurrent);
    }
    
    CellImpl getCellInternal(ColumnImpl col, boolean createIfSparse, boolean setCurrent)
    {
        assert col != null : "Column required";
        assert this.getTable() != null: "Table required";
        assert this.getTable() == col.getTable() : "Column not in same table";
        
        return col.getCellInternal(this, createIfSparse, setCurrent);
    }

	@Override
	protected CellImpl getCellByStringReference(String key) 
	{
        TableImpl t = getTable();
        ColumnImpl col = null;
        
        // if key is a positive numeric value, use it as a column index
        Object o = MathUtil.parseCellValue(key, true);
        if (o != null && o instanceof Integer && ((Integer)o) > 0) {
        	int colIdx = (Integer)o;
        	if (colIdx <= t.getNumColumns())
        		col = t.getColumnInternal(true, false, Access.ByIndex, colIdx);
        	else
            	col = t.addColumn(false, true, false, Access.ByIndex, colIdx);        		
        }
        else // Assume key is a column label
        	col = t.addColumn(true, true, false, Access.ByLabel, key);
        
		return col != null ? getCellInternal(col, true, false) : null;
	}
	
    /*
     * Overridden Methods
     */
    
    @Override 
    public boolean isLabelIndexed()
    {
        if (getTable() != null)
            return getTable().isRowLabelsIndexed();
        else
            return false;
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        vetElement();
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
    protected RowImpl insertSlice(int insertAt)
    {
        vetElement();
        
        // sanity check, insertAt must be >= 0 (indexes are 0-based)
        assert insertAt >= 0 : insertAt;
        
        // sanity check, table must exist
        TableImpl parent = getTable();
        assert parent != null : "Parent Table Null";
        
        // sanity check, rows list must exist
        ArrayList<RowImpl> rows = parent.getRowsInternal();
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
        }
        
        // mark this row as current
        setCurrent();
        
        return this;
    }

    @Override
    public RowImpl setCurrent()
    {
        vetElement();
        
        RowImpl prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentRow(this);
        
        return prevCurrent;
    } 
       
    @Override
    protected void delete(boolean compress)
    {
        if (isInvalid())
            return;
        
        // handle onBeforeDelete processing
        try {
            super.delete(compress); // handle on before delete processing
        }
        catch (BlockedRequestException e) {
            return;
        }        
        
    	// now, remove from the parent table, if it is defined
    	TableImpl parent = getTable();
    	if (parent != null) {
    	    synchronized (parent) {
                // sanity check, rows list must exist
                ArrayList<RowImpl> rows = parent.getRowsInternal();
                if (rows == null)
                    throw new IllegalTableStateException("Parent table requires rows");
                
                // sanity check, row index must be within array bounds
                int idx = getIndex() - 1;
                if (idx < 0 || idx >= rows.size())
                    throw new IllegalTableStateException("Row offset outside of parent table bounds: " + idx);
                
                // remove element from subsets that contain it
                removeFromAllSubsets();
                
                // clear any derivations and time series
                clearDerivation();
                clearTimeSeries();
                
                // clear any derivations on elements affected by this row
                if (m_affects != null) 
                    (new ArrayList<Derivable>(m_affects)).forEach(d -> d.clearDerivation());
                
                TableSliceElementImpl rc = rows.remove(idx);
                assert rc == this : "Removed row mismatch";
                
                // reindex remaining rows
                int nRows = parent.getNumRows();
                if (idx < nRows)
                	rows.listIterator(idx).forEachRemaining(r -> {if (r != null) r.setIndex(r.getIndex() - 1);});
                
                // sanity check
                nRows = nRows--;
                assert nRows == rows.size() : "Rows array size mismatch";
                
                // cache the cell offset so it can be reused, 
                // this also clears any derived cells in the row
                parent.cacheCellOffset(this.getCellOffset(), true);
                
                // clear this element from the current cell stack
                parent.purgeCurrentStack(this);
                
                // if this element is current, clear it
                if (parent.getCurrentRow() == this)
                    parent.setCurrentRow(null);
                
                if (compress)
                    parent.reclaimRowSpace();
    	    }
    	}
    	
    	// Mark the row not in in use
    	setCellOffset(-1);
    	setIndex(-1);
    	setInUse(false);   	
    	
        // mark row as deleted
        invalidate();        
                
        fireEvents(this, TableElementEventType.OnDelete);
    }
    
    @Override
    /**
     * Returns the count of the number of allocated cells that exist for this row
     */
    public int getNumCells()
    {
        vetElement();
        
        // if the offset isn't set, there can be no cells
        int cellOffset = getCellOffset();
        if (cellOffset < 0)
            return 0;
        
        TableImpl parent = getTable();
        assert parent != null : "Parent Table Null";

        int numCells = 0;
        ArrayList<ColumnImpl> cols = parent.getColumnsInternal();
        if (cols != null) {
            for (ColumnImpl c : cols) {
                if (c == null) continue;
                int numColCells = c.getCellsSize();
                if (cellOffset < numColCells) {
                    if (c.getCellInternal(this, false, false) != null)
                        numCells++;
                }
            }
        }
        
        return numCells;
    }
   
    @Override
    public Iterable<Cell> cells()
    {
        vetElement();
        return new RowCellIterable();
    }
    
    /**
     * Iterator to produce a row's table cells in column order. Columns and
     * cells are created, as needed, if they do not already exist.
     */
    protected class RowCellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private int m_index;
        private int m_numCols;
        private RowImpl m_row;
        private TableImpl m_table;
        
        public RowCellIterable()
        {
            m_row = RowImpl.this;
            m_table = m_row.getTable();
            m_index = 1;
            m_numCols = m_table != null ? m_table.getNumColumns() : 0;
        }

        @Override
        public Iterator<Cell> iterator()
        {
            m_index = 1;
            m_numCols = m_table != null ? m_table.getNumColumns() : 0;
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return m_index <= m_numCols;
        }

        @Override
        public CellImpl next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            ColumnImpl col = m_table.getColumnInternal(true, false, Access.ByIndex, m_index++);
            CellImpl c = m_row.getCell(col, false);
            return c;
        }       
    }
}
