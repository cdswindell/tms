package org.tms.io.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.TimeSeries;
import org.tms.api.derivables.TimeSeriesable;
import org.tms.api.io.ArchivalIOOption;
import org.tms.api.utils.RegisterOp;
import org.tms.api.utils.TableCellValidator;
import org.tms.api.utils.Validatable;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.TableExportAdapter;
import org.tms.tds.CellImpl;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.TableCellsElementImpl;
import org.tms.teq.DerivationImpl;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

abstract class BaseConverter implements Converter
{
    static final protected String TMS_TABLE_KEY = "__tms table key__";
    static final protected String TMS_TABLE_CONVERTER_KEY = "__tms table converter key__";
    static final protected String TMS_DERIVATIONS_KEY = "__tms derivations key__";
    static final protected String TMS_TIMESERIES_KEY = "__tms timeseries key__";
    static final protected String TMS_READONLY_KEY  = "__tms read-only key__";
    static final protected String TMS_ALLOWNULLS_KEY  = "__tms allow nulls key__";
    static final protected String TMS_ENFORCE_KEY  = "__tms enforce datatype key__";
    static final protected String TMS_DATATYPE_CACHE_KEY = "__tms database cache key__";
    static final protected String TMS_TO_REGISTER_CACHE_KEY = "__tms to_register cache key__";
    static final protected String TMS_TS_ROWS_TS_COL_KEY = "__tms ts rows ts col key__";
    static final protected String TMS_TS_COLS_TS_ROW_KEY = "__tms ts cols ts row key__";
    
    static final protected String LABEL_TAG = "label";
    static final protected String DESC_TAG = "description";
    static final protected String TAGS_TAG = "tags";
    static final protected String DERIV_TAG = "derivation";
    static final protected String TIMESERIES_TAG = "timeseries";
    static final protected String VALIDATOR_TAG = "validator";
    static final protected String VALIDATOR_CLASS_TAG = "valClass";
    static final protected String VALIDATOR_IMPL_TAG = "valImpl";
    
    static final public String UNITS_TAG = "units";
    static final public String FORMAT_TAG = "format";
    static final public String DATATYPE_TAG = "dataType";
    static final public String UUID_TAG = "uuid";
    
    static final protected String INDEX_ATTR = "index";
    static final protected String READONLY_ATTR = "readOnly";
    static final protected String ALLOWNULLS_ATTR = "allowNulls";
    static final protected String ENFORCE_DATATYPE_ATTR = "enforce";
    static final protected String DERIV_PERIOD_ATTR = "repeat";
    
    static final protected String TS_ROWS_TS_COL_ATTR = "tsCol";
    static final protected String TS_COLS_TS_ROW_ATTR = "tsRow";
    
    private BaseWriter<?> m_writer;
    private BaseReader<?> m_reader;
    private ArchivalIOOption<?> m_options;
    
    private Map<String, String> m_attribCache;
    private boolean m_useAttribCache;
    
    public BaseConverter(BaseWriter<?> writer)
    {
        m_writer = writer;
        m_options = (ArchivalIOOption<?>)writer.options();
        m_attribCache = new HashMap<String, String>();
        m_useAttribCache = false;
    }

    public BaseConverter(BaseReader<?> reader)
    {
        m_reader = reader;
        m_options = (ArchivalIOOption<?>)reader.options();
        m_attribCache = new HashMap<String, String>();
        m_useAttribCache = false;
    }

    protected ArchivalIOOption<?> options()
    {
        return m_options;
    }

    /*
     * Export Only
     */
    protected boolean isExternalDependenceTable() 
    {
		Table t = m_writer.getTable();
		return t instanceof ExternalDependenceTableElement;
	}

    protected void popExportAdapter()
    {
    }

    protected void pushExportAdapter(Table t)
    {
        TableExportAdapter tea = new TableExportAdapter(t, options());        
        m_writer.createDelegate(tea);
    }

    protected int getNumConsumableColumns()
    {
        return m_writer.getNumConsumableColumns();
    }

    protected List<Column> getActiveColumns()
    {
        return m_writer.getActiveColumns();
    }
    
    protected boolean isIgnoreColumn(Column c) 
    {
        return m_writer.isIgnore(c);
    }
    
    protected boolean isIgnoreColumn(int cIdx) 
    {
        return m_writer.isIgnoreColumn(cIdx);
    }
    
    protected int getRemappedColumnIndex(Column c) 
    {
        return m_writer.getRemappedColumnIndex(c);
    }
    
