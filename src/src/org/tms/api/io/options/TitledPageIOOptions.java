package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

/**
 * The base class that {@link BaseIOOptions} that support titles extend.
 * <p>
 * <b>Note</b>: {@code TitledPageIOOptions} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link BaseIOOptions} in this {@code TitledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see TitleableIOOption
 */
public abstract class TitledPageIOOptions<T extends TitledPageIOOptions<T>> 
    extends StyledPageIOOptions<T>
    implements TitleableIOOption
{
    private enum Options implements OptionEnum 
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
     * Assign a title string to display in the output on export.
     * @param title the title string to display in the output
     * @return a new {@link T} with the specified title string
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
     * Set the font size, in pixels, to use when drawing the title string on export.
     * @param fontSize the new title font size, in pixels
     * @return a new {@link T} with the specified title font size
     */
    public T withTitleFontSize(int fontSize)
    {
        final T newOptions = clone(this);
        newOptions.setTitleFontSize(fontSize);
        return newOptions;
    }
}
