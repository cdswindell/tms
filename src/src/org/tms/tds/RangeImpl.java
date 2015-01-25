package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Range;
import org.tms.api.Row;
import org.tms.api.TableCellsElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.util.JustInTimeSet;

public class RangeImpl extends TableCellsElementImpl implements Range
{
    private Set<RowImpl> m_rows;
    private Set<ColumnImpl> m_cols;
    private Set<RangeImpl> m_ranges;
    private Set<CellImpl> m_cells;
    private int m_numCells = Integer.MIN_VALUE;
    
    protected RangeImpl(TableImpl parentTable)
    {
        super(ElementType.Range, parentTable);
        
        // associate the group with the table
        if (parentTable != null)
            parentTable.add(this);
    }

    protected RangeImpl(TableImpl parentTable, String label)
    {
        this(parentTable);
        
        if (label != null && (label = label.trim()).length() > 0)
            this.setLabel(label);
    }

    /*
     * Field Getters/Setters
     */
    protected List<RowImpl> getRows()
    {
        return new ArrayList<RowImpl>(((JustInTimeSet<RowImpl>)m_rows).clone());
    }
    
    public Iterable<RowImpl> rows()
    {
        return new BaseElementIterable<RowImpl>(m_rows.isEmpty() ? getTable().getRowsInternal() : getRows());
    }
    
    protected int getNumRows()
    {
    	return m_rows.size();
    }

    protected List<ColumnImpl> getColumns()
    {
        return new ArrayList<ColumnImpl>(((JustInTimeSet<ColumnImpl>)m_cols).clone());
    }

    public Iterable<ColumnImpl> columns()
    {
        return new BaseElementIterable<ColumnImpl>(m_cols.isEmpty() ? getTable().getColumnsInternal() : getColumns());
    }
    
    protected int getNumColumns()
    {
        return m_cols.size();
    }

    @Override
    public List<Range> getRanges()
    {
        return new ArrayList<Range>(((JustInTimeSet<RangeImpl>)m_ranges).clone());
    }

    @Override
    public Iterable<Range> ranges()
    {
        return new BaseElementIterable<Range>(m_ranges);
    }
    
    protected int getNumRanges()
    {
        return m_ranges.size();
    }

    /*
     * Class-specific methods
     */
    @Override
    public boolean contains(TableCellsElement r)
    {
        if (r != null) {
            if (r instanceof RowImpl)
                return m_rows.contains(r);
            else if (r instanceof ColumnImpl)
                return m_cols.contains(r);
            else if (r instanceof RangeImpl)
                return m_ranges.contains(r);
            else if (r instanceof CellImpl)
                return m_cells.contains(r);
            else
                throw new UnimplementedException((TableCellsElementImpl)r, "contains");
        }
        else
            return false;
    }
    
