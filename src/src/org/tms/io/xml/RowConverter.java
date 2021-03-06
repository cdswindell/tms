package org.tms.io.xml;

import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RowConverter extends BaseConverter
{
    static final public String ELEMENT_TAG = "row";
    
    public RowConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    public RowConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return RowImpl.class == arg;
    }

    protected String getElementTag()
    {
        return RowConverter.ELEMENT_TAG;
    }
    
    /**
     * Return true of this row should be output
     */
    protected boolean isRelevant(Row r)
    {
        if ((options().isUnits() || options().isVerboseState()) && hasValue(r, TableProperty.Units))
            return true;
        
        if ((options().isDisplayFormats() || options().isVerboseState()) && hasValue(r, TableProperty.DisplayFormat))
            return true;
        
        if (r.getTable() != null && r.getTable() instanceof ExternalDependenceTableElement && !(r instanceof ExternalDependenceTableElement))
        	return true;
        
        return super.isRelevant(r);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        RowImpl r = (RowImpl)arg;
        
        // if the row only has defaults, no need to output it
        if (!isRelevant(r))
            return;
        
        writer.startNode(getElementTag());                
        writer.addAttribute(INDEX_ATTR, String.valueOf(getRemappedRowIndex(r)));
        
        if (options().isVerboseState() || options().isTimeSeries()) {
	        if (r == context.get(TMS_TS_COLS_TS_ROW_KEY))
	            writer.addAttribute(TS_COLS_TS_ROW_ATTR, "true");
        }
        
        marshalTableElement(r, writer, context, options().isRowLabels());
        
        if (options().isUnits())
            writeNode(r, TableProperty.Units, UNITS_TAG, writer, context);
        
        if (options().isDisplayFormats())
            writeNode(r, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        
        writer.endNode();
    }

    @Override
    public Row unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        
        int rIdx = Integer.valueOf(reader.getAttribute(INDEX_ATTR));
        Boolean isTSRow = readAttributeBoolean(TS_COLS_TS_ROW_ATTR, reader);
        
        Row r = t.addRow(getRemappedRowIdx(rIdx, context));
        postConstructAction(t, r, context);
        
        // Time Series support
        if (options().isVerboseState() || options().isTimeSeries()) {
	        if (isTSRow != null && isTSRow.booleanValue())
	        	context.put(TMS_TS_COLS_TS_ROW_KEY, r);
        }
        
        // upon return, we are left in the Columns or Cells tag
        unmarshalTableElement(r, options().isRowLabels(), reader, context);        
        String nodeName = reader.getNodeName();
        
        String strVal;
        while (true) {
            if (ELEMENT_TAG.equals(nodeName)) 
            	return r;
            
        	switch (nodeName) {
        		case UNITS_TAG:
        		{
		            if (options().isUnits()) {
		                strVal = reader.getValue();
		                if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                    r.setUnits(strVal);
		            }
		            
		            reader.moveUp();
        		}
        		break;
		            	        
        		case FORMAT_TAG:
        		{
		            if (options().isDisplayFormats()) {
		                strVal = reader.getValue();
		                if (strVal != null && (strVal = strVal.trim()).length() > 0)
		                    r.setDisplayFormat(strVal);
		            }
		            reader.moveUp();
		        }
        		break;
        		
        		default:
        			System.out.println("Unhandled Row Tag: " + nodeName);
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
        
        return r;
    }        
}
