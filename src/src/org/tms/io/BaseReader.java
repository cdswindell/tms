package org.tms.io;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.io.BaseIOOption;

abstract class BaseReader<E extends BaseIOOption<?>> extends BaseIO
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
        return m_options.isRowLabels();
    }
    
    /**
     * Return {@code true} if the Default file contains column names.
     * @return true if the Default file contains column names
     */
    public boolean isColumnNames()
    {
        return m_options.isColumnLabels();
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
