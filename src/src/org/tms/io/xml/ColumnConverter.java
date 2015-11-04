package org.tms.io.xml;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.ColumnImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ColumnConverter extends ConverterBase
{
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
        
        writer.startNode("column");                
        writer.addAttribute("index", String.valueOf(c.getIndex()));
        
        marshalTableElement(c, writer, context, options().isColumnLabels());
        
        writer.endNode();
    }

    @Override
    public Column unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int cIdx = Integer.valueOf(reader.getAttribute("index"));
        
        Column c = t.addColumn(cIdx);
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(c, reader, context);
        
        return c;
    }        
}

