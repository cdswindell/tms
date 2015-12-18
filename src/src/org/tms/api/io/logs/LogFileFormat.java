package org.tms.api.io.logs;

public interface LogFileFormat 
{
	public int getNumFields();	
	public String [] getFieldNames();	
	public Class<?> [] getFieldDataTypes();	
	public Object[] getFieldValues(String logFileLine);
}
