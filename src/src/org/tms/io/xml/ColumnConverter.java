package org.tms.io.xml;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.ColumnImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ColumnConverter extends ConverterBase
{
    static final public String COLUMN_TAG = "column";
    
    public ColumnConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public ColumnConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ColumnImpl.class.isAssignableFrom(arg);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        ColumnImpl c = (ColumnImpl)arg;
        if (options().isIgnoreEmptyColumns() && c.isNull())
            return;
        
        writer.startNode(COLUMN_TAG);                
        writer.addAttribute(INDEX_ATTR, String.valueOf(c.getIndex()));
        
        marshalTableElement(c, writer, context, options().isColumnLabels());
        
        writeNode(c, TableProperty.Units, UNITS_TAG, writer, context);
        writeNode(c, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        writeNode(c, TableProperty.DataType, DATATYPE_TAG, writer, context);
        
        writer.endNode();
    }

    @Override
    public Column unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int cIdx = Integer.valueOf(reader.getAttribute(INDEX_ATTR));
        
        Column c = t.addColumn(cIdx);
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(c, reader, context);
        
        String nodeName = reader.getNodeName();
        String strVal;
        if (UNITS_TAG.equals(nodeName)) {
            strVal = reader.getValue();
            if (strVal != null && (strVal = strVal.trim()).length() > 0)
                c.setUnits(strVal);
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
                c.setDisplayFormat(strVal);
            reader.moveUp();
            
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
        }
        
        if (DATATYPE_TAG.equals(nodeName)) {
            Class<?>dataType = (Class<?>)context.convertAnother(t, Class.class);
            if (dataType != null)
                c.setDataType(dataType);
            reader.moveUp();
            
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
        }
        
        return c;
    }        
}

