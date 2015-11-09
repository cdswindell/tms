package org.tms.io.xml;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
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
    static final public String ELEMENT_TAG = "cell";
    
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

    protected String getElementTag()
    {
        return CellConverter.ELEMENT_TAG;
    }
    
    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        CellImpl c = (CellImpl)arg;
        
        writer.startNode(getElementTag());                
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
        if (col == null)
            col = t.addColumn(Access.ByIndex, cIdx);
        
        Row row = t.getRow(rIdx);        
        if (row == null)
            row = t.addRow(Access.ByIndex, rIdx);
        
        Cell c = t.getCell(row, col);
        
        // upon return, we're left at the value tag
        unmarshalTableElement(c, true, reader, context);
        String nodeName = reader.getNodeName();
        
        // get data type
        Class<?> dataType = col.getDataType();
        String strVal;
        while (true) {
            if (ELEMENT_TAG.equals(nodeName)) 
            	return c;
            
        	switch (nodeName) {
	        	case UNITS_TAG: 
	        	{
		            strVal = reader.getValue();
		            if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                c.setUnits(strVal);
		            reader.moveUp();
	        	}
	        	break;
        
	        	case FORMAT_TAG: 
	        	{
		            strVal = reader.getValue();
		            if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                c.setDisplayFormat(strVal);
		            reader.moveUp();
	        	}
	        	break;
        
	        	case DATATYPE_TAG: 
	        	{
		            dataType = (Class<?>)context.convertAnother(t, Class.class);
		            reader.moveUp();
		        }
	        	break;
        
	        	case VALUE_TAG: 
	        	{
		            Object o = context.convertAnother(t, dataType);  
		            CellUtils.cellUpdater((CellImpl)c, o);
		            reader.moveUp();
	        	}
	        	break;
        	}
        	
            // check next tag
            if (reader.hasMoreChildren()) {
                reader.moveDown();
                nodeName = reader.getNodeName();
            }
            else 
            	break;
        }
        
        return c;
    }        
}
