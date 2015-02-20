package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.event.TableElementEventType;
import org.tms.api.event.TableElementListener;
import org.tms.api.event.exceptions.BlockedRequestException;
import org.tms.api.exceptions.IllegalTableStateException;

public class ColumnImpl extends TableSliceElementImpl implements Column
{
    private ArrayList<CellImpl> m_cells;
    private Class<? extends Object> m_dataType;
    private int m_cellsCapacity;
    
    protected ColumnImpl(TableImpl parentTable)
    {
        super(parentTable);
    }

    @Override
    protected void initialize(TableElementImpl e)
    {
        super.initialize(e);
        
        BaseElementImpl source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) 
        {
            Object value = source.getProperty(tp);            
            if (super.initializeProperty(tp, value)) continue;            
            switch (tp) {
                default:
                    throw new IllegalStateException("No initialization available for Column Property: " + tp);                       
            }
        }
        
        setStronglyTyped(false);
        m_cells = null;
        m_cellsCapacity = 0;
        m_dataType = null;
    }

    @Override 
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs )
    {
        List<TableElementListener> tblListeners = super.removeAllListeners(evTs);

        TableImpl t = getTable();
        if (t != null) {
            synchronized(t) {
                t.pushCurrent();
                try {
                    // remove listeners from all cells
                    if (m_cells != null)
                        m_cells.forEach(c -> {if (c!= null) c.removeAllListeners(evTs); });
                }
                finally {
                    t.popCurrent();
                }
            }
        }

        // return listeners on the this table
        return tblListeners;
    }
    
    /**
     * Compress the cells array as the result of row deletions. This method builds a new cells
     * array where the cells are ordered in row order.
     * @param rows
     * @param numRows 
     */
    void reclaimCellSpace(List<RowImpl> rows, int numRows)
    {
        // create a new cells array, ordered the same as the table rows
        if (numRows > 0 && m_cells != null) {            
            int numCells = m_cells.size();
            if (numCells > numRows) {
                ArrayList<CellImpl> cells = new ArrayList<CellImpl>(numRows);
                rows.forEach(r -> { if (r != null && r.getCellOffset() >= 0) cells.add(m_cells.get(r.getCellOffset())); });    
                
                m_cells = cells;
                numCells = m_cells.size();
            }
            else if (m_cellsCapacity > numCells)
                m_cells.trimToSize();
            
            m_cellsCapacity = numCells;
        }
        else {
            m_cells = null;
            m_cellsCapacity = 0;
        }
    }
    
    /*
     * Field getters and setters
     */
    public ElementType getElementType()
    {
        return ElementType.Column;
    }
    
    protected Class<? extends Object> getDataType()
    {
        return m_dataType;
    }
    
    protected void setDataType(Class<? extends Object> dataType)
    {
        m_dataType = dataType;
    }
    
    int getCellsCapacity()
    {
        return m_cellsCapacity;
    }
    
    protected boolean isStronglyTyped()
    {
        return isSet(sf_STRONGLY_TYPED_FLAG);
    }

    protected void setStronglyTyped(boolean stronglyTyped)
    {
        if (stronglyTyped && getDataType() == null)
            throw new IllegalTableStateException("DataType Missing"); // can't strongly type a column without a datatype
        
       set(sf_STRONGLY_TYPED_FLAG, stronglyTyped);
    }

    /*
     * Class-specific methods
     */    

    protected CellImpl getCell(RowImpl row, boolean setCurrent)
    {
        return getCellInternal(row, true, setCurrent);
    }
    
    CellImpl getCellInternal(RowImpl row, boolean createIfSparse, boolean setCurrent)
    {
        assert row != null : "Row required";
        assert this.getTable() != null: "Table required";
        assert this.getTable() == row.getTable() : "Row not in same table";
        
        CellImpl c = null;
        TableImpl table = getTable();
        if (table != null) {
            synchronized(table) {
                int numCells = getCellsSize();
                int cellOffset = row.getCellOffset();
                if (cellOffset < 0) {
                    // if the cell offset is not defined, no cells have been created
                    if (!createIfSparse ) return c;
                    
                    /*
                     *  consult the table for an available cell offset; this value is stored in
                     *  the row structure, and is used as an offset into the column cell array
                     */
                    cellOffset = table.calcNextAvailableCellOffset();
                    assert cellOffset >= 0 : "Invalid cell offset returned";
                    
                    row.setCellOffset(cellOffset);
                } // of assign cell offset to row
                
                // if offset is equal or greater than numCells, we haven't referenced this
                // cell yet, create it and add it to the array, if createIfSparse is true
                if (cellOffset < numCells) {
                    c = m_cells.get(cellOffset);
                    
                    if (c == null && createIfSparse) {
                        c = new CellImpl(this, cellOffset);
                        m_cells.set(cellOffset, c);
                    }
                }
                else {
                    if (!createIfSparse)
                        return c;
                    
                    // if cellOffset is equal to or > numCells, this should be a new slot
                    // in which case, cellOffset should equal numCells
                    assert cellOffset >= numCells;
                    
                    // make sure sufficient capacity exists
                    ensureCellCapacity(); // reget the cells array, in case it was null
                    
                    // if cellOffset is equal to or beyond num cells, add slots to cell array
                    while (cellOffset > numCells) {
                    	m_cells.add(null);
                    	numCells++;
                    }
                    
                    // at this point, cellOffset should equal numCells
                    assert cellOffset == numCells : "cellOffset != numCells";
                    
                    // create a new cell structure and add it to the array              
                    c = new CellImpl(this, cellOffset);
                    m_cells.add(c);
                }
            } // of synchronized
        } // of table not null
        
        // if the cell is non-null, mark the row and column as in use
        if (c != null) {
            if (setCurrent) {
            	this.setCurrent();
            	row.setCurrent();
            }
            
            this.setInUse(true);  
            row.setInUse(true);
        }
        
        return c;
    }
    
    List<CellImpl> ensureCellCapacity()
    {
        TableImpl table = getTable();
        assert table != null : "Parent table required";
        
        // cell capacity is based on the number of rows in a table
        if (table.getNumRows() > 0) {
            int reqCapacity = table.getRowsCapacity();
            if (m_cells == null) {
                m_cells = new ArrayList<CellImpl>(reqCapacity);
                m_cellsCapacity = reqCapacity;
            }
            else if (reqCapacity > m_cellsCapacity) {
                m_cells.ensureCapacity(reqCapacity);
                m_cellsCapacity = reqCapacity;
            }
        }
        
        return (List<CellImpl>) m_cells;
    }

	int getCellsSize()
    {
        if (m_cells != null) 
        	return m_cells.size();
        else 
            return 0;
    }
    

	/**
	 * Invalidate the specified cell in the column cell array. Called by TableImpl.cacheCellOffset
	 * in response to row deletions
	 * @param cellOffset
	 */
	void invalidateCell(int cellOffset) 
	{
		if (m_cells != null) {
			if (cellOffset < m_cells.size()) {
			    CellImpl cell = m_cells.get(cellOffset);
			    if (cell != null)
			        cell.invalidateCell();
			    
			    m_cells.set(cellOffset, null);
			}
		}
	}
	
    /*
     * Overridden methods
     */
    
    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case DataType:
                return getDataType();
                
            case isStronglyTyped:
                return isStronglyTyped();
                
            case numCells:
                return getNumCells();
                
            case numCellsCapacity:
                return getCellsCapacity();
                
            default:
                return super.getProperty(key);
        }
    }
    
    @Override
    protected ColumnImpl insertSlice(int insertAt)
    {
        // sanity check, insertAt must be >= 0 (indexes are 0-based)
        assert insertAt >= 0;
        
        // sanity check, table must exist
        TableImpl parent = getTable();
        assert parent != null;
        
        // sanity check, columns list must exist
        ArrayList<ColumnImpl> cols = parent.getColumnsInternal();
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
    public int getNumCells()
    {
        vetElement();
        if (m_cells != null) {
        	int numNonNullCells = 0;
        	for (Object o : m_cells)
        		if (o != null) numNonNullCells++;
        	
        	return numNonNullCells;
        }
        else 
            return 0;
    }
    
    @Override
    public ColumnImpl setCurrent()
    {
        vetElement();
        ColumnImpl prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentColumn(this);
        
        return prevCurrent;
    }  
    
    @Override
    protected void delete(boolean compress)
    {
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
                // sanity check, columns list must exist
                ArrayList<ColumnImpl> cols = parent.getColumnsInternal();
                if (cols == null)
                    throw new IllegalTableStateException("Parent table requires columns");
                
                int idx = getIndex() - 1;
                if (idx < 0 || idx >= cols.size())
                    throw new IllegalTableStateException("Column offset outside of parent table bounds: " + idx);
            
                // remove element from subsets that contain it
                removeFromAllSubsets();
                
                // clear any derivations
                clearDerivation();
                
                // clear any derivations on elements affected by this row
                if (m_affects != null)
                    (new ArrayList<Derivable>(m_affects)).forEach(d -> d.clearDerivation());
                
                // invalidate column cells
                if (m_cells != null) 
                    m_cells.forEach(c -> { if (c != null) c.invalidateCell(); });
                
                TableSliceElementImpl rc = cols.remove(idx);
                assert rc == this : "Removed column mismatch";
                
                // reindex remaining columns
                int nCols = parent.getNumColumns();
                if (idx < nCols)
                	cols.listIterator(idx).forEachRemaining(c -> {if (c != null) c.setIndex(c.getIndex() - 1);});
                
                // sanity check
                nCols = nCols--;
                assert nCols == cols.size() : "Column array size mismatch";
                
                // clear this element from the current cell stack
                parent.purgeCurrentStack(this);
                
                // if this element is current, clear it
                if (parent.getCurrentColumn() == this)
                    parent.setCurrentColumn(null);
                
                if (compress)
                    parent.reclaimColumnSpace();
    	    }
    	}   	

    	// Mark the column not in in use
    	m_table = null;
    	m_cellsCapacity = 0;
    	setIndex(-1);
    	setInUse(false);
    	
        // help the garbage collector
        this.m_cells = null;
        
        // mark row as deleted
        invalidate(); 
        
        fireEvents(this, TableElementEventType.OnDelete);
    }

    @Override
    public Iterable<Cell> cells()
    {
        return new ColumnCellIterable();
    }
    
    protected Iterable<CellImpl> cellsInternal()
    {
        if (m_cells == null)
            return Collections.emptyList();
        else if (m_cells instanceof List)
            return m_cells;
        else
            return null;
    }
    
    /**
     * Iterator to produce a column's table cells in row order. Rows and
     * cells are created, as needed, if they do not already exist.
     */
    protected class ColumnCellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private int m_index;
        private int m_numRows;
        private ColumnImpl m_col;
        private TableImpl m_table;
        
        public ColumnCellIterable()
        {
            m_col = ColumnImpl.this;
            m_table = m_col.getTable();
            m_index = 1;
            m_numRows = m_table != null ? m_table.getNumRows() : 0;
        }

        @Override
        public Iterator<Cell> iterator()
        {
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return m_index <= m_numRows;
        }

        @Override
        public CellImpl next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            RowImpl curRow = m_table.getCurrentRow();
            RowImpl row = m_table.getRow(Access.ByIndex, m_index++);
            CellImpl c = m_col.getCell(row, false);
            if (curRow != null && curRow.isValid())
                curRow.setCurrent();
            return c;
        }       
    }
}
