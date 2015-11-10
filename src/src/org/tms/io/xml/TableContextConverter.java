package org.tms.io.xml;

import org.tms.api.Row;
import org.tms.api.TableContext;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TableContextConverter extends BaseConverter
{
    static final public String ELEMENT_TAG = "tableContext";
    static final protected String TABLES_TAG = "tables";
    
    public TableContextConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public TableContextConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ContextImpl.class.isAssignableFrom(arg);
    }

    protected String getElementTag()
    {
        return TableContextConverter.ELEMENT_TAG;
    }
    
    /**
     * Return true of this row should be output
     */
    protected boolean isRelevant(Row r)
    {
    	return true;
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        ContextImpl tc = (ContextImpl)arg;
                
        writer.startNode(TABLES_TAG);
        
        for (TableImpl t : tc.getTables()) {
            pushExportAdapter(t);
            
            writer.startNode(TableConverter.ELEMENT_TAG);                
            context.convertAnother(t);
            writer.endNode();
            
            popExportAdapter();
        }
        
        writer.endNode();
    }

    @Override
    public TableContext unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        return null;
    }        
}
