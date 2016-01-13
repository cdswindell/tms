package org.tms.api.io.logs;

import java.io.Serializable;

public interface LogFileFormat extends Serializable
{
	public int getNumFields();	
	public String [] getFieldNames();	
	public Class<?> [] getFieldDataTypes();	
	public Object[] getFieldValues(String logFileLine);
}
