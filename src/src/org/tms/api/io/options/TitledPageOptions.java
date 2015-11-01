package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

/**
 * The base class that {@link IOOptions} that support titles extend.
 * <p>
 * <b>Note</b>: {@code TitledPageOptions} methods only affect export operations.
 * <p>
 * @param <E> the type of {@link IOOptions} in this {@code TitledPageOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see TitleableOption
 */
public abstract class TitledPageOptions<E extends TitledPageOptions<E>> 
    extends StyledPageOptions<E>
    implements TitleableOption
{
    private enum Options implements OptionEnum 
    {
        Title,
        TitleFontSize,
    }
    
    protected abstract E clone(TitledPageOptions<E> model);
    
    protected TitledPageOptions(final IOFileFormat format,
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

    protected TitledPageOptions(final TitledPageOptions<E> format)
    {
        super(format);
    }

    @Override
    protected E clone(StyledPageOptions<E> model)
    {
        return clone((TitledPageOptions<E>) model);
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public String getTitle()
    {
        return (String)get(Options.Title);
    }

    protected void setTitle(String t)
    {
        set(Options.Title, t);
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public boolean hasTitle()
    {
        String title = getTitle();
        return title != null && title.trim().length() > 0;
    }

    /**
     * Assign a title string to display in the output on export.
     * @param title the title string to display in the output
     * @return a new {@code TitledPageOptions} with the specified title string
     */
    public E withTitle(String title)
    {
        E newOption = clone(this);
        newOption.set(Options.Title, title);
        return newOption;
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public int getTitleFontSize()
    {
        Object d = get(Options.TitleFontSize);
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
     * @param f the new title font size, in pixels
     * @return a new {@code TitledPageOptions} with the specified title font size
     */
    public E withTitleFontSize(int f)
    {
        E newOptions = clone(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
