package org.tms.tds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.IllegalTableStateException;

public class ColumnImpl extends TableSliceElementImpl implements Column
{
    private Object m_cells;
    private Class<? extends Object> m_dataType;
    private int m_cellsCapacity;
    private boolean m_stronglyTyped;
    
    public ColumnImpl(TableImpl parentTable)
    {
        super(ElementType.Column, parentTable);
    }

    /*
     * Field getters and setters
     */
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
        return m_stronglyTyped;
    }

    protected void setStronglyTyped(boolean stronglyTyped)
    {
        if (stronglyTyped && getDataType() == null)
            throw new IllegalTableStateException("DataType Missing"); // can't strongly type a column without a datatype
        
        m_stronglyTyped = stronglyTyped;
    }

    /*
     * Class-specific methods
     */    

    protected CellImpl getCell(RowImpl row)
    {
        return getCellInternal(row, true);
    }
    
    @SuppressWarnings("unchecked")
    CellImpl getCellInternal(RowImpl row, boolean createIfSparse)
    {
        assert row != null : "Row required";
        assert this.getTable() != null: "Table required";
        assert this.getTable() == row.getTable() : "Row not in same table";
        
        CellImpl c = null;
        int numCells = getCellsSize();
        TableImpl table = getTable();
        synchronized(getTable()) {
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
            
            // get a type-safe reference to the cells array
            ArrayList<CellImpl> cells = (ArrayList<CellImpl>)m_cells;
            
            // if offset is equal or greater than numCells, we haven't referenced this
            // cell yet, create it and add it to the array, if createIfSparse is true
            if (cellOffset < numCells) {
                c = cells.get(cellOffset);
                
                if (c == null && createIfSparse) {
                    c = new CellImpl(this, cellOffset);
                    cells.set(cellOffset, c);
                }
            }
            else {
                if (!createIfSparse)
                    return c;
                
                // if cellOffset is equal to or > numCells, this should be a new slot
                // in which case, cellOffset should equal numCells
                assert cellOffset >= numCells;
                
                // make sure sufficient capacity exists
                ensureCellCapacity();
                cells = (ArrayList<CellImpl>)m_cells; // reget the cells array, in case it was null
                
                // if cellOffset is equal to or beyond num cells, add slots to cell array
                while (cellOffset > numCells) {
                	cells.add(null);
                	numCells++;
                }
                
                // at this point, cellOffset should equal numCells
                assert cellOffset == numCells : "cellOffset != numCells";
                
                // create a new cell structure and add it to the array              
                c = new CellImpl(this, cellOffset);
                cells.add(c);
            }
        } // of synchronized
        
        // if the cell is non-null, mark the row and column as in use
        if (c != null) {
        	this.setCurrent();
        	row.setCurrent();
            this.setInUse(true);  
            row.setInUse(true);
        }
        
        return c;
    }
    
    @SuppressWarnings("unchecked")
	int getCellsSize()
    {
        if (m_cells != null) 
        	return ((ArrayList<CellImpl>)m_cells).size();
        else 
            return 0;
    }
    

    @SuppressWarnings("unchecked")
    void ensureCellCapacity()
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
                ((ArrayList<CellImpl>)m_cells).ensureCapacity(reqCapacity);
                m_cellsCapacity = reqCapacity;
            }
        }
    }


	@SuppressWarnings("unchecked")
	void clearCell(int cellOffset) 
	{
		if (m_cells != null) {
			List<CellImpl> cellImpls = (ArrayList<CellImpl>)m_cells;
			if (cellOffset < cellImpls.size()) {
				cellImpls.set(cellOffset, null);
			}
		}
	}
	
    /*
     * Overridden methods
     */
    
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
        ArrayList<ColumnImpl> cols = parent.getColumns();
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
    @SuppressWarnings("unchecked")
    public int getNumCells()
    {
        if (m_cells != null) {
        	int numNonNullCells = 0;
        	for (Object o : (ArrayList<CellImpl>)m_cells)
        		if (o != null) numNonNullCells++;
        	
        	return numNonNullCells;
        }
        else 
            return 0;
    }
    
    @Override
    protected ColumnImpl setCurrent()
    {
        ColumnImpl prevCurrent = null;
        if (getTable() != null) 
            prevCurrent = getTable().setCurrentColumn(this);
        
        return prevCurrent;
    }  
    
    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else
            return this.isEnforceDataType() && getDataType() != null;
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
            ArrayList<ColumnImpl> cols = parent.getColumns();
            assert cols != null;
            
            int idx = getIndex() - 1;
            assert idx >= 0 : "Invalid column index";
            
            TableSliceElementImpl rc = cols.remove(idx);
            assert rc == this : "Removed column mismatch";
            
            // reindex remaining columns
            int nCols = parent.getNumColumns();
            if (idx < nCols)
            	cols.listIterator(idx).forEachRemaining(c -> {if (c != null) c.setIndex(c.getIndex() - 1);});
            
            // sanity check
            nCols = nCols--;
            assert nCols == cols.size() : "Column array size mismatch";
            
            // compact memory, if there are too many free columns
            int capacity = parent.getColumnsCapacity();
            compactIfNeeded(cols, capacity);
            
            parent.setCurrentColumn(null);
    	}   	

    	// Mark the column not in in use
    	setIndex(-1);
    	setInUse(false);
    	
        // help the garbage collector
        this.m_cells = null;
    }

    @Override
    public Iterable<Cell> cells()
    {
        return new ColumnCellIterable();
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
            
            RowImpl row = m_table.getRow(Access.ByIndex, m_index++);
            CellImpl c = m_col.getCell(row);
            return c;
        }       
    }
}
