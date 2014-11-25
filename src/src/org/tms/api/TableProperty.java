package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.tds.TableElement;

public enum TableProperty
{
    Label,
    Description,
    Context(true, TableElementType.Table, TableElementType.Row, TableElementType.Column, TableElementType.Cell, TableElementType.Range),
    Table(true, TableElementType.Row, TableElementType.Column, TableElementType.Cell, TableElementType.Range),
    Rows(true, TableElementType.Table, TableElementType.Range),
    Columns(true, TableElementType.Table, TableElementType.Range),
    Row(true, TableElementType.Cell),
    Column(true, TableElementType.Cell);
    
    private boolean m_readOnly;
    private Set<TableElementType> m_implementedBy = new HashSet<TableElementType>();
    
    private TableProperty()
    {
        this(false /* isReadOnly */,
             TableElementType.Table,
             TableElementType.Row,
             TableElementType.Column,
             TableElementType.Cell,
             TableElementType.Range,
             TableElementType.Context);
    }
    
    private TableProperty(boolean isReadOnly,
                          TableElementType... implementedBy)
    {
        m_readOnly = isReadOnly;
        
        if (implementedBy != null)
        {
            for (TableElementType t : implementedBy)
                m_implementedBy.add(t);
        }
    }
    
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    public boolean isImplementedBy(TableElement te)
    {
        if (te == null)
            return false;
        else
            return m_implementedBy.contains(te.getTableElementType());
    }
}
