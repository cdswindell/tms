package org.tms.io;

public abstract class IOOptions
{  
    protected enum FileFormat {
        CSV,
        PDF,
        TMS
    }
    
    abstract public IOOptions withRowNames(final boolean b);
    abstract public IOOptions withColumnNames(final boolean b);
    abstract public IOOptions withIgnoreEmptyRows(final boolean b);
    abstract public IOOptions withIgnoreEmptyColumns(final boolean b);
    
    private FileFormat m_fileFormat;
    protected boolean m_rowNames;
    protected boolean m_colNames;
    protected boolean m_ignoreEmptyRows;
    protected boolean m_ignoreEmptyCols;
    
    protected IOOptions(final FileFormat format, 
                     final boolean rowNames, 
                     final boolean columnNames, 
                     final boolean ignoreEmptyRows,
                     final boolean ignoreEmptyColumns)
    {
        m_fileFormat = format;
        m_rowNames = rowNames;
        m_colNames = columnNames;
        m_ignoreEmptyRows = ignoreEmptyRows;
        m_ignoreEmptyCols = ignoreEmptyColumns;
    }
    
    public boolean isTMS()
    {
        return m_fileFormat == FileFormat.TMS;
    }

    public boolean isCSV()
    {
        return m_fileFormat == FileFormat.CSV;
    }

    public boolean isColumnNames()
    {
        return m_colNames;
    }
    
    public IOOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    public boolean isRowNames()
    {
        return m_rowNames;
    }
    
    public IOOptions withRowNames()
    {
        return withRowNames(true);
    }

    public boolean isIgnoreEmptyRows()
    {
        return m_ignoreEmptyRows;
    }
    
    public IOOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    public boolean isIgnoreEmptyColumns()
    {
        return m_ignoreEmptyCols;
    }
    
    public IOOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }
}
