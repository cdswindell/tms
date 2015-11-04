package org.tms.io.xml;

import org.tms.api.Cell;
import org.tms.api.Table;
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
        
        writer.startNode("dataType");  
        context.convertAnother(c.getDataType());
        writer.endNode();
        
        writer.startNode("value");  
        context.convertAnother(c.getCellValue());
        writer.endNode();
        
        writer.endNode();
    }

    @Override
    public Cell unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int rIdx = Integer.valueOf(reader.getAttribute("rIdx"));
        int cIdx = Integer.valueOf(reader.getAttribute("cIdx"));
        
        Cell c = t.getCell(t.getRow(rIdx), t.getColumn(cIdx));
        
        // upon return, we're left at the value tag
        unmarshalTableElement(c, reader, context);
        
        // get data type
        Class<?> dataType = (Class<?>)context.convertAnother(t, Class.class);
        reader.moveUp();
        
        // get Cell Value
        reader.moveDown();
        Object o = context.convertAnother(t, dataType);        
        reader.moveUp();
        
        c.setCellValue(o);
        
        return c;
    }        
}