    protected boolean addAll(Collection<? extends TableCellsElement> elems)
    {
        boolean addedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableCellsElement e : elems) 
            {
                if (this.add(e))
                    addedAny = true;
            }
        }
        
        return addedAny;
    }
    
    protected boolean removeAll(Collection<? extends TableCellsElement> elems)
    {
        boolean removedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableCellsElement e : elems) 
            {
                if (this.remove(e))
                    removedAny = true;
            }
        }
        
        return removedAny;
    }
        
    protected boolean containsAll(Collection<? extends TableCellsElement> elems)
    {
        if (elems != null && !elems.isEmpty()) {            
            // iterate over all elements
            for (TableCellsElement e : elems) 
            {
                if (!this.contains(e))
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
        
    public boolean remove(TableCellsElement... tableCellElements)
    {
        boolean removedAny = false;
        if (tableCellElements != null) {
            for (TableCellsElement tce : tableCellElements) {               
                if (tce instanceof RowImpl)
                    removedAny = m_rows.remove((RowImpl)tce) ? true : removedAny;
                else if (tce instanceof ColumnImpl)
                    removedAny = m_cols.remove((ColumnImpl)tce) ? true : removedAny;
                else if (tce instanceof RangeImpl)
                    removedAny = m_ranges.remove((RangeImpl)tce) ? true : removedAny;
                else if (tce instanceof CellImpl)
                    removedAny = m_cells.remove((CellImpl)tce) ? true : removedAny;
                
                // remove the range from the corresponding object
                ((TableCellsElementImpl)tce).remove(this);
            }
        }
        
        if (removedAny)
            m_numCells = Integer.MIN_VALUE;
        
        return removedAny;
    }     

    @Override
    public boolean add(TableCellsElement... tableCellElements)
    {
        assert tableCellElements != null : "TableCellElements required";
        
        boolean addedAny = false;
        for (TableCellsElement tce : tableCellElements) {
            if (tce instanceof RowImpl)
                addedAny = m_rows.add((RowImpl)tce) ? true : addedAny;
            else if (tce instanceof ColumnImpl)
                addedAny = m_cols.add((ColumnImpl)tce) ? true : addedAny;
            else if (tce instanceof RangeImpl)
                addedAny = m_ranges.add((RangeImpl)tce) ? true : addedAny;
            else if (tce instanceof CellImpl)
                addedAny = m_cells.add((CellImpl)tce) ? true : addedAny;
            
            // remove the range from the corresponding object
            ((TableCellsElementImpl)tce).add(this);
        }
               
        if (addedAny)
            m_numCells = Integer.MIN_VALUE;
        
        return addedAny;
    }   

    @Override
    protected boolean add(RangeImpl r)
    {
        if (r != null) {
            /*
             *  if the range doesn't contain the range, use the range method to do all the work
             *  TableSliceElementImpl.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(new TableCellsElement[] {this});
            
            return m_ranges.add(r);
        }
        
        return false;
    }

    @Override
    protected boolean remove(RangeImpl r)
    {
        if (r != null) {
            /*
             * if the range contains the element, use the range method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
            if (r.contains(this))
                r.remove(new TableCellsElement[] {this});
            
            return m_ranges.remove(r);
        }
        
        return false;
    }
    
    /*
     * Overridden Methods
     */
    
    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else
            return isEnforceDataType();
    }

    @Override 
    public void delete()
    {
        // delete the range from its parent table
        if (getTable() != null) getTable().remove(this);  
        
    	// Remove the range from its component rows and columns
    	m_rows.forEach(r -> {if (r != null) r.remove(this);});
        m_cols.forEach(c -> {if (c != null) c.remove(this);});
        m_ranges.forEach(r -> {if (r != null) r.remove(this);});
        m_cells.forEach(c -> {if (c != null) c.remove(this);});
    	
        m_rows.clear();
        m_cols.clear();
        m_ranges.clear();
        m_cells.clear();
        
        m_numCells = Integer.MIN_VALUE;
    }
    
   @Override
    public boolean isNull()
    {
        boolean hasRows = (m_rows != null && !m_rows.isEmpty());
        boolean hasCols = (m_cols != null && !m_cols.isEmpty());
        boolean hasRanges = (m_ranges != null && !m_ranges.isEmpty());
        boolean hasCells = (m_cells != null && !m_cells.isEmpty());
        
        return !(hasRows || hasCols || hasRanges || hasCells);
    }

    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numRows:
                return getNumRows();
                
            case numColumns:
                return getNumColumns();
                
            case numRanges:
                return getNumRanges();
                
            case Rows:
                return getRows(); 
                
            case Columns:
                return getColumns(); 
                
            case Ranges:
                return getRanges(); 
                
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
                    throw new IllegalStateException("No initialization available for Range Property: " + tp);                       
            }
        }
        
        m_rows = new JustInTimeSet<RowImpl>();
        m_cols = new JustInTimeSet<ColumnImpl>();
        m_ranges = new JustInTimeSet<RangeImpl>();
        m_cells = new JustInTimeSet<CellImpl>();
    }
        
    @Override
    public int getNumCells()
    {
        if (m_numCells != Integer.MIN_VALUE)
            return m_numCells;
        
        int numCols = m_cols.size();
        if (numCols == 0)
            numCols = getTable() == null ? 0 : getTable().getNumColumns();
        
        int numRows = m_rows.size();
        if (numRows == 0)
            numRows = getTable() == null ? 0 : getTable().getNumRows();
        
        int numCells = numRows * numCols;
    	
    	for (RangeImpl range : m_ranges) 
    	    numCells += range.getNumCells();
    	
    	m_numCells = numCells += m_cells.size();
        return numCells;
    }

	@Override
	public void fill(Object o) 
	{
		if (!isNull()) {
		    TableImpl tbl = getTable();
		    if (tbl == null)
		        throw new IllegalTableStateException("Invalid Range: Table Reqired");
		    
		    clearComponentDerivations();
		    
		    tbl.pushCurrent();
		    try {
		        for (Cell cell : cells()) {
		            if (isDerived(cell))
		                continue;
		            
		            ((CellImpl)cell).setCellValue(o, true);
		        }		        
		    }
		    finally {	            
	            tbl.popCurrent();
		    }
		    
		    tbl.recalculateAffected(this);
		}
	}
	
	/*
	 * If the range contains only-rows and/or only columns, clear
	 * any derivations in those elements
	 */
	private void clearComponentDerivations()
    {
	    int numRows = getNumRows();
	    int numCols = getNumColumns();
	    
        if (numRows > 0 && numCols == 0) {
            for (Row row : rows()) {
                row.clearDerivation();
            }
        }
        else if (numCols > 0 && numRows == 0) {
            for (Column col : columns()) {
                col.clearDerivation();
            }
        }       
    }

    private boolean isDerived(Cell cell)
    {
        if (cell.isDerived())
            return true;
        
        Row row = cell.getRow();
        if (row != null && row.isDerived())
            return true;
        
        Column col = cell.getColumn();
        if (col != null && col.isDerived())
            return true;
        
        return false;
    }

    @Override
	public void clear()
	{
	    fill(null);
	}
	
    @Override
    public Iterable<Cell> cells()
    {
        return new RangeCellIterable();
    }
    
    protected class RangeCellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private RangeImpl m_range;
        private TableImpl m_table;
        private int m_rowIndex;
        private int m_colIndex;
        private int m_rangeIndex;
        private int m_cellIndex;
        private int m_numRows;
        private int m_numCols;
        private int m_numRanges;
        private int m_numCells;
        
        private List<RowImpl> m_rows;
        private List<ColumnImpl> m_cols;
        private List<RangeImpl> m_ranges;
        private List<CellImpl> m_cells;
        
        private Iterator<Cell> m_rangeIterator;

        public RangeCellIterable()
        {
            m_range = RangeImpl.this;
            m_table = m_range.getTable();
            
            m_rowIndex = m_colIndex = m_rangeIndex = m_cellIndex = 1;
            m_rangeIterator = null;
            
            if (m_range.getNumRows() > 0)
                m_rows = m_range.getRows();
            else {
                m_table.ensureRowsExist();
                m_rows = new ArrayList<RowImpl>(m_table.getRowsInternal());
            }
            
            m_numRows = m_rows.size();
                       
            if (m_range.getNumColumns() > 0)
                m_cols = m_range.getColumns();
            else {
                m_table.ensureColumnsExist();
                m_cols = new ArrayList<ColumnImpl>(m_table.getColumnsInternal());
            }
            
            m_numCols = m_cols.size();
            
            m_numRanges = m_range.m_ranges.size();
            if (m_numRanges > 0) 
                m_ranges = new ArrayList<RangeImpl>(m_range.m_ranges);
            
            m_numCells = m_range.m_cells.size();
            if (m_numCells > 0)
                m_cells = new ArrayList<CellImpl>(m_range.m_cells);
        }

        @Override
        public Iterator<Cell> iterator()
        {
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return m_rowIndex <= m_numRows && m_colIndex <= m_numCols || 
                   (m_rangeIndex <= m_numRanges || (m_rangeIterator != null && m_rangeIterator.hasNext())) || 
                   m_cellIndex <= m_numCells;
        }

        @Override
        public Cell next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            if (m_rowIndex <= m_numRows && m_colIndex <= m_numCols) {
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
            else if ((m_rangeIndex <= m_numRanges) || (m_rangeIterator != null && m_rangeIterator.hasNext())) {
                if (m_rangeIterator != null && m_rangeIterator.hasNext())
                    return m_rangeIterator.next();
                
                RangeImpl range = m_ranges.get(m_rangeIndex++);
                m_rangeIterator = range.cells().iterator();                
            }
            else if (m_cellIndex <= m_numCells) {
                Cell c = m_cells.get(m_cellIndex++);
                return c;
            }
            
            throw new IllegalTableStateException("Range cell supply exhasted");
        }      
    }
}
