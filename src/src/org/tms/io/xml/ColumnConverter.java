package org.tms.io.xml;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ExternalDependenceTableElement;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ColumnConverter extends BaseConverter
{
    static final public String ELEMENT_TAG = "column";
    
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
        return ColumnImpl.class == arg;
    }

    protected String getElementTag()
    {
        return ColumnConverter.ELEMENT_TAG;
    }
    
    /**
     * Return true of this row should be output
     */
    protected boolean isRelevant(Column c)
    {
        if ((options().isUnits() || options().isVerboseState()) && hasValue(c, TableProperty.Units))
            return true;
        
        if ((options().isDisplayFormats() || options().isVerboseState()) && hasValue(c, TableProperty.DisplayFormat))
            return true;
        
        if (hasValue(c, TableProperty.DataType))
            return true;
        
        if (c.getTable() != null && c.getTable() instanceof ExternalDependenceTableElement && !(c instanceof ExternalDependenceTableElement))
        	return true;
        
        return super.isRelevant(c);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        ColumnImpl c = (ColumnImpl)arg;
        
        // if the col only has defaults, no need to output it
        if (!isRelevant(c))
            return;
        
        writer.startNode(getElementTag());                
        writer.addAttribute(INDEX_ATTR, String.valueOf(getRemappedColumnIndex(c)));
        
        if (options().isVerboseState() || options().isTimeSeries()) {
	        if (c == context.get(TMS_TS_ROWS_TS_COL_KEY))
	            writer.addAttribute(TS_ROWS_TS_COL_ATTR, "true");
        }
        
        marshalTableElement(c, writer, context, options().isColumnLabels());
        
        if (options().isUnits())
            writeNode(c, TableProperty.Units, UNITS_TAG, writer, context);
        
        if (options().isDisplayFormats())
            writeNode(c, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        
        // we always want to output this, not optional
        writeNode(c, TableProperty.DataType, DATATYPE_TAG, writer, context);
        
        writer.endNode();
    }

    @Override
    public Column unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        
        int cIdx = Integer.valueOf(reader.getAttribute(INDEX_ATTR));
        Boolean isTSCol = readAttributeBoolean(TS_ROWS_TS_COL_ATTR, reader);
        
        Column c = t.addColumn(getRemappedColIdx(cIdx, context));
        postConstructAction(t, c, context);
        
        // Time Series support
        if (options().isVerboseState() || options().isTimeSeries()) {
	        if (isTSCol != null && isTSCol.booleanValue())
	        	context.put(TMS_TS_ROWS_TS_COL_KEY, c);
        }
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(c, options().isColumnLabels(), reader, context);        
        String nodeName = reader.getNodeName();
        
        String strVal;
        while (true) {
            if (ELEMENT_TAG.equals(nodeName)) 
            	return c;
            
        	switch (nodeName) {
	        	case UNITS_TAG: 
	        	{
		            if (options().isUnits()) {
		                strVal = reader.getValue();
		                if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                    c.setUnits(strVal);
		            }
		            
		            reader.moveUp();		            
		        }
	        	break;
		        
	        	case FORMAT_TAG: 
	        	{
		            if (options().isDisplayFormats()) {
		                strVal = reader.getValue();
		                if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                    c.setDisplayFormat(strVal);
		            }
		            
		            reader.moveUp();
		        }
	        	break;
		        
	        	case DATATYPE_TAG: 
	        	{
		            Class<?>dataType = (Class<?>)context.convertAnother(t, Class.class);
		            if (dataType != null) {
		                c.setDataType(dataType);
		                cacheDataType(context, dataType);
		            }
		            
		            reader.moveUp();
		        }
	        	break;
	        	
        		default:
        			System.out.println("Unhandled Column Tag: " + nodeName);
		            reader.moveUp();
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

