package org.tms.io.xml.logs;

import java.io.File;

import org.tms.api.TableElement;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.logs.LogFileFormat;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.xml.ExternalDependenceTableConverter;
import org.tms.tds.TableImpl;
import org.tms.tds.logs.LogsTableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LogsTableConverter extends ExternalDependenceTableConverter
{
    static final public String ELEMENT_TAG = "logsTable";
    
    public LogsTableConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public LogsTableConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return LogsTableImpl.class == arg;
    }

    @Override
    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
    	LogsTableImpl lTe = (LogsTableImpl)te;
        
        super.marshalClassSpecificElements(te, writer, context);
        
        writeNode(lTe.getLogFile().getPath(), "logFile", writer, context);
        writeNode(lTe.getLogFileFormat().getClass().getName(), "formatClass", writer, context);
        writeNode(lTe.getLogFileFormat(), "format", writer, context);
    }
    
    @Override
    protected TableImpl createTable(HierarchicalStreamReader reader, UnmarshallingContext context) 
    {
		File logFile = null;
		Class<?> logFileFormatClass = null;
		LogFileFormat logFileFormat = null;
		
		String tmp = null;
		int processingLogsTags = 3;
    	while (processingLogsTags > 0 && reader.hasMoreChildren()) {
    		reader.moveDown();
    		String nodeName = reader.getNodeName();
    		switch (nodeName) {
				case "logFile":
					tmp = (String)context.convertAnother(null, String.class);
					logFile = new File(tmp);
					processingLogsTags--;
					break;
					
				case "formatClass":
					tmp = (String)context.convertAnother(null, String.class);
	                try
	                {
	                	logFileFormatClass = (Class<?>) Class.forName(tmp).newInstance().getClass();
	                } 
	                catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						throw new TableIOException(e);
					}
					processingLogsTags--;
					break;
					
				case "format":
					logFileFormat = (LogFileFormat)context.convertAnother(null, logFileFormatClass);
					processingLogsTags--;
					break;
					
			    default:
			    	processingLogsTags = 0;
			    	break;
    		}

            reader.moveUp();
    	}
  
    	// we need a file and a file format to proceed
		if (logFile == null)
			throw new TableIOException("Log File Required");
		
		if (logFileFormat == null)
			throw new TableIOException("Log File Format Required");

    	try {
			LogsTableImpl t = (LogsTableImpl) TableFactory.createLogsTable(logFile, logFileFormat, getTableContext());
			cacheDimensions(t);
			return t;
		} 
    	catch (Exception e) {
			throw new TableIOException(e);
		} 
    }
}
