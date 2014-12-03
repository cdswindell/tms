package org.tms.tds;

import java.util.Collections;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.util.WeakHashSet;

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
                return getRowsField(FieldAccess.ReturnEmptyIfNull).size();
                
            case numColumns:
                return getColumnField(FieldAccess.ReturnEmptyIfNull).size();
                
            case Rows:
                return null; // TODO: implement
                
            case Columns:
                return null; // TODO: implement
                
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
        
        m_rows = null;
        m_cols = null;
    }
    
    private Set<Row> getRowsField(FieldAccess... fas)
    {
        FieldAccess fa = FieldAccess.checkAccess(fas);
        if (m_rows == null) {
            if (fa == FieldAccess.CreateIfNull)
                m_rows = new WeakHashSet<Row>();
            else
                return Collections.emptySet();
        }
        
        return m_rows;
    }
    
    private Set<Column> getColumnField(FieldAccess... fas)
    {
        FieldAccess fa = FieldAccess.checkAccess(fas);
        if (m_cols == null) {
            if (fa == FieldAccess.CreateIfNull)
                m_cols = new WeakHashSet<Column>();
            else
                return Collections.emptySet();
        }
        
        return m_cols;
    }
    
    protected boolean contains(TableElementSlice r)
    {
        if (r != null) {
            if (r instanceof Row)
                return getRowsField(FieldAccess.ReturnEmptyIfNull).contains(r);
            else if (r instanceof Column)
                return getColumnField(FieldAccess.ReturnEmptyIfNull).contains(r);
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
                if (getRowsField().add(r))
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
                if (getRowsField().remove(r))
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
                if (getColumnField().add(c))
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
                if (getColumnField().remove(c))
                    removedAny = true;
                
                // remove the group to the corresponding row
                c.remove(this);
            }
        }
        
        return removedAny;
    } 
    
}
