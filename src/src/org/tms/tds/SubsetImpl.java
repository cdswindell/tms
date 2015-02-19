package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.event.TableElementEventType;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.InvalidParentException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.tds.TableImpl.CellReference;
import org.tms.teq.Derivation;
import org.tms.util.JustInTimeSet;

public class SubsetImpl extends TableCellsElementImpl implements Subset
{
    private Set<RowImpl> m_rows;
    private Set<ColumnImpl> m_cols;
    private Set<SubsetImpl> m_subsets;
    private Set<CellImpl> m_cells;
    private int m_numCells = Integer.MIN_VALUE;
    
    protected SubsetImpl(TableImpl parentTable)
    {
        super(parentTable);
        
        // associate the group with the table
        if (parentTable != null)
            parentTable.add(this);
    }

    protected SubsetImpl(TableImpl parentTable, String label)
    {
        this(parentTable);
        
        if (label != null && (label = label.trim()).length() > 0)
            this.setLabel(label);
    }

    /*
     * Field Getters/Setters
     */
    public ElementType getElementType()
    {
        return ElementType.Subset;
    }
    
    protected List<RowImpl> getRows()
    {
        return new ArrayList<RowImpl>(((JustInTimeSet<RowImpl>)m_rows).clone());
    }
    
    protected Collection<RowImpl> getEffectiveRows()
    {
        if (this.getNumRows() > 0)
            return m_rows;
        
        TableImpl parent = getTable();
        if (parent != null) {
            if (this.getNumColumns() > 0)
                return parent.getRowsInternal();
        }
        
        return Collections.emptyList();
    }
    
    protected Collection<ColumnImpl> getEffectiveColumns()
    {
        if (this.getNumColumns() > 0)
            return m_cols;
        
        TableImpl parent = getTable();
        if (parent != null) {
            if (this.getNumRows() > 0)
                return parent.getColumnsInternal();
        }
        
        return Collections.emptyList();
    }
    
    protected Iterable<RowImpl> rows()
    {
        return getRows();
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
        return getColumns();
    }
    
    protected int getNumColumns()
    {
        return m_cols.size();
    }

    @Override
    public List<Subset> getSubsets()
    {
        return Collections.unmodifiableList(new ArrayList<Subset>(((JustInTimeSet<SubsetImpl>)m_subsets).clone()));
    }
    
    protected Set<SubsetImpl> getSubsetsInternal()
    {
        return m_subsets;
    }

    @Override
    public int getNumSubsets()
    {
        return m_subsets.size();
    }

    /*
     * Class-specific methods
     */
    @Override
    public boolean contains(TableElement r)
    {
        if (r != null) {
            if (r instanceof RowImpl)
                return m_rows.contains(r);
            else if (r instanceof ColumnImpl)
                return m_cols.contains(r);
            else if (r instanceof SubsetImpl)
                return m_subsets.contains(r);
            else if (r instanceof CellImpl)
                return m_cells.contains(r);
            else
                throw new UnimplementedException((TableImpl)r, "contains");
        }
        else
            return false;
    }
    