    protected int getRemappedColumnIndex(int idx) 
    {
        return m_writer.getRemappedColumnIndex(idx);
    }
    
    protected boolean isIgnoreRow(Row r) 
    {
        return m_writer.isIgnore(r);
    }
    
    protected boolean isIgnoreRow(int rIdx) 
    {
        return m_writer.isIgnoreRow(rIdx);
    }
    
    protected int getRemappedRowIndex(Row r) 
    {
        return m_writer.getRemappedRowIndex(r);
    }
    
    protected int getRemappedRowIndex(int idx) 
    {
        return m_writer.getRemappedRowIndex(idx);
    }
    
    protected int getNumConsumableRows()
    {
        return m_writer.getNumConsumableRows();
    }

    protected int getNumRows()
    {
        return m_writer.getNumRows();
    }

    protected Row getRow(int i) 
    {
    	return m_writer.getRow(i);
	}

    protected Row getRowByEffectiveIndex(int i) 
    {
    	return m_writer.getRowByEffectiveIndex(i);
	}

    protected ElementType getTableElementType()
    {
    	return m_writer.getTableElementType();
    }
    
    protected void writeNode(TableElement te, TableProperty key, String tag, 
            HierarchicalStreamWriter writer, MarshallingContext context)
    {
        if (te.hasProperty(key)) {
            Object val = te.getProperty(key);
            if (val == null || (val instanceof String && ((String)(val = ((String)val).trim())).length() == 0)) 
                return;
            
            writer.startNode(tag);  
            context.convertAnother(val);
            writer.endNode(); 
        }
    }
    
    protected void writeNode(String val, String tag, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        if (val != null && (val = val.trim()).length() > 0) {
            writer.startNode(tag);  
            context.convertAnother(val);
            writer.endNode(); 
        }
    }
    
    protected void writeNode(Object val, String tag, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        if (val != null) {
            writer.startNode(tag);  
            context.convertAnother(val);
            writer.endNode(); 
        }
    }
    
    protected boolean hasValue(TableElement te, TableProperty key)
    {
        if (te.hasProperty(key)) {
            Object val = te.getProperty(key);
            if (val == null)
                return false;
            
            // one more check for empty strings
            if (val instanceof String && ((String)val).trim().length() == 0)
                return false;
                    
            return true;
        }
        
        return false;        
    }

    protected boolean isRelevant(TableElement te)
    {
        if (te == te.getTable())
            return true;
        
        if (te.isReadOnly() != te.getTable().isReadOnly())
            return true;
        
        if (te.isSupportsNull() != te.getTable().isSupportsNull())
            return true;
        
        if ((te.hasProperty(TableProperty.isEnforceDataType) && 
                                    te.isEnforceDataType() != te.getTable().isEnforceDataType()))
            return true;
        
        if (hasValue(te, TableProperty.Label)) {
            if (te instanceof Row) { 
            	if (options().isRowLabels())
            		return true;
            }
            else if (te instanceof Column) {
            	if (options().isColumnLabels())
                    return true;
            }
            else // not a row or column but labeled; it is required
            	return true;
        }
        
        if ((options().isTags() || options().isVerboseState()) && hasValue(te, TableProperty.Tags)) 
            return true;
        
        if ((options().isDescriptions() || options().isVerboseState()) && hasValue(te, TableProperty.Description)) 
            return true;
        
        if ((options().isDerivations() || options().isVerboseState()) && 
        		getTableElementType() == ElementType.Table &&
        		hasValue(te, TableProperty.Derivation))
            return true;
        
        if ((options().isTimeSeries() || options().isVerboseState()) && 
        		getTableElementType() == ElementType.Table &&
        		hasValue(te, TableProperty.TimeSeries))
            return true;
        
        if ((options().isValidators() || options().isVerboseState()) && hasValue(te, TableProperty.Validator))
            return true;
        
        if ((options().isUUIDs() || options().isVerboseState()) && hasValue(te, TableProperty.UUID))
            return true;
        
        return false;
    }
    
