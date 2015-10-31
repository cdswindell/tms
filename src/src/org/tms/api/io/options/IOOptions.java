package org.tms.api.io.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.tms.io.options.IOConstants;
import org.tms.io.options.IOFileFormat;
import org.tms.io.options.OptionEnum;

public abstract class IOOptions extends IOConstants
{      
    
    abstract public IOOptions withRowNames();
    abstract public IOOptions withRowNames(final boolean b);
    
    abstract public IOOptions withColumnNames();
    abstract public IOOptions withColumnNames(final boolean b);
    
    abstract public IOOptions withIgnoreEmptyRows();
    abstract public IOOptions withIgnoreEmptyRows(final boolean b);
    
    abstract public IOOptions withIgnoreEmptyColumns();
    abstract public IOOptions withIgnoreEmptyColumns(final boolean b);
    
    protected Map<OptionEnum, Object> m_options;
    
    private enum BaseOptions implements OptionEnum 
    {
        FileFormat,
        IsRowNames,
        IsColumnNames,
        IsIgnoreEmptyRows,
        IsIgnoreEmptyColumns;        
    }
    
    protected IOOptions(final IOFileFormat format, 
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
        return IOFileFormat.TMS == getFileFormat();
    }

    public boolean isCSV()
    {
        return IOFileFormat.CSV == getFileFormat();
    }

    public boolean isPDF()
    {
        return IOFileFormat.PDF == getFileFormat();
    }

    public boolean isExcel()
    {
        return IOFileFormat.EXCEL == getFileFormat();
    }

    public boolean isHtml()
    {
        return IOFileFormat.HTML == getFileFormat();
    }

    public boolean isRTF()
    {
        return IOFileFormat.RTF == getFileFormat();
    }

    public IOFileFormat getFileFormat()
    {
        return (IOFileFormat)m_options.get(BaseOptions.FileFormat);
    }
    
    public boolean canImport()
    {
        return getFileFormat() != null && getFileFormat().isSupportsImport();
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
