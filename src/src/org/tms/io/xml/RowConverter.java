package org.tms.io.xml;

import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.RowImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RowConverter extends ConverterBase
{
    static final public String ROW_TAG = "row";
    
    public RowConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public RowConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return RowImpl.class.isAssignableFrom(arg);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        RowImpl r = (RowImpl)arg;
        
        writer.startNode(ROW_TAG);                
        writer.addAttribute(INDEX_ATTR, String.valueOf(r.getIndex()));
        
        marshalTableElement(r, writer, context, options().isRowLabels());
        
        writeNode(r, TableProperty.Units, UNITS_TAG, writer, context);
        writeNode(r, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        
        writer.endNode();
    }

    @Override
    public Row unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int rIdx = Integer.valueOf(reader.getAttribute(INDEX_ATTR));
        
        Row r = t.addRow(rIdx);
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(r, reader, context);
        
        String nodeName = reader.getNodeName();
        String strVal;
        if (UNITS_TAG.equals(nodeName)) {
            strVal = reader.getValue();
            if (strVal != null && (strVal = strVal.trim()).length() > 0)
                r.setUnits(strVal);
            reader.moveUp();
            
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
        }
        
        if (FORMAT_TAG.equals(nodeName)) {
            strVal = reader.getValue();
            if (strVal != null && (strVal = strVal.trim()).length() > 0)
                r.setDisplayFormat(strVal);
            reader.moveUp();
            
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
        }
        
        return r;
    }        
}
