package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.tds.BaseElement;

public enum TableProperty
{
    Index(true, false),
    Context(true, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Table(true, true, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Rows(true, false, ElementType.Table, ElementType.Range),
    Columns(true, false, ElementType.Table, ElementType.Range),
    Row(true, false, ElementType.Cell),
    Column(true, false, ElementType.Cell),
    NumAllocRows(true, false, ElementType.Table),
    NumAllocColumns(true, false, ElementType.Table),
    RowAllocIncr(false, true, ElementType.Context, ElementType.Table),
    ColumnAllocIncr(false, true, ElementType.Context, ElementType.Table),
    Label,
    Description;
    
    private boolean m_readOnly;
    private boolean m_initializable;
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    
    /**
     * Constructor for properties that apply to objects that extend BaseElement
     */
    private TableProperty()
    {
        this(false /* isReadOnly */,
             false /* Initializable */,
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
    private TableProperty(boolean readOnly, boolean initializable)
    {
        this(readOnly,
             initializable,
             ElementType.Table,
             ElementType.Row,
             ElementType.Column,
             ElementType.Cell,
             ElementType.Range);
    }
    
    private TableProperty(boolean isReadOnly,
                          boolean isInitializable,
                          ElementType... implementedBy)
    {
        m_readOnly = isReadOnly;
        m_initializable = isInitializable;
        
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
    
    public boolean isInitializable()
    {
        return m_initializable;
    }
    
    public boolean isImplementedBy(BaseElement te)
    {
        if (te == null)
            return false;
        else
            return isImplementedBy(te.getElementType());
    }
    
    public boolean isImplementedBy(ElementType et)
    {
        if (et == null)
            return false;
        else
            return m_implementedBy.contains(et);
    }
}
