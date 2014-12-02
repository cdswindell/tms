package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.tds.BaseElement;

public enum TableProperty
{
    Index(true, false),
    ReadOnly(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column),
    SupportsNull(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    Context(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Table(true, false, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
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
    
    private boolean m_optional;
    private boolean m_readOnly;
    private boolean m_initializable;
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    
    /**
     * Constructor for properties that apply to objects that extend BaseElement
     */
    private TableProperty()
    {
        this(true  /* optional */,
             false /* isReadOnly */,
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
        this(false,
             readOnly,
             initializable,
             ElementType.Table,
             ElementType.Row,
             ElementType.Column,
             ElementType.Cell,
             ElementType.Range);
    }
    
    /**
     * Constructor used by all required (non-optional) properties
     * @param isReadOnly
     * @param isInitializable
     * @param implementedBy
     */
    private TableProperty(boolean isReadOnly,
                          boolean isInitializable,
                          ElementType... implementedBy)
    {
        this(false, isReadOnly, isInitializable, implementedBy);
    }
    
    /**
     * Full constructor
     * @param isOptional
     * @param isReadOnly
     * @param isInitializable
     * @param implementedBy
     */
    private TableProperty(boolean isOptional,
                          boolean isReadOnly,
                          boolean isInitializable,
                          ElementType... implementedBy)
    {
        m_optional = isOptional;
        m_readOnly = isReadOnly;
        m_initializable = isInitializable;
        
        if (implementedBy != null)
        {
            for (ElementType t : implementedBy)
                m_implementedBy.add(t);
        }
    }
    
    public boolean isBooleanValue()
    {
        switch(this)
        {
            case ReadOnly:
            case SupportsNull:
                return true;
                
            default:
                return false;
        }
    }
    
    public boolean isIntValue()
    {
        switch(this)
        {
            case Index:
            case NumAllocRows:
            case NumAllocColumns:
            case RowAllocIncr:
            case ColumnAllocIncr:
                return true;
                
            default:
                return false;
        }
    }
    
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    public boolean isOptional()
    {
        return m_optional;
    }
    
    public boolean isNonOptional()
    {
        return !isOptional();
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
