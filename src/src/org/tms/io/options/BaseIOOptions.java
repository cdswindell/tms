package org.tms.io.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.tms.api.io.IOOption;
import org.tms.api.io.IOFileFormat;

/**
 * The base class for all {@code IOOption}, which facilitate the import and export of 
 * {@link org.tms.api.Table Table}s to other file formats. 
 * {@code IOOption} and its super-classes support methods that determine what and how TMS data
 * is imported and exported. For example, methods exist to set output titles and font sizes, for
 * formats that support titles.
 * {@code IOOption} utilize the Builder design pattern, meaning that
 * methods that modify instance objects all return a new {@code IOOption} instance.
 * This allows different methods to be chained together, such as:
  * <blockquote><pre>
 *     PDFOptions.Default.withRowNames(false).withDescriptions(false).withTitle("Active Compounds")
 * </pre></blockquote>
 * Each concrete class implementing {@code IOOption} or one of its super-classes defines
 * a {@code public static} instance named {@code Default} which can be further modified, as needed.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code IOOption}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public abstract class BaseIOOptions<T extends BaseIOOptions<T>> implements IOOption<T>
{          
    protected Map<OptionEnum, Object> m_options;
    
    protected enum BaseOptions implements OptionEnum 
    {
        FileFormat,
        IsRowLabels,
        IsColumnLabels,
        IsIgnoreEmptyRows,
        IsIgnoreEmptyColumns;        
    }
    
    protected abstract T clone(final BaseIOOptions<T> model);
    
    protected BaseIOOptions(final IOFileFormat format, 
                     		final boolean ignoreEmptyRows,
                     		final boolean ignoreEmptyColumns)
    {
        m_options = new HashMap<OptionEnum, Object>();
        
        set(BaseOptions.FileFormat, format);
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
     * @see org.tms.api.io.IOOption#withIgnoreEmptyRows(boolean)
     */
    @Override
    public T withIgnoreEmptyRows(final boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setIgnoreEmptyRows(enabled);
        return newOptions;
    } 

    /* (non-Javadoc)
     * @see org.tms.api.io.IOOption#isIgnoreEmptyColumns()
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
     * @see org.tms.api.io.IOOption#withIgnoreEmptyColumns()
     */
    @Override
    public T withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }
    
    /* (non-Javadoc)
     * @see org.tms.api.io.IOOption#withIgnoreEmptyColumns(boolean)
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
    
    protected Object unset(OptionEnum key) 
    {
    	return m_options.remove(key);
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
