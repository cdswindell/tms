package org.tms.tds.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.api.io.logs.LogFileFormat;
import org.tms.tds.ContextImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;
import org.tms.util.WeakHashSet;

public class LogsTableImpl extends TableImpl
{
    public static final LogsTableImpl createTable(File logFile, LogFileFormat lff) 
    throws IOException
    {
        return new LogsTableImpl(logFile, lff, ContextImpl.fetchDefaultContext());
    }
    
    public static final LogsTableImpl createTable(File logFile, LogFileFormat lff, ContextImpl tc) 
    throws IOException
    {
        return new LogsTableImpl(logFile, lff, tc);
    }

    private int m_numLogsCols;
    private int m_numLogsRows;
    
    private Set<LogsRowImpl> m_unprocessedRows;
    private boolean m_processingLogFile= false;
    
	private File m_logFile;
    private LogFileFormat m_logFileFormat;
	private LineNumberReader m_logFileReader;
	private FileInputStream m_logFileInputStream;
    
    public LogsTableImpl(File logFile, LogFileFormat lff, ContextImpl tc) 
    throws IOException
    {
        // initialize the default table object
        super(tc.getRowCapacityIncr(), tc.getColumnCapacityIncr(), tc);
        m_numLogsCols = m_numLogsRows = 0;
        
    	if (logFile == null)
    		throw new IllegalArgumentException("File required");
    	
    	if (!logFile.exists())
    		throw new FileNotFoundException("File not found: " + logFile.getPath());
    	
    	if (!logFile.canRead())
    		throw new IllegalArgumentException(logFile.getPath() + " cannot be opened for read access");
    	
        m_logFile = logFile;
        m_logFileFormat = lff;
        
        processLogFile(false);
    }
    
    public int getNumLogsColumns()
    {
        return m_numLogsCols;
    }
    
    public int getNumLogsRows()
    {
        return m_numLogsRows;
    }
    
    synchronized public void refresh() 
    {
    	try {
			processLogFile(true);
		} 
    	catch (IOException e) { /* noop */}
    }
    
    /**
     * Remove the specified row from the set of unprocessed rows and release file resources
     * if all rows have been processed
     * @param row the row to removed from the unprocessed set
     */
    synchronized protected void removeLogsRowFromUnprocessed(LogsRowImpl row)
    {
        // exit if we're processing a result set, rows are deleted  in this case
        if (m_processingLogFile)
            return;
        
        if (row != null && m_unprocessedRows != null) {
            m_unprocessedRows.remove(row);
            if (m_unprocessedRows.isEmpty())
                releaseResources();
        }       
    }
    
    synchronized protected int decrementNumLogsRows()
    {
        // skip operation if we're processing a result set
        if (m_processingLogFile)
            return m_numLogsRows;
        
        if (--m_numLogsRows < 0)
            m_numLogsRows = 0;
        
        return m_numLogsRows;
    }
    
    private void processLogFile(boolean isRefresh) 
    throws IOException 
    {
    	m_processingLogFile = true;        
        try {
            releaseResources();
            
            // delete existing rows
            if (isRefresh) {
                if (m_unprocessedRows != null) {
                    this.delete(m_unprocessedRows.toArray(new Row [] {}));
                    m_unprocessedRows = null;
                }
                    
                Set<Row> toDeleteRows = new HashSet<Row>(getNumRows());            
                for (RowImpl row : getRowsInternal()) {
                    if (row instanceof LogsRowImpl)
                        toDeleteRows.add(row);
                }
                
                this.delete(toDeleteRows.toArray(new Row [] {}));
                m_numLogsRows = 0;
            }
            else
                m_numLogsCols = m_numLogsRows = 0;
            
            // open log file for read access
            m_logFileInputStream = new FileInputStream(m_logFile);
            BufferedReader fr = new BufferedReader(new InputStreamReader(m_logFileInputStream));
            m_logFileReader = new LineNumberReader(fr);
            
            // create row data structures
            m_numLogsRows = getLogsRowCount();
            
            setRowsCapacity(calcRowsCapacity(Math.max(m_numLogsRows, getNumRows())));
            m_unprocessedRows = new WeakHashSet<LogsRowImpl>(m_numLogsRows);
            for (int i = 1; i <= m_numLogsRows; i++) {
                LogsRowImpl row = new LogsRowImpl(this, i);
                m_unprocessedRows.add(row);
                add(row, false, false, Access.ByIndex, i);
            }
           
            // process column information
            if (!isRefresh) {
            	m_numLogsCols = m_logFileFormat.getNumFields();
                setColumnsCapacity(calcColumnsCapacity(m_numLogsCols));
                
            	String [] fieldNames = m_logFileFormat.getFieldNames();
            	Class<?> [] dataTypes = m_logFileFormat.getFieldDataTypes();
            	
            	for (int i = 0; i < m_numLogsCols; i++) {
            		LogsColumnImpl c = new LogsColumnImpl(this, i, fieldNames[i], dataTypes[i]);
                    add(c, false, false, Access.ByIndex, i+1);
            	}
            }
            
            // if we're refreshing, recalculate all derivations
            if (isRefresh)
                recalculate();
        }
        finally {
        	m_processingLogFile = false;
        }
    }

    private int getLogsRowCount() 
    throws IOException
    {
        int totalRows = 0;
        long totalFileSize = m_logFile.length();
        
        m_logFileReader.skip(totalFileSize + 1);
        totalRows = m_logFileReader.getLineNumber();
        
        // Reset Stream and line number;
        m_logFileInputStream.getChannel().position(0);
        m_logFileReader.setLineNumber(0);
        
        return totalRows ;
    }

    private void releaseResources()
    {
    	if (m_logFileReader != null) {
    		try {
				m_logFileReader.close();
			} catch (IOException e) { /* noop */ }
    		m_logFileReader = null;
    	}
    	
    	if (m_logFileInputStream != null) {
    		try {
    			m_logFileInputStream.close();
			} catch (IOException e) { /* noop */ }
    		m_logFileInputStream = null;
    	}

    }
    
    @Override
    protected void delete(boolean compress)
    {
        try {
            super.delete(compress);
            m_unprocessedRows = null;
        }
        finally {
            releaseResources();
        }
    }
    
    @Override
    public void finalize() 
    {
        try {
            super.finalize();   
            m_unprocessedRows.clear();
            m_unprocessedRows = null;
        }
        finally {
            releaseResources();
        }
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a log file element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
}
