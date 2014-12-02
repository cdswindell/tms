package org.tms.tds;

import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;

public class Range extends TableElement
{
    private Set<Row> m_rows;
    private Set<Column> m_cols;
    
    public Range(Table parentTable)
    {
        super(ElementType.Range, parentTable);
    }

    @Override
    protected boolean isEmpty()
    {
        boolean hasRows = (m_rows != null && !m_rows.isEmpty());
        boolean hasCols = (m_cols != null && !m_cols.isEmpty());
        
        return !hasRows  && !hasCols;
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
        
        m_rows = new LinkedHashSet<Row>();
        m_cols = new LinkedHashSet<Column>();
    }
    
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
                return add((Row)tes);
            else if (tes instanceof Column)
                return add((Column)tes);
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
                return remove((Row)tes);
            else if (tes instanceof Column)
                return remove((Column)tes);
            else
                throw new UnimplementedException(tes, "remove");
        }
        else
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
                if (m_rows.remove(c))
                    removedAny = true;
                
                // remove the group to the corresponding row
                c.remove(this);
            }
        }
        
        return removedAny;
    } 
    
}
