package org.tms.io.options;

import org.tms.api.io.IOFileFormat;
import org.tms.api.io.TitleableIOOption;
import org.tms.api.io.IOOption;

/**
 * The base class that {@link IOOption} that support titles extend.
 * <p>
 * <b>Note</b>: {@code TitledPageIOOptions} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code TitledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see TitleableIOOption
 */
public abstract class TitledPageIOOptions<T extends TitledPageIOOptions<T>> 
    extends StyledPageIOOptions<T>
    implements TitleableIOOption<T>
{
	protected enum Options implements OptionEnum 
    {
        Title,
        TitleFontSize,
    }
    
    protected abstract T clone(final TitledPageIOOptions<T> model);
    
    protected TitledPageIOOptions(final IOFileFormat format,
            final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final int colWidthPx,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(format, rowNames, colNames, 
              ignoreEmptyRows, ignoreEmptyCols, colWidthPx, defaultFontSize, defaultFontFamily);
    }

    protected TitledPageIOOptions(final TitledPageIOOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(StyledPageIOOptions<T> model)
    {
        return clone((TitledPageIOOptions<T>) model);
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public String getTitle()
    {
        return (String)get(Options.Title);
    }

    protected void setTitle(final String t)
    {
        set(Options.Title, t);
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public boolean hasTitle()
    {
        final String title = getTitle();
        return title != null && title.trim().length() > 0;
    }

    /**
     * {@inheritDoc} 
     */
    public T withTitle(String title)
    {
        final T newOption = clone(this);
        newOption.set(Options.Title, title);
        return newOption;
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public int getTitleFontSize()
    {
        final Object d = get(Options.TitleFontSize);
        return d != null ? (int)d : (int)(getDefaultFontSize() * 2);
    }

    protected void setTitleFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Title font size must be at least 4px");

        set(Options.TitleFontSize, i);
    }
    
    /**
     * {@inheritDoc} 
     */
    public T withTitleFontSize(int fontSize)
    {
        final T newOptions = clone(this);
        newOptions.setTitleFontSize(fontSize);
        return newOptions;
    }
}
