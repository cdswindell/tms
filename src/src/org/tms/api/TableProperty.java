package org.tms.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.tds.BaseElementImpl;

public enum TableProperty implements Comparable<TableProperty>
{
    // Base Element Properties
    Label("lb"),
    Description("desc"),
    Tags("tags"),
    isNull(null, true, false, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    isReadOnly("isRO", false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column),
    isSupportsNull("isNulls", false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    UUID("uuid", true, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset, ElementType.Cell),
    Ident("id", true, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    // Table Element Properties (TableContext implements initializable ones)
    Context(null, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Table(null, true, false, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    Precision("pr", true, false, true, ElementType.TableContext, ElementType.Table),
    
    // TableContext/Table Properties  
    numTables(null, true, false, ElementType.TableContext),
    TokenMapper(null, true, true, ElementType.TableContext),
    RowCapacityIncr("rci", false, true, ElementType.TableContext, ElementType.Table),
    ColumnCapacityIncr("cci", false, true, ElementType.TableContext, ElementType.Table),
    FreeSpaceThreshold("fst", false, true, ElementType.TableContext, ElementType.Table),
    isAutoRecalculate("recalc", false, true, ElementType.TableContext, ElementType.Table),
    isRowLabelsIndexed("isRLbX", false, true, ElementType.TableContext, ElementType.Table),
    isColumnLabelsIndexed("isCLbX", false, true, ElementType.TableContext, ElementType.Table),
    isCellLabelsIndexed("isClLbX", false, true, ElementType.TableContext, ElementType.Table),
    isSubsetLabelsIndexed("isSLbX", false, true, ElementType.TableContext, ElementType.Table),
    isPersistant("isP", false, true, ElementType.TableContext, ElementType.Table),
    
    // PendingDerivationThreadPool Properties
    isPendingAllowCoreThreadTimeout(null, true, false, true, ElementType.TableContext, ElementType.Table),
    numPendingCorePoolThreads(null, true, false, true, ElementType.TableContext, ElementType.Table),
    numPendingMaxPoolThreads(null, true, false, true, ElementType.TableContext, ElementType.Table),
    PendingThreadKeepAliveTimeout(null, true, false, true, ElementType.TableContext, ElementType.Table),    
    PendingThreadKeepAliveTimeoutUnit(null, true, false, true, ElementType.TableContext, ElementType.Table),    
    isPendingThreadPoolEnabled(null, true, false, true, ElementType.TableContext, ElementType.Table),

    // EventProcessorThreadPool Properties
    isEventsNotifyInSameThread(null, true, false, true, ElementType.TableContext, ElementType.Table),
    isEventsAllowCoreThreadTimeout(null, true, false, true, ElementType.TableContext, ElementType.Table),
    numEventsCorePoolThreads(null, true, false, true, ElementType.TableContext, ElementType.Table),
    numEventsMaxPoolThreads(null, true, false, true, ElementType.TableContext, ElementType.Table),
    EventsThreadKeepAliveTimeout(null, true, false, true, ElementType.TableContext, ElementType.Table),    

    // Table Element Properties 
    numSubsets("nSets", true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRows("nRows", true, false, ElementType.Table, ElementType.Subset),
    numColumns("nCols", true, false, ElementType.Table, ElementType.Subset),
    numCells("nCells", true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    numRowsCapacity(null, true, false, ElementType.Table),
    numColumnsCapacity(null, true, false, ElementType.Table),
    numCellsCapacity(null, true, false, ElementType.Column),
    NextCellOffset(null, true, false, ElementType.Table),
    Derivation("fx", false, false, ElementType.Column, ElementType.Row, ElementType.Cell),
    TimeSeries("tx", false, false, ElementType.Column, ElementType.Row),
    Affects(null, true, false, ElementType.Table, ElementType.Subset, ElementType.Column, ElementType.Row, ElementType.Cell),
    Index(null, true, false, ElementType.Row, ElementType.Column),
    isEnforceDataType("isEDT", false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    isInUse(null, true, false, ElementType.Row, ElementType.Column),
    
    Rows(null, true, false, ElementType.Table, ElementType.Subset),
    Columns(null, true, false, ElementType.Table, ElementType.Subset), 
    Subsets(null, true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    Cells(null, true, false, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    // Time Series support
    isTimeSeriesedRows(null, true, false, ElementType.Table),
    isTimeSeriesedRowsActive(null, true, false, ElementType.Table),
    TimeSeriesedRowsPeriod(null, true, false, ElementType.Table),
    TimeSeriesedRowsTimeStampColumn(null, true, false, ElementType.Table),
    
    isTimeSeriesedColumns(null, true, false, ElementType.Table),
    isTimeSeriesedColumnsActive(null, true, false, ElementType.Table),
    TimeSeriesedColumnsPeriod(null, true, false, ElementType.Table),
    TimeSeriesedColumnsTimeStampRow(null, true, false, ElementType.Table),
    
    // CellImpl properties
    Row(null, true, false, ElementType.Cell),
    Column(null, true, false, ElementType.Cell),
    CellOffset(null, true, false, ElementType.Row, ElementType.Cell),
    DataType("dt", false, false, ElementType.Column, ElementType.Cell),
    CellValue("v", false, false, ElementType.Cell),
    ErrorMessage("e", true, false, false, ElementType.Cell),
    Units("u", true, false, false, ElementType.Row, ElementType.Column, ElementType.Cell),
    DisplayFormat("f", true, false, true, ElementType.TableContext, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    Validator("cv", true, false, false, ElementType.Row, ElementType.Column, ElementType.Cell),
    ;
    
    private boolean m_optional;
    private boolean m_readOnly;
    private boolean m_initializable;
    private String m_tag;
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    
    /**
     * Constructor for properties that apply to objects that extend BaseElement
     */
    private TableProperty(String tag)
    {
        this(tag,
        	 true  /* optional */,
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
    private TableProperty(String tag, boolean readOnly, boolean initializable)
    {
        this(tag,
             false,
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
    private TableProperty(String tag,
    					  boolean isReadOnly,
                          boolean isInitializable,
                          ElementType... implementedBy)
    {
        this(tag, false, isReadOnly, isInitializable, implementedBy);
    }
    
    /**
     * Full constructor
     * @param isOptional
     * @param isReadOnly
     * @param isInitializable
     * @param implementedBy
     */
    private TableProperty(String tag,
    					  boolean isOptional,
                          boolean isReadOnly,
                          boolean isInitializable,
                          ElementType... implementedBy)
    {
        m_optional = isOptional;
        m_readOnly = isReadOnly;
        m_initializable = isInitializable;
        m_tag = tag;
        
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
    
    public boolean isMutable()
    {
        return !m_readOnly;
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
    
    public String getTag()
    {
    	if (m_tag != null)
    		return m_tag;
    	else
    		return this.name();
    }
    
    private static final Map<String, TableProperty> sf_tpLookUp = new HashMap<String, TableProperty>(TableProperty.values().length);
    static {
    	for (TableProperty tp : TableProperty.values()) {
	    	if (sf_tpLookUp.put(tp.getTag(), tp) != null)
	    		throw new IllegalTableStateException("Mutable Property tag clash: " + tp.name());
    	}
    }
    public static int compareByName(TableProperty a, TableProperty b)
    {
        return a.name().compareTo(b.name());
    }
    
    public static TableProperty byTag(String tag)
    {
    	if (tag == null || (tag = tag.trim()).length() == 0)
    		return null;
    	
    	TableProperty tp = sf_tpLookUp.get(tag);
    	if (tp != null)
    		return tp;
    	
    	tp = sf_tpLookUp.get(tag.toLowerCase());
    	if (tp != null)
    		return tp;
    	
    	try {
    		tp = TableProperty.valueOf(tag);
    	}
    	catch (IllegalArgumentException e) {}
    	
        return tp;
    }

}
