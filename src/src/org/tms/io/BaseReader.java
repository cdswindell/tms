package org.tms.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.io.IOOption;

abstract public class BaseReader<E extends IOOption<?>> extends BaseIO
{
    protected static final InputStream makeInputStream(File inputFile)
    {
        try
        {
            return new FileInputStream(inputFile);
        }
        catch (FileNotFoundException e)
        {
            throw new TableIOException(e);
        }
    }
    
    private File m_inputFile;
    private E m_options;
    private TableContext m_context;
    private InputStream m_inputStream;
    
    BaseReader(File inputFile, TableContext context, E options) 
    {
        this(makeInputStream(inputFile), context, options);
        
        m_inputFile = inputFile;
    }
    
    public BaseReader(InputStream in, TableContext context, E options)
    {
        if (options == null)
            throw new IllegalArgumentException("Options required");
        
        m_inputStream = in;
        m_context = context;
        m_options = options;
    }

    public E options()
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

    public InputStream getInputStream()
    {
        return m_inputStream;
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
     * Prune empty columns from imported table, if so directed
     * @param t
     */
    protected void pruneEmptyColumns(Table t)
    {
        if (options().isIgnoreEmptyColumns()) {
            Set<Column> emptyCols = null;
            for (Column c : t.getColumns()) {
                if (c != null && c.isNull()) {
                    if (emptyCols == null)
                        emptyCols = new HashSet<Column>();
                    emptyCols.add(c);
                }
            }
            
            if (emptyCols != null)
                t.delete(emptyCols.toArray(new Column [] {}));
        }
    }
}
