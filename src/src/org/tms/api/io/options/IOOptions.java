package org.tms.api.io.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.tms.io.options.OptionEnum;

/**
 * The base class for all {@code IOOptions}, which facilitate the import and export of 
 * {@link org.tms.api.Table Table}s to other file formats. 
 * {@code IOOptions} and its super-classes support methods that determine what and how TMS data
 * is imported and exported. For example, methods exist to set output titles and font sizes, for
 * formats that support titles.
 * {@code IOOptions} utilize the Builder design pattern, meaning that
 * methods that modify instance objects all return a new {@code IOOptions} instance.
 * This allows different methods to be chained together, such as:
  * <blockquote><pre>
 *     PDFOptions.Default.withRowNames(false).withDescriptions(false).withTitle("Active Compounds")
 * </pre></blockquote>
 * Each concrete class implementing {@code IOOptions} or one of its super-classes defines
 * a {@code public static} instance named {@code Default} which can be further modified, as needed.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public abstract class IOOptions
{          
    /**
     * Returns a new {@link IOOptions} with row labels enabled. When
     * exporting, this means that labels assigned to the {@link org.tms.api.Row Row}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output.
     * When importing, this means that row labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Row Row}s read from the file.
     * @return a new {@link IOOptions} with row labels enabled
     */
    abstract public IOOptions withRowLabels();
    
    /**
     * Returns a new {@link IOOptions} with row labels enabled or disabled, as per the
     * supplied parameter {@code b}. When
     * exporting, this means that when enabled, labels assigned to the {@link org.tms.api.Row Row}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output. When disabled,
     * row labels in the exported table are ignored and are not included in the output.
     * When importing, this means that when enabled, row labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Row Row}s read from the file.
     * @param b {@code true} to include {@code Row} labels, {@code false} to ignore them
     * @return a new {@link IOOptions} with row labels enabled or disabled
     */
    abstract public IOOptions withRowLabels(final boolean b);
    
    /**
     * Returns a new {@link IOOptions} with column labels enabled. When
     * exporting, this means that labels assigned to the {@link org.tms.api.Column Column}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output.
     * When importing, this means that column labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Column Column}s read from the file.
     * @return a new {@link IOOptions} with column labels enabled
     */
    abstract public IOOptions withColumnLabels();
        
    /**
     * Returns a new {@link IOOptions} with column labels enabled or disabled, as per the
     * supplied parameter {@code b}. When
     * exporting, this means that when enabled, labels assigned to the {@link org.tms.api.Column Column}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output. When disabled,
     * column labels in the exported table are ignored and are not included in the output.
     * When importing, this means that when enabled, column labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Column Column}s read from the file.
     * @param b {@code true} to include {@code Column} labels, {@code false} to ignore them
     * @return a new {@link IOOptions} with column labels enabled or disabled
     */
    abstract public IOOptions withColumnNames(final boolean b);
    
    /**
     * Returns a new {@link IOOptions} where empty rows in the imported file are ignored, and where
     * empty {@link org.tms.api.Row Row}s in a {@link org.tms.api.Table Table} are ignored and not
     * included in the export file. 
     * @return a new {@link IOOptions} where empty rows are ignored
     */
    abstract public IOOptions withIgnoreEmptyRows();
    
    /**
     * Sets the behavior this {@link IOOptions} uses to handle empty rows. When set to {@code true},
     * empty rows in the source {@link org.tms.api.Table Table} are ignored (not written to the output file). 
     * On import, empty rows in the source file are ignored.
     * When set to {@code false}, empty rows are included in the exported file, in a file-format-appropriate
     * manner. On import, empty {@link org.tms.api.Row Row}s are inserted into the new {@link org.tms.api.Table Table}
     * when encountered in the imported file.
     * @param b {@code true} to import/export empty {@code Row}s, {@code false} to ignore them
     * @return a new {@link IOOptions} where empty rows are or are not ignored
     */
    abstract public IOOptions withIgnoreEmptyRows(final boolean b);
    
    /**
     * Returns a new {@link IOOptions} where empty columns in the imported file are ignored, and where
     * empty {@link org.tms.api.Column Column}s in a {@link org.tms.api.Table Table} are ignored and not
     * included in the export file. 
     * @return a new {@link IOOptions} where empty columns are ignored
     */
    abstract public IOOptions withIgnoreEmptyColumns();
    
    /**
     * Sets the behavior this {@link IOOptions} uses to handle empty columns. When set to {@code true},
     * empty columns in the source {@link org.tms.api.Table Table} are ignored (not written to the output file). 
     * On import, empty columns in the source file are ignored.
     * When set to {@code false}, empty columns are included in the exported file, in a file-format-appropriate
     * manner. On import, empty {@link org.tms.api.Column Column}s are inserted into the new {@link org.tms.api.Table Table}
     * when encountered in the imported file.
     * @param b {@code true} to import/export empty {@code Column}s, {@code false} to ignore them
     * @return a new {@link IOOptions} where empty columns are or are not ignored
     */
    abstract public IOOptions withIgnoreEmptyColumns(final boolean b);
    
    protected Map<OptionEnum, Object> m_options;
    
    private enum BaseOptions implements OptionEnum 
    {
        FileFormat,
        IsRowLabels,
        IsColumnLabels,
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
        set(BaseOptions.IsRowLabels, rowNames);
        set(BaseOptions.IsColumnLabels, columnNames);
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
    
    /**
     * Return the {@link IOFileFormat} enum representing the file format associated with this
     * {@code IOOptions}. 
     * @return the {code IOFileFormat} associated with this {@code IOOptions}
     */
    public IOFileFormat getFileFormat()
    {
        return (IOFileFormat)m_options.get(BaseOptions.FileFormat);
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} supports import. Currently,
     * {@link CSVOptions}, {@link XlsOptions}, and {@link XMLOptions} all support import, 
     * meaning that TMS {@link org.tms.api.Table Table}s can be imported from
     * comma separated value (CSV) files, Excel files (both xls and xlsx formatsare supported),
     * and XML documents.
     * @return {@code true} if this {@code IOOptions} supports import
     */
    public boolean canImport()
    {
        return getFileFormat() != null && getFileFormat().isSupportsImport();
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} supports export.
     * @return {@code true} if this {@code IOOptions} supports export
     */
    public boolean canExport()
    {
        return true;
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} is configured to import or export
     * TMS {@link org.tms.api.Column Column} labels.
     * @return {@code true} if this {@code IOOptions} imports/export {@code Column} labels
     */
    public boolean isColumnLabels()
    {
        return isTrue(BaseOptions.IsColumnLabels);
    }
    
    protected void setColumnLabels(boolean b)
    {
        set(BaseOptions.IsColumnLabels, b);
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} is configured to import or export
     * TMS {@link org.tms.api.Row Row} labels.
     * @return {@code true} if this {@code IOOptions} imports/export {@code Row} labels
     */
    public boolean isRowLabels()
    {
        return isTrue(BaseOptions.IsRowLabels);
    }
    
    protected void setRowLabels(boolean b)
    {
        set(BaseOptions.IsRowLabels, b);
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} is configured to ignore empty 
     * {@link org.tms.api.Row Row}s
     * when importing/exporting TMS {@link org.tms.api.Table Table}s.
     * @return {@code true} if empty {@code Row}s are ignored on import/export
     */
    public boolean isIgnoreEmptyRows()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyRows);
    }
    
    protected void setIgnoreEmptyRows(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyRows, b);
    }
    
    /**
     * Returns {@code true} if this {@code IOOptions} is configured to ignore empty 
     * {@link org.tms.api.Column Column}s
     * when importing/exporting TMS {@link org.tms.api.Table Table}s.
     * @return {@code true} if empty {@code Column}s are ignored on import/export
     */
    public boolean isIgnoreEmptyColumns()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyColumns);
    }
    
    protected void setIgnoreEmptyColumns(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyColumns, b);
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
}
