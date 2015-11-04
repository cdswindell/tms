package org.tms.io.xml;

import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.RowImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RowConverter extends ConverterBase
{
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
        
        writer.startNode("row");                
        writer.addAttribute("index", String.valueOf(r.getIndex()));
        
        marshalTableElement(r, writer, context, options().isRowLabels());
        
        writer.endNode();
    }

    @Override
    public Row unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int rIdx = Integer.valueOf(reader.getAttribute("index"));
        
        Row r = t.addRow(rIdx);
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(r, reader, context);
        
        return r;
    }        
}
