package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.io.options.CSVOptions;
import org.tms.api.io.options.IOOptions;
import org.tms.api.io.options.TMSOptions;
import org.tms.api.io.options.TitledPageOptions;
import org.tms.api.io.options.XlsOptions;

public class TableExportAdapter
{
    private Table m_table;
    private IOOptions m_options;
    private File m_file;
    private OutputStream m_output;
    private boolean m_isFileBased;
    
    public TableExportAdapter(Table t, String fileName, IOOptions options) 
    throws IOException
    {
        if (t == null)
            throw new IllegalArgumentException("Table required");
        
        if (fileName == null || (fileName = fileName.trim()).length() <= 0)
            throw new IllegalArgumentException("File name required");
        
        m_table = t;
        m_file = new File(fileName);
        
        // check if file can be written
        if (!canWrite())
                throw new IOException("Cannot write to " + fileName);
        
        // select default options, if none provided
        if (options == null) {
            m_options = IOOptions.generateOptionsFromFileExtension(m_file);
            
            if (m_options == null)
                throw new UnimplementedException(String.format("No support for writting %s", fileName));
        }
        else {
            m_options = options;
        }
        
        // create the output stream
        m_output = new FileOutputStream(m_file);
        
        // indicate that this writer is based on a file, output stream should be closed
        m_isFileBased = true;
    }

    public TableExportAdapter(Table t, OutputStream out, IOOptions options) 
    throws IOException
    {
        if (options == null)
            throw new UnimplementedException("Options required");
        
        m_table = t;
        m_output = out;
        m_options = options;
        m_isFileBased = false;
    }
    
    private boolean canWrite()
    {
        if (m_file.exists())
            return m_file.canWrite();
          
        // have to try to create the file; if we can, then delete it right away
        try
        {
            m_file.createNewFile();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
        finally {
            if (m_file.exists())
                m_file.delete();
        }
    }

    public void export() 
    throws IOException
    {
        switch (m_options.getFileFormat()) {
            case TMS:
                TMSWriter.export(this, m_output, (TMSOptions)m_options);
                break;
                
            case CSV:
                CSVWriter.export(this, m_output, (CSVOptions)m_options);
                break;
                
            case EXCEL:
                XlsWriter.export(this, m_output, (XlsOptions)m_options);
                break;
                
            case PDF:
            case RTF:
            case HTML:
            case WORD:
                JasperWriter.export(this, m_output, (TitledPageOptions<?>)m_options);
                break;
                
            default:
                break;
        }
        
        if (m_isFileBased && m_output != null) {
            m_output.flush();
            m_output.close();
        }
    }

    public int getNumColumns()
    {
        return m_table.getNumColumns();
    }
    
    public List<Column> getColumns()
    {
        return m_table.getColumns();
    }

    public int getNumRows()
    {
        return m_table.getNumRows();
    }
    
    public List<Row> getRows()
    {
        return m_table.getRows();
    }

    public Table getTable()
    {
        return m_table;
    }

    public Row getRow(int rowIndex)
    {
        List<Row> rows = getRows();
        if (rowIndex >= 1 && rowIndex <= rows.size())
            return rows.get(rowIndex - 1);
        else
            return null;
    }
}
