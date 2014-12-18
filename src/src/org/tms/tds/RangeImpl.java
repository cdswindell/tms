package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.util.JustInTimeSet;

public class RangeImpl extends TableCellsElementImpl
{
    private Set<RowImpl> m_rows;
    private Set<ColumnImpl> m_cols;
    
    protected RangeImpl(TableImpl parentTable)
    {
        super(ElementType.Range, parentTable);
        
        // associate the group with the table
        if (parentTable != null)
            parentTable.add(this);
    }

    /*
     * Field Getters/Setters
     */
    protected List<RowImpl> getRows()
    {
        return new ArrayList<RowImpl>(((JustInTimeSet<RowImpl>)m_rows).clone());
    }
    
    protected int getNumRows()
    {
    	return m_rows.size();
    }

    protected List<ColumnImpl> getColumns()
    {
        return new ArrayList<ColumnImpl>(((JustInTimeSet<ColumnImpl>)m_cols).clone());
    }

    protected int getNumColumns()
    {
    	return m_cols.size();
    }

    /*
     * Class-specific methods
     */
    protected boolean contains(TableSliceElement r)
    {
        if (r != null) {
            if (r instanceof RowImpl)
                return m_rows.contains(r);
            else if (r instanceof ColumnImpl)
                return m_cols.contains(r);
            else
                throw new UnimplementedException(r, "contains");
        }
        else
            return false;
    }
    
    boolean add(TableSliceElement tes)
    {
        if (tes != null) {
            if (tes instanceof RowImpl)
                return add(new RowImpl[] {(RowImpl)tes});
            else if (tes instanceof ColumnImpl)
                return add(new ColumnImpl[] {(ColumnImpl)tes});
            else
                throw new UnimplementedException(tes, "add");
        }
        else
            return false;
    }
    
    boolean remove(TableSliceElement tes)
    {
        if (tes != null) {
            if (tes instanceof RowImpl)
                return remove(new RowImpl[] {(RowImpl)tes});
            else if (tes instanceof ColumnImpl)
                return remove(new ColumnImpl[] {(ColumnImpl)tes});
            else
                throw new UnimplementedException(tes, "remove");
        }
        else
            return false;
    }
    
    protected boolean addAll(Collection<? extends TableSliceElement> elems)
    {
        boolean addedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableSliceElement e : elems) 
            {
                if (this.add(e))
                    addedAny = true;
            }
        }
        
        return addedAny;
    }
    
    protected boolean removeAll(Collection<? extends TableSliceElement> elems)
    {
        boolean removedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableSliceElement e : elems) 
            {
                if (this.remove(e))
                    removedAny = true;
            }
        }
        
        return removedAny;
    }
        
    protected boolean containsAll(Collection<? extends TableSliceElement> elems)
    {
        if (elems != null && !elems.isEmpty()) {            
            // iterate over all elements
            for (TableSliceElement e : elems) 
            {
                if (!this.contains(e))
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
        
    protected boolean add(RowImpl... rows)
    {
        boolean addedAny = false;
        if (rows != null) {
            // make sure all of the rows belong to the same table
            vetParent(rows);
            
            // iterate over all rows, adding them to the group
            for (RowImpl r : rows) 
            {
                if (m_rows.add(r))
                    addedAny = true;
                
                // add the group to the corresponding row
                r.add(this);
            }
        }
        
        return addedAny;
    }       

    protected boolean remove(RowImpl... rows)
    {
        boolean removedAny = false;
        if (rows != null) {
            // iterate over all rows, removing them from the group
            for (RowImpl r : rows) 
            {
                if (m_rows.remove(r))
                    removedAny = true;
                
                // remove the group to the corresponding row
                r.remove(this);
            }
        }
        
        return removedAny;
    } 
    
    protected boolean add(ColumnImpl... cols)
    {
        boolean addedAny = false;
        if (cols != null) {
            // make sure all of the columns belong to the same table
            vetParent(cols);

            // iterate over all rows, adding them to the group
            for (ColumnImpl c : cols) 
            {
                if (m_cols.add(c))
                    addedAny = true;

                // add the group to the corresponding row
                c.add(this);
            }
        }

        return addedAny;
    }
    
    protected boolean remove(ColumnImpl... cols)
    {
        boolean removedAny = false;
        if (cols != null) {
            // iterate over all rows, removing them from the group
            for (ColumnImpl c : cols) 
            {
                if (m_cols.remove(c))
                    removedAny = true;
                
                // remove the group to the corresponding row
                c.remove(this);
            }
        }
        
        return removedAny;
    } 
    
    protected Iterable<RowImpl> rowIterable()
    {
        return new BaseElementIterable<RowImpl>(getRows());
    }
    
    protected Iterable<ColumnImpl> columnIterable()
    {
        return new BaseElementIterable<ColumnImpl>(getColumns());
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
    	// Remove the range from its component rows and columns
    	m_rows.forEach(r -> {if (r != null) r.remove(this);});
    	m_cols.forEach(c -> {if (c != null) c.remove(this);});
    	
    	m_rows.clear();
    	m_cols.clear();
    	
    	// remove the range from its parent table
    	if (getTable() != null) getTable().remove(this);	
    }
    
    @Override
    protected boolean isEmpty()
    {
        boolean hasRows = (m_rows != null && !m_rows.isEmpty());
        boolean hasCols = (m_cols != null && !m_cols.isEmpty());
        
        return !hasRows  && !hasCols;
    }

    @Override
    protected Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numRows:
                return getNumRows();
                
            case numColumns:
                return getNumColumns();
                
            case Rows:
                return getRows(); 
                
            case Columns:
                return getColumns(); 
                
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
    }
        
    @Override
    public int getNumCells()
    {
    	int numCells = 0;
    	for (ColumnImpl c : m_cols) {
    		if (c != null) 
    			numCells += c.getNumCells();
    	}
    	
        return numCells;
    }

	@Override
	public void fill(Object o) 
	{
		if (!isEmpty()) {
			if (getNumRows() == 0 && m_cols != null) 
				m_cols.forEach(c->c.fill(o)); // if group is just columns, fill them
			else if (getNumColumns() == 0 && m_rows != null) 
				m_rows.forEach(r->r.fill(o)); // if group is just rows, fill them
			else {
				for (ColumnImpl c : m_cols) {
					for (RowImpl r : m_rows) {
						CellImpl cell = c.getCell(r);
						cell.setCellValue(o);
					}
				}
			}
		}
	}
}
