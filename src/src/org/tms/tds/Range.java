package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.util.JustInTimeSet;

public class Range extends TableElement
{
    private Set<Row> m_rows;
    private Set<Column> m_cols;
    
    protected Range(Table parentTable)
    {
        super(ElementType.Range, parentTable);
        
        // associate the group with the table
        if (parentTable != null)
            parentTable.add(this);
    }

    /*
     * Field Getters/Setters
     */
    protected List<Row> getRows()
    {
        return new ArrayList<Row>(((JustInTimeSet<Row>)m_rows).clone());
    }
    
    protected int getNumRows()
    {
    	return m_rows.size();
    }

    protected List<Column> getColumns()
    {
        return new ArrayList<Column>(((JustInTimeSet<Column>)m_cols).clone());
    }

    protected int getNumColumns()
    {
    	return m_cols.size();
    }

    /*
     * Class-specific methods
     */
    protected boolean contains(TableElementSlice r)
    {
        if (r != null) {
            if (r instanceof Row)
                return m_rows.contains(r);
            else if (r instanceof Column)
                return m_cols.contains(r);
            else
                throw new UnimplementedException(r, "contains");
        }
        else
            return false;
    }
    
    boolean add(TableElementSlice tes)
    {
        if (tes != null) {
            if (tes instanceof Row)
                return add(new Row[] {(Row)tes});
            else if (tes instanceof Column)
                return add(new Column[] {(Column)tes});
            else
                throw new UnimplementedException(tes, "add");
        }
        else
            return false;
    }
    
    boolean remove(TableElementSlice tes)
    {
        if (tes != null) {
            if (tes instanceof Row)
                return remove(new Row[] {(Row)tes});
            else if (tes instanceof Column)
                return remove(new Column[] {(Column)tes});
            else
                throw new UnimplementedException(tes, "remove");
        }
        else
            return false;
    }
    
    protected boolean addAll(Collection<? extends TableElementSlice> elems)
    {
        boolean addedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableElementSlice e : elems) 
            {
                if (this.add(e))
                    addedAny = true;
            }
        }
        
        return addedAny;
    }
    
    protected boolean removeAll(Collection<? extends TableElementSlice> elems)
    {
        boolean removedAny = false;
        if (elems != null) {            
            // iterate over all rows, adding them to the group
            for (TableElementSlice e : elems) 
            {
                if (this.remove(e))
                    removedAny = true;
            }
        }
        
        return removedAny;
    }
        
    protected boolean containsAll(Collection<? extends TableElementSlice> elems)
    {
        if (elems != null && !elems.isEmpty()) {            
            // iterate over all elements
            for (TableElementSlice e : elems) 
            {
                if (!this.contains(e))
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
        
    protected boolean add(Row... rows)
    {
        boolean addedAny = false;
        if (rows != null) {
            // make sure all of the rows belong to the same table
            vetParent(rows);
            
            // iterate over all rows, adding them to the group
            for (Row r : rows) 
            {
                if (m_rows.add(r))
                    addedAny = true;
                
                // add the group to the corresponding row
                r.add(this);
            }
        }
        
        return addedAny;
    }       

    protected boolean remove(Row... rows)
    {
        boolean removedAny = false;
        if (rows != null) {
            // iterate over all rows, removing them from the group
            for (Row r : rows) 
            {
                if (m_rows.remove(r))
                    removedAny = true;
                
                // remove the group to the corresponding row
                r.remove(this);
            }
        }
        
        return removedAny;
    } 
    
    protected boolean add(Column... cols)
    {
        boolean addedAny = false;
        if (cols != null) {
            // make sure all of the columns belong to the same table
            vetParent(cols);

            // iterate over all rows, adding them to the group
            for (Column c : cols) 
            {
                if (m_cols.add(c))
                    addedAny = true;

                // add the group to the corresponding row
                c.add(this);
            }
        }

        return addedAny;
    }
    
    protected boolean remove(Column... cols)
    {
        boolean removedAny = false;
        if (cols != null) {
            // iterate over all rows, removing them from the group
            for (Column c : cols) 
            {
                if (m_cols.remove(c))
                    removedAny = true;
                
                // remove the group to the corresponding row
                c.remove(this);
            }
        }
        
        return removedAny;
    } 
    
    protected Iterable<Row> rowIterable()
    {
        return new BaseElementIterable<Row>(getRows());
    }
    
    protected Iterable<Column> columnIterable()
    {
        return new BaseElementIterable<Column>(getColumns());
    }
    
    /*
     * Overridden Methods
     */
    
    @Override protected void delete()
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
    protected void initialize(TableElement e)
    {
        super.initialize(e);
        
        BaseElement source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {
                default:
                    throw new IllegalStateException("No initialization available for Range Property: " + tp);                       
            }
        }
        
        m_rows = new JustInTimeSet<Row>();
        m_cols = new JustInTimeSet<Column>();
    }
        
    @Override
    protected int getNumCells()
    {
    	int numCells = 0;
    	for (Column c : m_cols) {
    		if (c != null) 
    			numCells += c.getNumCells();
    	}
    	
        return numCells;
    }

	@Override
	protected void fill(Object o) 
	{
		if (!isEmpty()) {
			if (getNumRows() == 0 && m_cols != null) 
				m_cols.forEach(c->c.fill(o)); // if group is just columns, fill them
			else if (getNumColumns() == 0 && m_rows != null) 
				m_rows.forEach(r->r.fill(o)); // if group is just rows, fill them
			else {
				for (Column c : m_cols) {
					for (Row r : m_rows) {
						Cell cell = c.getCell(r);
						cell.setCellValue(o);
					}
				}
			}
		}
	}
}
