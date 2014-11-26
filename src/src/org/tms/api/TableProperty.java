package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.tds.BaseElement;

public enum TableProperty
{
    Label,
    Description,
    Index(true),
    Context(true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Table(true, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Rows(true, ElementType.Table, ElementType.Range),
    Columns(true, ElementType.Table, ElementType.Range),
    Row(true, ElementType.Cell),
    Column(true, ElementType.Cell);
    
    private boolean m_readOnly;
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    
    /**
     * Constructor for properties that apply to objects that extend BaseElement
     */
    private TableProperty()
    {
        this(false /* isReadOnly */,
             ElementType.Table,
             ElementType.Row,
             ElementType.Column,
             ElementType.Cell,
             ElementType.Range,
             ElementType.Context);
    }
    
    /**
     * Constructor for properties that apply to objects that extend TableElement
     * @param readOnly
     */
    private TableProperty(boolean readOnly)
    {
        this(readOnly,
             ElementType.Table,
             ElementType.Row,
             ElementType.Column,
             ElementType.Cell,
             ElementType.Range);
    }
    
    private TableProperty(boolean isReadOnly,
                          ElementType... implementedBy)
    {
        m_readOnly = isReadOnly;
        
        if (implementedBy != null)
        {
            for (ElementType t : implementedBy)
                m_implementedBy.add(t);
        }
    }
    
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    public boolean isImplementedBy(BaseElement te)
    {
        if (te == null)
            return false;
        else
            return m_implementedBy.contains(te.getTableElementType());
    }
}
