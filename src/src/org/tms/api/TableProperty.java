package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.tds.BaseElement;

public enum TableProperty implements Comparable<TableProperty>
{
    // Base Element Properties
    Label,
    Description,   
    isEmpty(true, false, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    isReadOnly(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column),
    isSupportsNull(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    // Table Element Properties (Context implements initializable ones)
    Index(true, false),
    Context(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    Table(true, false, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Range),
    isEnforceDataType(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    isInUse(true, false, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    // Context/Table Properties  
    RowCapacityIncr(false, true, ElementType.Context, ElementType.Table),
    ColumnCapacityIncr(false, true, ElementType.Context, ElementType.Table),
    
    // Table Properties (some shared with Ranges)
    numRanges(true, false, ElementType.Table, ElementType.Row, ElementType.Column),
    numRows(true, false, ElementType.Table, ElementType.Range),
    numColumns(true, false, ElementType.Table, ElementType.Range),
    numRowsCapacity(true, false, ElementType.Table),
    numColumnsCapacity(true, false, ElementType.Table),
    
    Rows(true, false, ElementType.Table, ElementType.Range),
    Columns(true, false, ElementType.Table, ElementType.Range), 
    Ranges(true, false, ElementType.Table, ElementType.Row, ElementType.Column),
    
    // Cell properties
    Row(true, false, ElementType.Cell),
    Column(true, false, ElementType.Cell),

    ;
    
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
                if (!m_implementedBy.add(t))
                    throw new TableException(String.format("Table Property: %s Duplicate BaseElementType: %s", this, t), TableErrorClass.Invalid);
        }
    }
    
    public boolean isBooleanValue()
    {
        if (this.name().startsWith("is"))
            return true;
        
        switch(this)
        {
            default:
                return false;
        }
    }
    
    public boolean isIntValue()
    {
        if (this.name().startsWith("num"))
            return true;
        
        switch(this)
        {
            case Index:
            case RowCapacityIncr:
            case ColumnCapacityIncr:
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
    
    public static int compareByName(TableProperty a, TableProperty b)
    {
        return a.name().compareTo(b.name());
    }
}
