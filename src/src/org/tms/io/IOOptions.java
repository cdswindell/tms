package org.tms.io;

public class IOOptions
{
    
    private enum FileFormat {
        CSV,
        PDF,
        TMS
    }
    
    public static final IOOptions CSV = new IOOptions(FileFormat.CSV, true, true, false, false, true);
    public static final IOOptions PDF = new IOOptions(FileFormat.PDF, true, true, false, false, true);
    
    private FileFormat m_fileFormat;
    private boolean m_rowNames;
    private boolean m_colNames;
    private boolean m_ignoreEmptyRows;
    private boolean m_ignoreEmptyCols;
    private boolean m_ignoreSuroundingSpaces;
    
    private IOOptions(final FileFormat format, 
                     final boolean rowNames, 
                     final boolean columnNames, 
                     final boolean ignoreEmptyRows,
                     final boolean ignoreEmptyColumns,
                     final boolean ignoreSurroundingSpaces)
    {
        m_fileFormat = format;
        m_rowNames = rowNames;
        m_colNames = columnNames;
        m_ignoreEmptyRows = ignoreEmptyRows;
        m_ignoreEmptyCols = ignoreEmptyColumns;
        m_ignoreSuroundingSpaces = ignoreSurroundingSpaces;
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

    public IOOptions withColumnNames(final boolean b)
    {
        return new IOOptions(m_fileFormat, m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }

    public boolean isRowNames()
    {
        return m_rowNames;
    }
    
    public IOOptions withRowNames()
    {
        return withRowNames(true);
    }

    public IOOptions withRowNames(final boolean b)
    {
        return new IOOptions(m_fileFormat, b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }
    
    public boolean isIgnoreEmptyRows()
    {
        return m_ignoreEmptyRows;
    }
    
    public IOOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    public IOOptions withIgnoreEmptyRows(final boolean b)
    {
        return new IOOptions(m_fileFormat, m_rowNames, m_colNames, b, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    } 
    
    public boolean isIgnoreEmptyColumns()
    {
        return m_ignoreEmptyCols;
    }
    
    public IOOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    public IOOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new IOOptions(m_fileFormat, m_rowNames, m_colNames, m_ignoreEmptyRows, b, m_ignoreSuroundingSpaces);
    } 
    
    public boolean isIgnoreSuroundingSpaces()
    {
        return m_ignoreSuroundingSpaces;
    }
    
    public IOOptions withIgnoreSuroundingSpaces()
    {
        return withIgnoreSuroundingSpaces(true);
    }

    public IOOptions withIgnoreSuroundingSpaces(final boolean b)
    {
        return new IOOptions(m_fileFormat, m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, b);
    } 
}
