package org.tms.api.io.options;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.tms.io.options.OptionEnum;

public abstract class IOOptions extends Constants
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
                    case TMS:
                        return TMSOptions.Default;
                        
                    case CSV:
                        return CSVOptions.Default;
                        
                    case HTML:
                        return HTMLOptions.Default;
                        
                    case RTF:
                        return RTFOptions.Default;
                        
                    case PDF:
                        return PDFOptions.Default;
                        
                    case XML:
                        return XMLOptions.Default;
                        
                    case EXCEL:
                        // if xls file, return modified option
                        if ("xls".equalsIgnoreCase(ext))
                            return XlsOptions.Default.withXlsFormat();
                        else
                            return XlsOptions.Default;
                        
                    default:
                        break;
                }
            }
            
            return null;
        }
        else
            return TMSOptions.Default;
    }
    
    private static final Map<String, FileFormat> sf_FileFormatMap = new HashMap<String, FileFormat>();
    
    public static enum FileFormat 
    {
        CSV(true, "csv"),
        WORD(false, "docx", "doc"),
        EXCEL(true, "xlsx", "xls"), 
        HTML(false, "htm", "html"),
        PDF(false, "pdf"),
        JSON(true, "json"),
        RTF(false, "rtf"),
        XML(true, "xml"),
        TMS(true, "tms");
        
        private boolean m_supportsImport;
        private Set<String> m_fileExtensions;
        
        private FileFormat(boolean supportsImport, String... fileExtensions)
        {
            m_supportsImport = supportsImport;
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
    
    abstract public IOOptions withRowNames();
    abstract public IOOptions withRowNames(final boolean b);
    
    abstract public IOOptions withColumnNames();
    abstract public IOOptions withColumnNames(final boolean b);
    
    abstract public IOOptions withIgnoreEmptyRows();
    abstract public IOOptions withIgnoreEmptyRows(final boolean b);
    
    abstract public IOOptions withIgnoreEmptyColumns();
    abstract public IOOptions withIgnoreEmptyColumns(final boolean b);
    
    protected Map<OptionEnum, Object> m_options;
    
    private enum BaseOptions implements OptionEnum {
        FileFormat,
        IsRowNames,
        IsColumnNames,
        IsIgnoreEmptyRows,
        IsIgnoreEmptyColumns;        
    }
    
    protected IOOptions(final FileFormat format, 
                     final boolean rowNames, 
                     final boolean columnNames, 
                     final boolean ignoreEmptyRows,
                     final boolean ignoreEmptyColumns)
    {
        m_options = new HashMap<OptionEnum, Object>();
        
        set(BaseOptions.FileFormat, format);
        set(BaseOptions.IsRowNames, rowNames);
        set(BaseOptions.IsColumnNames, columnNames);
        set(BaseOptions.IsIgnoreEmptyRows, ignoreEmptyRows);
        set(BaseOptions.IsIgnoreEmptyColumns, ignoreEmptyColumns);
    }
    
    protected IOOptions(final IOOptions format)
    {
        m_options = new HashMap<OptionEnum, Object>();
        for (Entry<OptionEnum, Object> e : format.m_options.entrySet()) {
            m_options.put(e.getKey(), e.getValue());
        }        
    }
                  
    public boolean isTMS()
    {
        return FileFormat.TMS == getFileFormat();
    }

    public boolean isCSV()
    {
        return FileFormat.CSV == getFileFormat();
    }

    public boolean isPDF()
    {
        return FileFormat.PDF == getFileFormat();
    }

    public boolean isExcel()
    {
        return FileFormat.EXCEL == getFileFormat();
    }

    public boolean isHtml()
    {
        return FileFormat.HTML == getFileFormat();
    }

    public boolean isRTF()
    {
        return FileFormat.RTF == getFileFormat();
    }

    public FileFormat getFileFormat()
    {
        return (FileFormat)m_options.get(BaseOptions.FileFormat);
    }
    
    public boolean canImport()
    {
        return getFileFormat() != null && getFileFormat().m_supportsImport;
    }
    
    public boolean canExport()
    {
        return true;
    }
    
    protected Object get(OptionEnum key) 
    {
        return m_options.get(key);
    }
    
    protected Object set(OptionEnum key, Object value) 
    {
        Object oldValue = get(key);
        if (value == null)
            m_options.remove(key);
        else
            m_options.put(key, value);
        
        return oldValue;
    }
    
    protected boolean isSet(OptionEnum key)
    {
        return m_options.containsKey(key);
    }
    
    protected boolean isTrue(OptionEnum key) 
    {
        Object value = get(key);
        if (value != null && Boolean.class.isAssignableFrom(value.getClass()))
            return ((Boolean)value).booleanValue();
        else
            return false;
    }
    
    public boolean isColumnNames()
    {
        return isTrue(BaseOptions.IsColumnNames);
    }
    
    protected void setColumnNames(boolean b)
    {
        set(BaseOptions.IsColumnNames, b);
    }
    
    public boolean isRowNames()
    {
        return isTrue(BaseOptions.IsRowNames);
    }
    
    protected void setRowNames(boolean b)
    {
        set(BaseOptions.IsRowNames, b);
    }
    
    public boolean isIgnoreEmptyRows()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyRows);
    }
    
    protected void setIgnoreEmptyRows(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyRows, b);
    }
    
    public boolean isIgnoreEmptyColumns()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyColumns);
    }
    
    protected void setIgnoreEmptyColumns(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyColumns, b);
    }
}
