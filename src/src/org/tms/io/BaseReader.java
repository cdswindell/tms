package org.tms.io;

import java.io.File;

import org.tms.api.TableContext;
import org.tms.io.options.IOOptions;

abstract class BaseReader<E extends IOOptions> extends BaseIO
{
    private File m_inputFile;
    private E m_options;
    private TableContext m_context;
    
    BaseReader(File inputFile, TableContext context, E options) 
    {
        if (options == null)
            throw new IllegalArgumentException("Options required");
        
        m_inputFile = inputFile;
        m_context = context;
        m_options = options;
    }
    
    protected E options()
    {
        return m_options;
    }
    
    public TableContext getTableContext()
    {
        return m_context;
    }
    
    /**
     * Return the {@link java.io.File} to parse.
     * @return the {@link java.io.File} to parse
     */
    public File getInputFile()
    {
        return m_inputFile;
    }

    /**
     * Return the file name to parse.
     * @return the file name to parse
     */
    public String getFileName()
    {
        return m_inputFile.getName();
    }

    /**
     * Return {@code true} if the Default file contains row names.
     * @return true if the Default file contains row names
     */
    public boolean isRowNames()
    {
        return m_options.isRowNames();
    }
    /**
     * Return {@code true} if the Default file contains column names.
     * @return true if the Default file contains column names
     */
    public boolean isColumnNames()
    {
        return m_options.isColumnNames();
    }
}
