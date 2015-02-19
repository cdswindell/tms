package org.tms.api;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.tds.BaseElementImpl;

public enum TableProperty implements Comparable<TableProperty>
{
    // Base Element Properties
    Label,
    Description,   
    isNull(true, false, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    isReadOnly(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column),
    isSupportsNull(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    // Table Element Properties (Context implements initializable ones)
    Context(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Table(true, false, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Precision(false, true, ElementType.Context, ElementType.Table),
    isEnforceDataType(false, true, ElementType.Context, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    isInUse(true, false, ElementType.Row, ElementType.Column),
    
    // Context/Table Properties  
    TokenMapper(true, true, ElementType.Context),
    RowCapacityIncr(false, true, ElementType.Context, ElementType.Table),
    ColumnCapacityIncr(false, true, ElementType.Context, ElementType.Table),
    FreeSpaceThreshold(false, true, ElementType.Context, ElementType.Table),
    isAutoRecalculate(false, true, ElementType.Context, ElementType.Table),
    
    // EventProcessorThreadPool Properties
    isAllowCoreThreadTimeout(false, true, ElementType.Context),
    numCorePoolThreads(false, true, ElementType.Context),
    numMaxPoolThreads(false, true, ElementType.Context),
    ThreadKeepAliveTimeout(false, true, ElementType.Context),    

    // Table Properties (some shared with Subsets)
    numSubsets(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRows(true, false, ElementType.Table, ElementType.Subset),
    numColumns(true, false, ElementType.Table, ElementType.Subset),
    numCells(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRowsCapacity(true, false, ElementType.Table),
    numColumnsCapacity(true, false, ElementType.Table),
    numCellsCapacity(true, false, ElementType.Column),
    NextCellOffset(true, false, ElementType.Table),
    Derivation(false, false, ElementType.Column, ElementType.Row, ElementType.Cell),
    Affects(true, false, ElementType.Table, ElementType.Subset, ElementType.Column, ElementType.Row, ElementType.Cell),
    Index(true, false, ElementType.Row, ElementType.Column),
    
    Rows(true, false, ElementType.Table, ElementType.Subset),
    Columns(true, false, ElementType.Table, ElementType.Subset), 
    Subsets(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    Cells(true, false, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    // CellImpl properties
    Row(true, false, ElementType.Cell),
    Column(true, false, ElementType.Cell),
    CellOffset(true, false, ElementType.Row, ElementType.Cell),
    DataType(false, false, ElementType.Column, ElementType.Cell),
    CellValue(false, false, ElementType.Cell),
    isStronglyTyped(false, false, ElementType.Column);
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
             ElementType.Subset,
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
             ElementType.Subset);
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
    

    public boolean isLongValue()
    {
        switch(this)
        {
            case ThreadKeepAliveTimeout:
                return true;
                
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
            case Precision:
            case CellOffset:
            case NextCellOffset:
            case RowCapacityIncr:
            case ColumnCapacityIncr:
                return true;
                
            default:
                return false;
        }
    }
    
    public boolean isDoubleValue()
    {
        switch(this)
        {
            case FreeSpaceThreshold:
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
    
    public boolean isImplementedBy(BaseElementImpl te)
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
