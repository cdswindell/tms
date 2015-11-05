package org.tms.io.xml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.io.IOOption;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

abstract public class BaseConverter implements Converter
{
    static final protected String TMS_TABLE_KEY = "__tms table key__";
    static final protected String TMS_DERIVATIONS_KEY = "__tms derivations key__";
    static final protected String TMS_READONLY_KEY  = "__tms read-only key__";
    static final protected String TMS_ALLOWNULLS_KEY  = "__tms allow nulls key__";
    static final protected String TMS_ENFORCE_KEY  = "__tms enforce datatype key__";
    
    static final protected String LABEL_TAG = "label";
    static final protected String DESC_TAG = "description";
    static final protected String TAGS_TAG = "tags";
    static final protected String DERIV_TAG = "derivation";
    
    static final public String UNITS_TAG = "units";
    static final public String FORMAT_TAG = "format";
    static final public String DATATYPE_TAG = "dataType";
    
    static final protected String INDEX_ATTR = "index";
    static final protected String READONLY_ATTR = "readOnly";
    static final protected String ALLOWNULLS_ATTR = "allowNulls";
    static final protected String ENFORCE_DATATYPE_ATTR = "enforce";
    
    private BaseWriter<?> m_writer;
    private BaseReader<?> m_reader;
    private IOOption<?> m_options;
    
    public BaseConverter(BaseWriter<?> writer)
    {
        m_writer = writer;
        m_options = writer.options();
    }

    public BaseConverter(BaseReader<?> reader)
    {
        m_reader = reader;
        m_options = reader.options();
    }

    protected IOOption<?> options()
    {
        return m_options;
    }

    /*
     * XML Export Only
     */
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
        return m_writer.isIgnoreColumn(c);
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
        return m_writer.isIgnoreRow(r);
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
        
        if (includeLabel)
            writeNode(te, TableProperty.Label, LABEL_TAG, writer, context);
        
        writeNode(te, TableProperty.Description, DESC_TAG, writer, context);
        writeNode(te, TableProperty.Tags, TAGS_TAG, writer, context);

        if (te instanceof Derivable && ((Derivable)te).isDerived()) {
            Derivation d =  ((Derivable)te).getDerivation();
            writer.startNode(DERIV_TAG);
            writer.setValue(d.getExpression());
            writer.endNode();
        }
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
    
    protected void unmarshalTableElement(TableElement t, HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Boolean val = readAttributeBoolean(READONLY_ATTR, reader);
        if (val != null)
            t.setReadOnly(val);
        
        val = readAttributeBoolean(ALLOWNULLS_ATTR, reader);
        if (val != null)
            t.setSupportsNull(val);
        
        val = readAttributeBoolean(ENFORCE_DATATYPE_ATTR, reader);
        if (val != null)
            t.setEnforceDataType(val);
        
        // process other common elements
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            String strVal = null;
            switch (nodeName) {
                case LABEL_TAG:
                    strVal = (String)context.convertAnother(t, String.class);
                    t.setLabel(strVal);
                    break;
                    
                case DESC_TAG:
                    strVal = (String)context.convertAnother(t, String.class);
                    t.setDescription(strVal);
                    break;
                    
                case TAGS_TAG:
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        strVal = (String)context.convertAnother(t, String.class);
                        t.tag(strVal);
                        reader.moveUp();
                    }
                    break;
                    
                case DERIV_TAG:
                    strVal = (String)context.convertAnother(t, String.class);
                    if (t instanceof Derivable)
                        cacheDerivation((Derivable)t, strVal, context);
                    break;
                    
                default:
                    return;
            }
            
            reader.moveUp();
        }
    }       
    
    private void cacheDerivation(Derivable te, String deriv, UnmarshallingContext context)
    {
        Map<Derivable, String> derivsMap = getDerivationsMap(context);       
        derivsMap.put(te,  deriv);
    }

    protected Map<Derivable, String> getDerivationsMap(UnmarshallingContext context)
    {
        @SuppressWarnings("unchecked")
        Map<Derivable, String> derivsMap = (Map<Derivable, String>)context.get(TMS_DERIVATIONS_KEY);
        if (derivsMap == null) {
            derivsMap = new LinkedHashMap<Derivable, String>();
            context.put(TMS_DERIVATIONS_KEY, derivsMap);
        }
        
        return derivsMap;
    }

    protected Boolean readAttributeBoolean(String attrName, HierarchicalStreamReader reader)
    {
        String val = reader.getAttribute(attrName);
        if (val != null) 
            return Boolean.parseBoolean(val);
        
        return null;
    }
    
    protected Integer readAttributeInteger(String attrName, HierarchicalStreamReader reader)
    {
        try {
            String val = reader.getAttribute(attrName);
            if (val != null)          
                return Integer.parseInt(val);
        }
        catch (Exception e) {}
        
        return null;
    }
}