    protected boolean addAll(Collection<? extends TableElement> elems)
    {
        boolean addedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableElement e : elems) 
            {
                if (this.add(e))
                    addedAny = true;
            }
        }
        
        return addedAny;
    }
    
    protected boolean removeAll(Collection<? extends TableElement> elems)
    {
        boolean removedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableElement e : elems) 
            {
                if (this.remove(e))
                    removedAny = true;
            }
        }
        
        return removedAny;
    }
        
    protected boolean containsAll(Collection<? extends TableElement> elems)
    {
        if (elems != null && !elems.isEmpty()) {            
            // iterate over all elements
            for (TableElement e : elems) 
            {
                if (!this.contains(e))
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
        
    public boolean remove(TableElement... tableElements)
    {
        boolean removedAny = false;
        if (tableElements != null) {
            for (TableElement tce : tableElements) {               
                if (tce instanceof RowImpl)
                    removedAny = m_rows.remove((RowImpl)tce) ? true : removedAny;
                else if (tce instanceof ColumnImpl)
                    removedAny = m_cols.remove((ColumnImpl)tce) ? true : removedAny;
                else if (tce instanceof SubsetImpl)
                    removedAny = m_subsets.remove((SubsetImpl)tce) ? true : removedAny;
                else if (tce instanceof CellImpl)
                    removedAny = m_cells.remove((CellImpl)tce) ? true : removedAny;
                
                // remove the  from the corresponding object
                if (removedAny)
                    ((TableElementImpl)tce).remove(this);
            }
        }
        
        if (removedAny)
            m_numCells = Integer.MIN_VALUE;
        
        return removedAny;
    }     

    @Override
    public boolean add(TableElement... tableElements)
    {
        vetElement();
        boolean addedAny = false;
        if (tableElements != null) {
            for (TableElement tce : tableElements) {
                if (tce == null) continue;
                
                vetElement((BaseElementImpl)tce);
                
                if (tce.getTable() != this.getTable())
                    throw new InvalidParentException(tce, this);
                
                if (tce instanceof TableCellsElementImpl){
                    if (tce instanceof RowImpl)
                        addedAny = m_rows.add((RowImpl)tce) ? true : addedAny;
                    else if (tce instanceof ColumnImpl)
                        addedAny = m_cols.add((ColumnImpl)tce) ? true : addedAny;
                    else if (tce instanceof SubsetImpl) {
                        if (tce == this)
                            throw new IllegalTableStateException("Cannot add subset to itself");
                        addedAny = m_subsets.add((SubsetImpl)tce) ? true : addedAny;
                    }
                    if (tce instanceof CellImpl)
                        addedAny = m_cells.add((CellImpl)tce) ? true : addedAny;
                    
                    // add the subset from the corresponding object
                    ((TableCellsElementImpl)tce).add(this);
                }
                else if (tce instanceof CellImpl) {
                    addedAny = m_cells.add((CellImpl)tce) ? true : addedAny;
                    ((CellImpl)tce).add(this);
                }
            }
        }
               
        if (addedAny)
            m_numCells = Integer.MIN_VALUE;
        
        return addedAny;
    }   

    @Override
    protected boolean add(SubsetImpl r)
    {
        if (r != null) {
            /*
             *  if the subset doesn't contain the subset, use the subset method to do all the work
             *  TableSliceElementImpl.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(new TableElement[] {this});
            
            return m_subsets.add(r);
        }
        
        return false;
    }

    @Override
    protected boolean remove(SubsetImpl r)
    {
        if (r != null) {
            /*
             * if the subset contains the element, use the subset method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
            if (r.contains(this))
                r.remove(new TableElement[] {this});
            
            return m_subsets.remove(r);
        }
        
        return false;
    }
    
    /*
     * Overridden Methods
     */
    
    @Override 
    protected void delete(boolean compress)
    {
        super.delete(compress); // handle on before delete processing
        
        // delete the subset from its parent table
        if (getTable() != null) getTable().remove(this);  
        
    	// Remove the subset from its component rows and columns
    	m_rows.forEach(r -> {if (r != null) r.remove(this);});
        m_cols.forEach(c -> {if (c != null) c.remove(this);});
        m_subsets.forEach(r -> {if (r != null) r.remove(this);});
        m_cells.forEach(c -> {if (c != null) c.remove(this);});
    	
        // clear out caches
        m_rows.clear();
        m_cols.clear();
        m_subsets.clear();
        m_cells.clear();
        
        m_numCells = Integer.MIN_VALUE;
        
        // Mark the subset as deleted
        invalidate();      
        
        fireEvents(this, TableElementEventType.OnDelete);
    }
    
    /**
     * Call TableElementImpl.delete() when object is cleaned up
     */
    @Override
    public void finalize()
    {
        if (isValid())
            delete();
    }
    
    @Override
    public boolean isNull()
    {
        boolean hasRows = (m_rows != null && !m_rows.isEmpty());
        boolean hasCols = (m_cols != null && !m_cols.isEmpty());
        boolean hasSubsets = (m_subsets != null && !m_subsets.isEmpty());
        boolean hasCells = (m_cells != null && !m_cells.isEmpty());
        
        return !(hasRows || hasCols || hasSubsets || hasCells);
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
                
            case numSubsets:
                return getNumSubsets();
                
            case numCells:
                return getNumCells();
                
            case Rows:
                return getRows(); 
                
            case Columns:
                return getColumns(); 
                
            case Subsets:
                return getSubsets(); 
                
            case Cells:
                return getCells(); 
                
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
                    throw new IllegalStateException("No initialization available for Subset Property: " + tp);                       
            }
        }
        
        m_rows = new JustInTimeSet<RowImpl>();
        m_cols = new JustInTimeSet<ColumnImpl>();
        m_subsets = new JustInTimeSet<SubsetImpl>();
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
    	
    	for (SubsetImpl subset : m_subsets) 
    	    numCells += subset.getNumCells();
    	
    	m_numCells = numCells += m_cells.size();
        return numCells;
    }

    public List<Cell> getCells()
    {
        return new ArrayList<Cell>(((JustInTimeSet<CellImpl>)m_cells).clone());
    }
    
    protected Set<CellImpl> getCellsInternal()
    {
        return m_cells;
    }

	@Override
	public void fill(Object o) 
	{
	    vetElement();
		if (!isNull()) {
		    TableImpl tbl = getTable();
		    if (tbl == null)
		        throw new IllegalTableStateException("Invalid Subset: Table Reqired");
		    
		    clearComponentDerivations();
		    
	        CellReference cr = tbl.getCurrent();
		    tbl.deactivateAutoRecalculate();
		    boolean setSome = false;
		    try {
		        for (Cell cell : cells()) {
		            if (isDerived(cell))
		                continue;
		            
		            if (((CellImpl)cell).setCellValue(o, true, false))
		                setSome = true;
		        }
		        
		        if (setSome)
		            fireEvents(this,TableElementEventType.OnNewValue,  o);
		    }
		    finally {	            	            
	            tbl.activateAutoRecalculate();
	            cr.setCurrentCellReference();
		    }
		    
		    if (tbl.isAutoRecalculateEnabled())
		        Derivation.recalculateAffected(this);
		}
	}
	
    /*
	 * If the subset contains only-rows and/or only columns, clear
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
    public List<Derivable> getDerivedElements()
    {
        vetElement();
        Set<Derivable> derived = new LinkedHashSet<Derivable>();
        
        for (RowImpl row : getEffectiveRows())
            if (row != null && row.isDerived())
                derived.add(row);
        
        for (ColumnImpl col : getEffectiveColumns())
            if (col != null && col.isDerived())
                derived.add(col);
        
        for (SubsetImpl s : m_subsets)
            if (s != null)
                derived.addAll(s.getDerivedElements());
        
        for (CellImpl c : m_cells)
            if (c != null && c.isDerived())
                derived.add(c);
        
        return Collections.unmodifiableList(new ArrayList<Derivable>(derived));
    }
    
    @Override
	public void clear()
	{
	    fill(null);
	}
	
    @Override
    public boolean isEnforceDataType()
    {
        return false;
    }

    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null)
            return getTable().isDataTypeEnforced();
        else
            return isEnforceDataType();
    }

    @Override
    public boolean isSupportsNull()
    {
        return true;
    }

    @Override
    protected boolean isNullsSupported()
    {
        if (getTable() != null)
            return getTable().isNullsSupported();
        else
            return isEnforceDataType();
    }

    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    @Override
    protected boolean isWriteProtected()
    {
        if (getTable() != null)
            return getTable().isWriteProtected();
        else
            return isReadOnly();
    }
    
    @Override
    public Iterable<Cell> cells()
    {
        return new SubsetCellIterable();
    }
    
    protected class SubsetCellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private SubsetImpl m_subset;
        private TableImpl m_table;
        private int m_rowIndex;
        private int m_colIndex;
        private int m_subsetIndex;
        private int m_cellIndex;
        private int m_numRows;
        private int m_numCols;
        private int m_numSubsets;
        private int m_numCells;
        
        private List<RowImpl> m_rows;
        private List<ColumnImpl> m_cols;
        private List<SubsetImpl> m_subsets;
        private List<CellImpl> m_cells;
        
        private Iterator<Cell> m_subsetIterator;

        public SubsetCellIterable()
        {
            m_subset = SubsetImpl.this;
            m_table = m_subset.getTable();
            
            m_rowIndex = m_colIndex = m_subsetIndex = m_cellIndex = 1;
            m_subsetIterator = null;
            
            if (m_subset.getNumRows() > 0 || m_subset.getNumColumns() > 0) {
                if (m_subset.getNumRows() > 0)
                    m_rows = m_subset.getRows();
                else {
                    m_table.ensureRowsExist();
                    m_rows = new ArrayList<RowImpl>(m_table.getRowsInternal());
                }
                
                m_numRows = m_rows.size();
                           
                if (m_subset.getNumColumns() > 0)
                    m_cols = m_subset.getColumns();
                else {
                    m_table.ensureColumnsExist();
                    m_cols = new ArrayList<ColumnImpl>(m_table.getColumnsInternal());
                }
                
                m_numCols = m_cols.size();
            }
            
            m_numSubsets = m_subset.m_subsets.size();
            if (m_numSubsets > 0) 
                m_subsets = new ArrayList<SubsetImpl>(m_subset.m_subsets);
            
            m_numCells = m_subset.m_cells.size();
            if (m_numCells > 0)
                m_cells = new ArrayList<CellImpl>(m_subset.m_cells);
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
                   (m_subsetIndex <= m_numSubsets || (m_subsetIterator != null && m_subsetIterator.hasNext())) || 
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
                
                Cell c = col.getCell(row, false);
                
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
            else if ((m_subsetIndex <= m_numSubsets) || (m_subsetIterator != null && m_subsetIterator.hasNext())) {
                if (m_subsetIterator != null && m_subsetIterator.hasNext())
                    return m_subsetIterator.next();
                
                SubsetImpl subset = m_subsets.get(m_subsetIndex++);
                m_subsetIterator = subset.cells().iterator();                
            }
            else if (m_cellIndex <= m_numCells) {
                Cell c = m_cells.get(m_cellIndex++);
                return c;
            }
            
            throw new IllegalTableStateException("Subset cell supply exhasted");
        }      
    }
}
