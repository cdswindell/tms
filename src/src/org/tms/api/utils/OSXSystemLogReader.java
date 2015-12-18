package org.tms.api.utils;

import java.util.Date;

import org.tms.api.io.logs.LogFileFormat;

public class OSXSystemLogReader implements LogFileFormat 
{
	private String[] m_fieldNames = {"Timestamp", "System", "Source", "PID", "Severity", "Message"};
	private Class<?>[] m_fieldTypes = {Date.class, String.class, String.class, int.class, String.class, String.class};

	@Override
	public int getNumFields() 
	{
		return m_fieldNames.length;
	}

	@Override
	public String[] getFieldNames() 
	{
		return m_fieldNames;
	}

	@Override
	public Class<?>[] getFieldDataTypes() 
	{
		return m_fieldTypes;
	}

	@Override
	public Object[] getFieldValues(String logFileLine) 
	{
		return null;
	}

}
