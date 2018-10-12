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
    Tags,
    isNull(true, false, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    isReadOnly(false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column),
    isSupportsNull(false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    UUID(true, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset, ElementType.Cell),
    Ident(true, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    // Table Element Properties (TableContext implements initializable ones)
    Context(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Table(true, false, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Precision(true, false, true, ElementType.TableContext, ElementType.Table),
    
    // TableContext/Table Properties  
    numTables(true, false, ElementType.TableContext),
    TokenMapper(true, true, ElementType.TableContext),
    RowCapacityIncr(false, true, ElementType.TableContext, ElementType.Table),
    ColumnCapacityIncr(false, true, ElementType.TableContext, ElementType.Table),
    FreeSpaceThreshold(false, true, ElementType.TableContext, ElementType.Table),
    isAutoRecalculate(false, true, ElementType.TableContext, ElementType.Table),
    isRowLabelsIndexed(false, true, ElementType.TableContext, ElementType.Table),
    isColumnLabelsIndexed(false, true, ElementType.TableContext, ElementType.Table),
    isCellLabelsIndexed(false, true, ElementType.TableContext, ElementType.Table),
    isSubsetLabelsIndexed(false, true, ElementType.TableContext, ElementType.Table),
    isPersistant(false, true, ElementType.TableContext, ElementType.Table),
    
    // PendingDerivationThreadPool Properties
    isPendingAllowCoreThreadTimeout(true, false, true, ElementType.TableContext, ElementType.Table),
    numPendingCorePoolThreads(true, false, true, ElementType.TableContext, ElementType.Table),
    numPendingMaxPoolThreads(true, false, true, ElementType.TableContext, ElementType.Table),
    PendingThreadKeepAliveTimeout(true, false, true, ElementType.TableContext, ElementType.Table),    

    // EventProcessorThreadPool Properties
    isEventsNotifyInSameThread(true, false, true, ElementType.TableContext, ElementType.Table),
    isEventsAllowCoreThreadTimeout(true, false, true, ElementType.TableContext, ElementType.Table),
    numEventsCorePoolThreads(true, false, true, ElementType.TableContext, ElementType.Table),
    numEventsMaxPoolThreads(true, false, true, ElementType.TableContext, ElementType.Table),
    EventsThreadKeepAliveTimeout(true, false, true, ElementType.TableContext, ElementType.Table),    

    // Table Element Properties 
    numSubsets(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRows(true, false, ElementType.Table, ElementType.Subset),
    numColumns(true, false, ElementType.Table, ElementType.Subset),
    numCells(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRowsCapacity(true, false, ElementType.Table),
    numColumnsCapacity(true, false, ElementType.Table),
    numCellsCapacity(true, false, ElementType.Column),
    NextCellOffset(true, false, ElementType.Table),
    Derivation(false, false, ElementType.Column, ElementType.Row, ElementType.Cell),
    TimeSeries(false, false, ElementType.Column, ElementType.Row),
    Affects(true, false, ElementType.Table, ElementType.Subset, ElementType.Column, ElementType.Row, ElementType.Cell),
    Index(true, false, ElementType.Row, ElementType.Column),
    isEnforceDataType(false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    isInUse(true, false, ElementType.Row, ElementType.Column),
    
    Rows(true, false, ElementType.Table, ElementType.Subset),
    Columns(true, false, ElementType.Table, ElementType.Subset), 
    Subsets(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    Cells(true, false, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    // Time Series support
    isTimeSeriesedRows(true, false, ElementType.Table),
    isTimeSeriesedRowsActive(true, false, ElementType.Table),
    TimeSeriesedRowsPeriod(true, false, ElementType.Table),
    TimeSeriesedRowsTimeStampColumn(true, false, ElementType.Table),
    
    isTimeSeriesedColumns(true, false, ElementType.Table),
    isTimeSeriesedColumnsActive(true, false, ElementType.Table),
    TimeSeriesedColumnsPeriod(true, false, ElementType.Table),
    TimeSeriesedColumnsTimeStampRow(true, false, ElementType.Table),
    
    // CellImpl properties
    Row(true, false, ElementType.Cell),
    Column(true, false, ElementType.Cell),
    CellOffset(true, false, ElementType.Row, ElementType.Cell),
    DataType(false, false, ElementType.Column, ElementType.Cell),
    CellValue(false, false, ElementType.Cell),
    ErrorMessage(true, false, false, ElementType.Cell),
    Units(true, false, false, ElementType.Row, ElementType.Column, ElementType.Cell),
    DisplayFormat(true, false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    Validator(true, false, false, ElementType.Row, ElementType.Column, ElementType.Cell),
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
             ElementType.TableContext);
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
    
    public boolean isStringValue()
    {
        switch(this)
        {
            case Label:
            case Units:
            case Description:
            case DisplayFormat:
                return true;
                
            default:
                return false;
        }
    }
    
    public boolean isLongValue()
    {
        switch(this)
        {
            case EventsThreadKeepAliveTimeout:
            case PendingThreadKeepAliveTimeout:
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
