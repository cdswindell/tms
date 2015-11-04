package org.tms.io.xml;

import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.CellImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CellConverter extends ConverterBase
{
    public CellConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public CellConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return CellImpl.class.isAssignableFrom(arg);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        CellImpl c = (CellImpl)arg;
        if (c.isNull())
            return;
        
        writer.startNode("cell");                
        writer.addAttribute("rIdx", String.valueOf(c.getRow().getIndex()));
        writer.addAttribute("cIdx", String.valueOf(c.getColumn().getIndex()));
        
        marshalTableElement(c, writer, context, true);
        
        writer.startNode("value");                
        context.convertAnother(c.getCellValue());
        writer.endNode();
        
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext writer)
    {
        return null;
    }        
}
