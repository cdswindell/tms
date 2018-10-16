package org.tms.io.options;

import org.tms.api.io.IOFileFormat;
import org.tms.api.io.IOOption;
import org.tms.api.io.LabeledIOOption;

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
public abstract class LabeledIOOptions<T extends LabeledIOOptions<T>> extends BaseIOOptions<T> implements LabeledIOOption<T>
{          
    protected abstract T clone(final LabeledIOOptions<T> model);
    
    protected LabeledIOOptions(final IOFileFormat format, 
                     final boolean rowNames, 
                     final boolean columnNames, 
                     final boolean ignoreEmptyRows,
                     final boolean ignoreEmptyColumns)
    {
    	super(format, ignoreEmptyRows, ignoreEmptyColumns);
        set(BaseOptions.IsRowLabels, rowNames);
        set(BaseOptions.IsColumnLabels, columnNames);
    }
    
    protected LabeledIOOptions(final LabeledIOOptions<T> format)
    {
    	super(format);
    }
    
    @Override
    protected T clone(final BaseIOOptions<T> model)
    {
        return clone((LabeledIOOptions<T>) model);
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
}
