package org.tms.io;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.io.options.ArchivalIOOptions;

abstract class ArchivalWriter<T extends ArchivalIOOptions<T>> extends BaseWriter<T>
{  
    public ArchivalWriter(TableExportAdapter tea, OutputStream out, T options)
    {
        super(tea, out, options);       
    }  
    
	protected List<TableProperty> getExportableProperties(ElementType et) 
	{
		List<TableProperty> eProps;
		if (options().isVerboseState())
			eProps = et.getMutableProperties();
		else {
			eProps = new ArrayList<TableProperty>();
			eProps.add(TableProperty.Label);
			
			if (options().isDescriptions()) 
				eProps.add(TableProperty.Description);
			
			if (options().isDisplayFormats()) 
				eProps.add(TableProperty.DisplayFormat);
			
			if (options().isUnits()) 
				eProps.add(TableProperty.Units);
			
			if (options().isTags()) 
				eProps.add(TableProperty.Tags);
		}
    	
		if (options().isUUIDs()) 
			eProps.add(TableProperty.UUID);
		
		if (et == ElementType.Table) {
			eProps.add(TableProperty.numRows);
			eProps.add(TableProperty.numColumns);
		}
		
		return eProps;
	}
	
	protected Object getProperty(TableElement te, TableProperty tp) 
	{
		Object val = te.getProperty(tp);
		if (val != null) {
			switch (tp) {			
				case DataType:
					if (te.getElementType() == ElementType.Cell && !((Cell)te).isEnforceDataType())
						val = null;
					break;
					
				case numRows:
					val = this.getNumConsumableRows();
					break;
					
				case numColumns:
					val = this.getNumConsumableColumns();
					break;
				
				default:
					break;				
			}
		}
		
		return val;
	}

    protected boolean hasValue(TableElement te, TableProperty key)
    {
        if (te.hasProperty(key)) {
            Object val = getProperty(te, key);
            
            if (val != null) {
	            // one more check for empty strings
	            if (val instanceof String && ((String)val).trim().length() == 0)
	                return false;
	               
	            // if te isn't a table, check that value differs from parent table
	            if (te.getElementType() != ElementType.Table) {
	            	if (hasValue(te.getTable(), key) && te.getTable().getProperty(key) == val)
	            		return false;
	            }
	            
	            return true;
	        }
        }
        
        return false;        
    }  
}
