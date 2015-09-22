package org.tms.io;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class IOOptions
{      
    public static IOOptions generateOptionsFromFileExtension(File file)
    {
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');
        if (idx >= -1) {
            String ext = fileName.substring(idx + 1).trim().toLowerCase();
            FileFormat fmt = sf_FileFormatMap.get(ext);
            if (fmt != null) {
                switch (fmt) {
                    case CSV:
                        return CSVOptions.CSV;
                        
                    case TMS:
                        return TMSOptions.TMS;
                        
                    default:
                        break;
                }
            }
            
            return null;
        }
        else
            return TMSOptions.TMS;
    }
    
    private static final Map<String, FileFormat> sf_FileFormatMap = new HashMap<String, FileFormat>();
    
    protected static enum FileFormat 
    {
        CSV("csv"),
        PDF("pdf"),
        Excel("xls", "xlsx"), 
        TMS("tms");
        
        private Set<String> m_fileExtensions;
        
        private FileFormat(String... fileExtensions)
        {
            m_fileExtensions = new HashSet<String>();
            if (fileExtensions != null) {
                for (String s : fileExtensions) {
                    String ext = s.trim().toLowerCase();
                    if (m_fileExtensions.add(ext))
                        sf_FileFormatMap.put(ext, this);                    
                }
            }
        }
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

    public FileFormat getFileFormat()
    {
        return m_fileFormat;
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
