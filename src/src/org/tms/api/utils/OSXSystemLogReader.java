package org.tms.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.tms.api.io.logs.LogFileFormat;

public class OSXSystemLogReader implements LogFileFormat 
{
	private static final long serialVersionUID = 5847443802414685349L;
	
	private String[] m_fieldNames = {"Timestamp", "System", "Source", "PID", "Severity", "Message"};
	private Class<?>[] m_fieldTypes = {Date.class, String.class, String.class, int.class, String.class, String.class};
	
	private SimpleDateFormat m_logDateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");

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
		Object [] values = new Object[getNumFields()];
		
		try {
			String dateStr = logFileLine.substring(0, 15);
			Date theDate = m_logDateFormat.parse(dateStr);
			
			Calendar c = Calendar.getInstance();
			int theYear = c.get(Calendar.YEAR);
			c.setTime(theDate);
			c.set(Calendar.YEAR, theYear);
			values[0]  = c.getTime();
		}
		catch (ParseException pe) {
			// todo: HANDLE
		}
		
		logFileLine = logFileLine.substring(16).trim();
		int nextSpace = logFileLine.indexOf(' ');
		String system = logFileLine.substring(0, nextSpace);
		values[1] = system;
		
		logFileLine = logFileLine.substring(system.length() + 1).trim();
		int nextSep = logFileLine.indexOf('[');
		String source = logFileLine.substring(0, nextSep).trim();
		values[2] = source;
		
		logFileLine = logFileLine.substring(nextSep + 1).trim();
		nextSep = logFileLine.indexOf(']');
		int pid = Integer.parseInt(logFileLine.substring(0, nextSep).trim());
		values[3] = pid;

		logFileLine = logFileLine.substring(nextSep + 1).trim();
		nextSep = logFileLine.indexOf('<');
		logFileLine = logFileLine.substring(nextSep + 1).trim();
		nextSep = logFileLine.indexOf('>');
		String severity = logFileLine.substring(0, nextSep).trim();
		values[4] = severity;

		logFileLine = logFileLine.substring(nextSep + 1).trim();
		nextSep = logFileLine.indexOf(':');
		values[5] = logFileLine.substring(nextSep + 1).trim();
		
		return values;
	}
}
