package org.tms.io;

public class IOFormat
{
    
    private enum FileFormat {
        CSV,
        PDF,
        TMS
    }
    
    public static final IOFormat CSV = new IOFormat(FileFormat.CSV, true, true, false, false);
    public static final IOFormat PDF = new IOFormat(FileFormat.PDF, true, true, false, false);
    
    private FileFormat m_fileFormat;
    private boolean m_rowNames;
    private boolean m_colNames;
    private boolean m_ignoreEmptyRows;
    private boolean m_ignoreEmptyCols;
    
    private IOFormat(final FileFormat format, 
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
    
    public IOFormat withColumnNames()
    {
        return withColumnNames(true);
    }

    public IOFormat withColumnNames(final boolean b)
    {
        return new IOFormat(m_fileFormat, m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }

    public boolean isRowNames()
    {
        return m_rowNames;
    }
    
    public IOFormat withRowNames()
    {
        return withRowNames(true);
    }

    public IOFormat withRowNames(final boolean b)
    {
        return new IOFormat(m_fileFormat, b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }
    
    public boolean isIgnoreEmptyRows()
    {
        return m_ignoreEmptyRows;
    }
    
    public IOFormat withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    public IOFormat withIgnoreEmptyRows(final boolean b)
    {
        return new IOFormat(m_fileFormat, m_rowNames, m_colNames, b, m_ignoreEmptyCols);
    } 
    
    public boolean isIgnoreEmptyColumns()
    {
        return m_ignoreEmptyCols;
    }
    
    public IOFormat withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    public IOFormat withIgnoreEmptyColumns(final boolean b)
    {
        return new IOFormat(m_fileFormat, m_rowNames, m_colNames, m_ignoreEmptyRows, b);
    } 
}