    public void marshalTableElement(TableElement te, 
                                    HierarchicalStreamWriter writer, 
                                    MarshallingContext context, 
                                    boolean includeLabel)
    {
        if (te == te.getTable() || te.isReadOnly() != te.getTable().isReadOnly())
            writer.addAttribute(READONLY_ATTR, String.valueOf(te.isReadOnly()));
            
        if (te == te.getTable() || te.isSupportsNull() != te.getTable().isSupportsNull())
            writer.addAttribute(ALLOWNULLS_ATTR, String.valueOf(te.isSupportsNull()));            
        
        if (te == te.getTable() || (te.hasProperty(TableProperty.isEnforceDataType) && 
                                    te.isEnforceDataType() != te.getTable().isEnforceDataType()))
            writer.addAttribute(ENFORCE_DATATYPE_ATTR, String.valueOf(te.isEnforceDataType()));            
        
        // Handle class-specific attributes and labels
        marshalClassSpecificElements(te, writer, context);
        
        if (includeLabel)
            writeNode(te, TableProperty.Label, LABEL_TAG, writer, context);
        
        if (options().isDescriptions() || options().isVerboseState())
            writeNode(te, TableProperty.Description, DESC_TAG, writer, context);
        
        if (options().isTags() || options().isVerboseState())
            writeNode(te, TableProperty.Tags, TAGS_TAG, writer, context);

        if ((options().isDerivations() || options().isVerboseState()) && getTableElementType() == ElementType.Table &&
        		te instanceof Derivable && ((Derivable)te).isDerived()) 
        {
            Derivation d =  ((Derivable)te).getDerivation();
            writer.startNode(DERIV_TAG);
            if (d.isPeriodic())
            	writer.addAttribute(DERIV_PERIOD_ATTR, String.valueOf(((DerivationImpl)d).getPeriodInMilliSeconds()));
            writer.setValue(d.getExpression());
            writer.endNode();
        }
        
        if ((options().isTimeSeries() || options().isVerboseState()) && getTableElementType() == ElementType.Table &&
        		te instanceof TimeSeriesable && ((TimeSeriesable)te).isTimeSeries()) 
        {
            TimeSeries d =  ((TimeSeriesable)te).getTimeSeries();
            writer.startNode(TIMESERIES_TAG);
            writer.setValue(d.getExpression());
            writer.endNode();
        }
        
        if (options().isValidators() && te instanceof Validatable) {
            Validatable v = (Validatable)te;
            Object o = v.getValidator();
            if (o != null) {
                writer.startNode(VALIDATOR_TAG);
                
                writer.startNode(VALIDATOR_CLASS_TAG);
                context.convertAnother(o.getClass());              
                writer.endNode();
                
                writer.startNode(VALIDATOR_IMPL_TAG);
                context.convertAnother(o);              
                writer.endNode();
                
                writer.endNode();
            }
        }
        
        if (options().isUUIDs() || options().isVerboseState())
            writeNode(te, TableProperty.UUID, UUID_TAG, writer, context);
    }        

    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        // override in super classes to handle class-specific data       
    }

    /*
     * XML Import Only
     */
    protected TableContext getTableContext()
    {
        return m_reader.getTableContext();
    }
    
    protected Table getTable(UnmarshallingContext context)
    {
        return (Table)context.get(TMS_TABLE_KEY);
    }
    
    protected void cacheAllAttributes(HierarchicalStreamReader reader)
    {
    	clearCachedAttributes();
    	
    	int attrCnt = reader.getAttributeCount();
    	for (int i = 0; i < attrCnt; i++) {
    		String attrName = reader.getAttributeName(i);
    		String attrVal = reader.getAttribute(i);
    		
    		if (attrVal != null && (attrVal = attrVal.trim()).length() > 0)
    			m_attribCache.put(attrName, attrVal);
    	}
    	
    	m_useAttribCache = true;
    }
    
    protected void clearCachedAttributes() 
    {
    	m_attribCache.clear();
    	m_useAttribCache = false;
	}

	protected void unmarshalTableElement(TableElement t, boolean doLabel, HierarchicalStreamReader reader, UnmarshallingContext context)
    {
		try {
	        Boolean val = readAttributeBoolean(READONLY_ATTR, reader);
	        if (val != null)
	            t.setReadOnly(val);
	        
	        val = readAttributeBoolean(ALLOWNULLS_ATTR, reader);
	        if (val != null)
	            t.setSupportsNull(val);
	        
	        val = readAttributeBoolean(ENFORCE_DATATYPE_ATTR, reader);
	        if (val != null)
	            t.setEnforceDataType(val);
	        
	        // Handle class-specific attributes and labels
	        unmarshalClassSpecificElements(t, reader, context);
	        
	        // process other common elements
	        while (reader.hasMoreChildren()) {
	            reader.moveDown();
	            String nodeName = reader.getNodeName();
	            String strVal = null;
	            switch (nodeName) {
	                case LABEL_TAG:
	                    if (doLabel) {
	                        strVal = (String)context.convertAnother(t, String.class);
	                        t.setLabel(strVal);
	                    }
	                    break;
	                    
	                case DESC_TAG:
	                    if (options().isDescriptions()) {
	                        strVal = (String)context.convertAnother(t, String.class);
	                        t.setDescription(strVal);
	                    }
	                    break;
	                    
	                case TAGS_TAG:
	                    while (reader.hasMoreChildren()) {
	                        reader.moveDown();
	                        if (options().isTags()) {
	                            strVal = (String)context.convertAnother(t, String.class);
	                            t.tag(strVal);
	                        }
	                        reader.moveUp();
	                    }
	                    break;
	                    
	                case DERIV_TAG:
	                    if (options().isDerivations()) {
	                    	Long period = readAttributeLong(DERIV_PERIOD_ATTR, reader);
	                        strVal = (String)context.convertAnother(t, String.class);
	                        if (t instanceof Derivable)
	                            cacheDerivation((Derivable)t, strVal, period, context);
	                    }
	                    break;
	                    
	                case TIMESERIES_TAG:
	                    if (options().isTimeSeries()) {
	                        strVal = (String)context.convertAnother(t, String.class);
	                        if (t instanceof TimeSeriesable)
	                            cacheTimeSeries((TimeSeriesable)t, strVal, context);
	                    }
	                    break;
	                    
	                case VALIDATOR_TAG:
	                    if (options().isValidators() && t instanceof Validatable) {
	                        Validatable v = (Validatable)t;
	                        Class<?> valClass = null;
	                        reader.moveDown();
	                        try {
	                            valClass = (Class<?>)context.convertAnother(v, Class.class);
	                        }
	                        catch (Exception e) {
	                            valClass = TableCellValidator.class;
	                        }
	                        reader.moveUp();
	                        
	                        reader.moveDown();
	                        Object o = context.convertAnother(v, valClass);;
	                        reader.moveUp();
	                        
	                        if (o != null)
	                            v.setValidator((TableCellValidator)o);
	                    }
	                    break;
	                    
	                case UUID_TAG:
	                    if (options().isUUIDs()) {
	                        strVal = (String)context.convertAnother(t, String.class);
	                        if (t instanceof TableCellsElementImpl) 
	                            ((TableCellsElementImpl)t).setUUID(strVal);
	                        if (t instanceof CellImpl) 
	                            ((CellImpl)t).setUUID(strVal);
	                    }
	                    break;
	                    
	                default:
	                	return;
	            }
	            
	            reader.moveUp();
	        }
		}
		finally {
			clearCachedAttributes();
		}
    }  	

	protected void unmarshalClassSpecificElements(TableElement t, HierarchicalStreamReader reader, UnmarshallingContext context) 
	{
		// override in subclass as appropriate
	}

	protected void postConstructAction(Table t, Object te, UnmarshallingContext context) 
	{
		ExternalDependenceTableConverter edCvt = (ExternalDependenceTableConverter)context.get(TMS_TABLE_CONVERTER_KEY);
		if (edCvt != null) 
			edCvt.postConstructAction(t, te);
	}
	
	protected int getRemappedColIdx(int cIdx, UnmarshallingContext context) 
	{
		ExternalDependenceTableConverter edCvt = (ExternalDependenceTableConverter)context.get(TMS_TABLE_CONVERTER_KEY);
		if (edCvt != null) 
			cIdx = edCvt.getRemappedColIdx(cIdx);
		
		return cIdx;
	}   
	
	protected int getRemappedRowIdx(int rIdx, UnmarshallingContext context) 
	{
		ExternalDependenceTableConverter edCvt = (ExternalDependenceTableConverter)context.get(TMS_TABLE_CONVERTER_KEY);
		if (edCvt != null) 
			rIdx = edCvt.getRemappedRowIdx(rIdx);
		
		return rIdx;
	}   
	
	private void cacheDerivation(Derivable te, String deriv, Long period, UnmarshallingContext context)
    {
        Map<Derivable, CachedDerivation> derivsMap = getDerivationsMap(context);  
        CachedDerivation cd = new CachedDerivation(deriv, period);
        derivsMap.put(te,  cd);
    }

	private void cacheTimeSeries(TimeSeriesable te, String ts, UnmarshallingContext context)
    {
        Map<TimeSeriesable, CachedDerivation> tsMap = getTimeSeriesMap(context);  
        CachedDerivation cd = new CachedDerivation(ts);
        tsMap.put(te,  cd);
    }

    protected Map<Derivable, CachedDerivation> getDerivationsMap(UnmarshallingContext context)
    {
        @SuppressWarnings("unchecked")
        Map<Derivable, CachedDerivation> derivsMap = (Map<Derivable, CachedDerivation>)context.get(TMS_DERIVATIONS_KEY);
        if (derivsMap == null) {
            derivsMap = new LinkedHashMap<Derivable, CachedDerivation>();
            context.put(TMS_DERIVATIONS_KEY, derivsMap);
        }
        
        return derivsMap;
    }

    protected Map<TimeSeriesable, CachedDerivation> getTimeSeriesMap(UnmarshallingContext context)
    {
        @SuppressWarnings("unchecked")
        Map<TimeSeriesable, CachedDerivation> tsMap = (Map<TimeSeriesable, CachedDerivation>)context.get(TMS_TIMESERIES_KEY);
        if (tsMap == null) {
        	tsMap = new LinkedHashMap<TimeSeriesable, CachedDerivation>();
            context.put(TMS_TIMESERIES_KEY, tsMap);
        }
        
        return tsMap;
    }

    protected Boolean readAttributeBoolean(String attrName, HierarchicalStreamReader reader)
    {
        String val = m_useAttribCache ? m_attribCache.get(attrName) : reader.getAttribute(attrName);
        if (val != null) 
            return Boolean.parseBoolean(val);
        
        return null;
    }
    
    protected Integer readAttributeInteger(String attrName, HierarchicalStreamReader reader)
    {
        try {
            String val = m_useAttribCache ? m_attribCache.get(attrName) : reader.getAttribute(attrName);
            if (val != null)          
                return Integer.parseInt(val);
        }
        catch (Exception e) {}
        
        return null;
    }
    
    protected Long readAttributeLong(String attrName, HierarchicalStreamReader reader)
    {
        try {
            String val = m_useAttribCache ? m_attribCache.get(attrName) : reader.getAttribute(attrName);
            if (val != null)          
                return Long.parseLong(val);
        }
        catch (Exception e) {}
        
        return null;
    }
    
    protected Double readAttributeDouble(String attrName, HierarchicalStreamReader reader)
    {
        try {
            String val = m_useAttribCache ? m_attribCache.get(attrName) : reader.getAttribute(attrName);
            if (val != null)          
                return Double.parseDouble(val);
        }
        catch (Exception e) {}
        
        return null;
    }
        
    /**
     * Column and cell dataType classes registered with the RegisterOp annotation must be
     * processed before derivations are processed. The code below caches classtes to register
     * in an efficient manner.
     * @param context
     * @param dataType
     */
	protected void cacheDataType(UnmarshallingContext context, Class<?> dataType) 
	{
		// first check if the class has already been evaluated and determined to be registerable
		@SuppressWarnings("unchecked")
		Set<Class<?>> toRegisterDataTypes = (Set<Class<?>>)context.get(TMS_TO_REGISTER_CACHE_KEY);
		if (toRegisterDataTypes != null && toRegisterDataTypes.contains(dataType))
			return;

		//If it isn't, then check if it's been evaluated
		@SuppressWarnings("unchecked")
		Set<Class<?>> evaluatedDatatypes = (Set<Class<?>>)context.get(TMS_DATATYPE_CACHE_KEY);
		if (evaluatedDatatypes == null) {
			evaluatedDatatypes = new HashSet<Class<?>>();
			context.put(TMS_DATATYPE_CACHE_KEY, evaluatedDatatypes);
		}
		
		if (evaluatedDatatypes.contains(dataType))
			return;
		
		// ok, so we haven't seen this class before in any context; mark it so we don' reprocess
		evaluatedDatatypes.add(dataType);
		
		// now check if the marking annotation is present
		if (dataType.getAnnotation(RegisterOp.class) != null) {
			if (toRegisterDataTypes == null) {
				toRegisterDataTypes = new HashSet<Class<?>>();
				context.put(TMS_TO_REGISTER_CACHE_KEY, toRegisterDataTypes);
			}
			
			toRegisterDataTypes.add(dataType);
		}
	}        

    final static class CachedDerivation
    {
    	private String m_deriv;
		private long m_period;

		CachedDerivation(String deriv)
    	{
    		this(deriv, null);
    	}
		
		CachedDerivation(String deriv, Long period)
    	{
    		m_deriv = deriv;
    		m_period = period != null ? period : 0l;
    	}
		
		String getDerivation()
		{
			return m_deriv;
		}
		
		long getPeriod()
		{
			return m_period;
		}
    }
}
