package org.tms.io.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.exceptions.TableIOException;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.CellImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CellConverter extends BaseConverter
{
    static final public String ELEMENT_TAG = "cell";
    
    static final public String VALUE_TAG = "value";
    static final public String ERROR_MESSAGE_TAG = "error";
    
    /*
     * In order to rebuild a cell, we need access to some methods
     * that are explicitly not available outside of the CellImpl
     * class/package.
     * 
     * We will use Java Reflection to get the method calls we need
     * then mark them as accessible
     */
    static private Method setCellValueNoCheck = null;
    static {
        try
        {
            // setCellValueNoDataTypeCheck
            setCellValueNoCheck = CellImpl.class.getDeclaredMethod("setCellValueNoDataTypeCheck", 
                                                                   new Class<?>[] {Object.class});
            setCellValueNoCheck.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new TableIOException(e);
        }
    }
    
    static private Method setCellErrorMessage = null;
    static {
        try
        {
            setCellErrorMessage = CellImpl.class.getDeclaredMethod("setErrorMessage", 
                                                                   new Class<?>[] {String.class});
            setCellErrorMessage.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new TableIOException(e);
        }
    }
    
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
        return CellImpl.class == arg;
    }

    protected String getElementTag()
    {
        return CellConverter.ELEMENT_TAG;
    }
    
    /**
     * Return true of this cell should be output
     */
    protected boolean isRelevant(Cell c)
    {
    	// don't persist calculated values for external dependence tables
    	if (isExternalDependenceTable()) {
    		Column col = c.getColumn();
    		if (col.isDerived())
    			return false;
    		
    		Row row = c.getRow();
    		if (row.isDerived())
    			return false;
    	}

    	if (c.getCellValue() != null)
    		return true;
    	
        if ((options().isUnits() || options().isVerboseState()) && hasValue(c, TableProperty.Units))
            return true;
        
        if ((options().isDisplayFormats() || options().isVerboseState()) && hasValue(c, TableProperty.DisplayFormat))
            return true;
        
        return super.isRelevant(c);
    }

	@Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        CellImpl c = (CellImpl)arg;
        
        // if the row only has defaults, no need to output it
        if (!isRelevant(c))
            return;
        
        writer.startNode(getElementTag());                
        writer.addAttribute("rIdx", String.valueOf(getRemappedRowIndex(c.getRow())));
        writer.addAttribute("cIdx", String.valueOf(getRemappedColumnIndex(c.getColumn())));
        
        marshalTableElement(c, writer, context, true);
        
        if (options().isUnits() || options().isVerboseState())
        	writeNode(c, TableProperty.Units, UNITS_TAG, writer, context);
        
        if (options().isDisplayFormats() || options().isVerboseState())
        	writeNode(c, TableProperty.DisplayFormat, FORMAT_TAG, writer, context);
        
        if (options().isUUIDs() || options().isVerboseState())
            writeNode(c, TableProperty.UUID, UUID_TAG, writer, context);
        
        Column cCol = c.getColumn();
        Class<?> dataType = c.getDataType();
        if (dataType == null && c.isErrorValue())
            dataType = ErrorCode.class;
        if (dataType == null)
            dataType = cCol.getDataType();
        if (dataType != null && dataType != cCol.getDataType()) {
            writer.startNode(DATATYPE_TAG);  
            context.convertAnother(dataType);
            writer.endNode();
        }
        
        Object cellValue = c.getCellValue();
        if (cellValue != null) {
            writer.startNode(VALUE_TAG);  
            context.convertAnother(c.isErrorValue() ? c.getErrorCode() : cellValue);
            writer.endNode();
            
            if (c.isErrorValue() && c.hasProperty(TableProperty.ErrorMessage)) {
                writer.startNode(ERROR_MESSAGE_TAG);  
                writer.setValue(c.getErrorMessage());
                writer.endNode();
            }                
        }
        
        writer.endNode();
    }

    @Override
    public Cell unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        int rIdx = Integer.valueOf(reader.getAttribute("rIdx"));
        int cIdx = Integer.valueOf(reader.getAttribute("cIdx"));
        
        // check that we don't need to add a new row/col to table
        // to house the concrete (non-external-dependent) cell
		rIdx = getRemappedRowIdx(rIdx, context);
		cIdx = getRemappedColIdx(cIdx, context);
        
        Row row = t.getRow(rIdx);        
        if (row == null)
            row = t.addRow(Access.ByIndex, rIdx);
        
        Column col = t.getColumn(cIdx);
        if (col == null)
            col = t.addColumn(Access.ByIndex, cIdx);
        
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
                    try
                    {
                        setCellValueNoCheck.invoke(c, o);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
                    {
                        throw new TableIOException(e);
                    }
                    reader.moveUp();
                }
                break;
                
                case ERROR_MESSAGE_TAG: 
                {
                    String eMsg = (String)context.convertAnother(t, String.class);  
                    try
                    {
                        setCellErrorMessage.invoke(c, eMsg);
                    }
                    catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e)
                    {
                        throw new TableIOException(e);
                    }
                    reader.moveUp();
                }
                break;
        	}       	
            
            // remember data types
            if (dataType != null)
            	cacheDataType(context, dataType);
            
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
