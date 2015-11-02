package org.tms.io.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.tms.api.io.BaseIOOption;
import org.tms.api.io.IOFileFormat;

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
 * @param <T> the type of {@link BaseIOOptions} in this {@code IOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public abstract class BaseIOOptions<T extends BaseIOOptions<T>> implements BaseIOOption<T>
{          
    protected Map<OptionEnum, Object> m_options;
    
    private enum BaseOptions implements OptionEnum 
    {
        FileFormat,
        IsRowLabels,
        IsColumnLabels,
        IsIgnoreEmptyRows,
        IsIgnoreEmptyColumns;        
    }
    
    protected abstract T clone(final BaseIOOptions<T> model);
    
    protected BaseIOOptions(final IOFileFormat format, 
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
    
    protected BaseIOOptions(final BaseIOOptions<T> format)
    {
        m_options = new HashMap<OptionEnum, Object>();
        for (Entry<OptionEnum, Object> e : format.m_options.entrySet()) {
            m_options.put(e.getKey(), e.getValue());
        }        
    }
    
    @Override
    public IOFileFormat getFileFormat()
    {
        return (IOFileFormat)m_options.get(BaseOptions.FileFormat);
    }
    
    @Override
    public boolean canImport()
    {
        return getFileFormat() != null && getFileFormat().isSupportsImport();
    }
    
    @Override
    public boolean canExport()
    {
        return true;
    }
    
    @Override
    public boolean isColumnLabels()
    {
        return isTrue(BaseOptions.IsColumnLabels);
    }
    
    protected void setColumnLabels(boolean b)
    {
        set(BaseOptions.IsColumnLabels, b);
    }
    
    @Override
    public T withColumnLabels()
    {
        return withColumnLabels(true);
    }
       
    @Override
    public T withColumnLabels(final boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setColumnLabels(enabled);
        return newOptions;
    }
    
    @Override
    public boolean isRowLabels()
    {
        return isTrue(BaseOptions.IsRowLabels);
    }
    
    protected void setRowLabels(boolean enabled)
    {
        set(BaseOptions.IsRowLabels, enabled);
    }
        
    @Override
    public T withRowLabels()
    {
        return withRowLabels(true);
    }

    @Override
    public T withRowLabels(final boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setRowLabels(enabled);
        return newOptions;
    }

    @Override
    public boolean isIgnoreEmptyRows()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyRows);
    }
    
    protected void setIgnoreEmptyRows(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyRows, b);
    }
    
   @Override
    public T withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    /* (non-Javadoc)
     * @see org.tms.api.io.BaseIOOption#withIgnoreEmptyRows(boolean)
     */
    @Override
    public T withIgnoreEmptyRows(final boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setIgnoreEmptyRows(enabled);
        return newOptions;
    } 

    /* (non-Javadoc)
     * @see org.tms.api.io.BaseIOOption#isIgnoreEmptyColumns()
     */
    @Override
    public boolean isIgnoreEmptyColumns()
    {
        return isTrue(BaseOptions.IsIgnoreEmptyColumns);
    }
    
    protected void setIgnoreEmptyColumns(boolean b)
    {
        set(BaseOptions.IsIgnoreEmptyColumns, b);
    }
    
    /* (non-Javadoc)
     * @see org.tms.api.io.BaseIOOption#withIgnoreEmptyColumns()
     */
    @Override
    public T withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }
    
    /* (non-Javadoc)
     * @see org.tms.api.io.BaseIOOption#withIgnoreEmptyColumns(boolean)
     */
    @Override
    public T withIgnoreEmptyColumns(final boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setIgnoreEmptyColumns(enabled);
        return newOptions;
    } 
    
    protected Object get(OptionEnum key) 
    {
        return m_options.get(key);
    }
    
    protected Object set(OptionEnum key, Object value) 
    {
        final Object oldValue = get(key);
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
        final Object value = get(key);
        if (value != null && Boolean.class.isAssignableFrom(value.getClass()))
            return ((Boolean)value).booleanValue();
        else
            return false;
    }
}