package org.tms.io.xml;

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
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext writer)
    {
        return null;
    }        
}

