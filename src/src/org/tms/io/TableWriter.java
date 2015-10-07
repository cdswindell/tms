package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.exceptions.UnimplementedException;
import org.tms.io.options.CSVOptions;
import org.tms.io.options.IOOptions;
import org.tms.tds.TableImpl;

public class TableWriter
{
    private TableImpl m_table;
    private IOOptions m_options;
    private File m_file;
    
    public TableWriter(TableImpl t, String fileName, IOOptions options) 
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
        else
            m_options = options;
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
            case CSV:
                CSVWriter.export(m_table, m_file, (CSVOptions)m_options);
                break;
                
            case PDF:
            case RTF:
            case HTML:
            case DOCX:
                JasperWriter.export(m_table, m_file, m_options);
                break;
                
            default:
                break;
        }        
    }
}
