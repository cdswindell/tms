package org.tms.io.xml;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.CellImpl;
import org.tms.tds.CellUtils;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CellConverter extends BaseConverter
{
    static final public String CELL_TAG = "cell";
    static final public String VALUE_TAG = "value";
    
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
        
        writer.startNode(CELL_TAG);                
        writer.addAttribute("rIdx", String.valueOf(getRemappedRowIndex(c.getRow())));
        writer.addAttribute("cIdx", String.valueOf(getRemappedColumnIndex(c.getColumn())));
        
        marshalTableElement(c, writer, context, true);
        
        writeNode(c, TableProperty.Units, UNITS_TAG, writer, context);
        writeNode(c, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        
        Class<?> dataType = c.getDataType();
        if (dataType != c.getColumn().getDataType() && dataType != null) {
            writer.startNode(DATATYPE_TAG);  
            context.convertAnother(c.getDataType());
            writer.endNode();
        }
        
        Object cellValue = c.getCellValue();
        if (cellValue != null) {
            writer.startNode(VALUE_TAG);  
            context.convertAnother(cellValue);
            writer.endNode();
        }
        
        writer.endNode();
    }


    @Override
    public Cell unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int rIdx = Integer.valueOf(reader.getAttribute("rIdx"));
        int cIdx = Integer.valueOf(reader.getAttribute("cIdx"));
        
        Column col = t.getColumn(cIdx);
        Cell c = t.getCell(t.getRow(rIdx), t.getColumn(cIdx));
        
        // upon return, we're left at the value tag
        unmarshalTableElement(c, reader, context);
        
        // process units and display format tags, if present
        String strVal;
        String nodeName = reader.getNodeName();
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
        
        // get data type
        Class<?> dataType = col.getDataType();
        if (DATATYPE_TAG.equals(nodeName)) {
            dataType = (Class<?>)context.convertAnother(t, Class.class);
            reader.moveUp();
            
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
        }
        
        // get Cell Value
        if (VALUE_TAG.equals(nodeName)) {
            Object o = context.convertAnother(t, dataType);  
            CellUtils.cellUpdater((CellImpl)c, o);
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
