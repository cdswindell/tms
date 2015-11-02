package org.tms.io.options;

import org.tms.api.io.DateTimeFormatIOOption;
import org.tms.api.io.IOFileFormat;
import org.tms.api.io.PageableIOOption;

/**
 * The base class that {@link BaseIOOptions} that support formatted and paged export output.
 * Format and paging elements
 * that are supported include:
 * <ul>
 * <li>page width,</li> 
 * <li>page height,</li>
 * <li>page numbers,</li>
 * <li>paging,</li>
 * <li>sticky row labels,</li>
 * <li>sticky columns labels, and</li>
 * <li>date-time format pattern for page footers.</li>
 * </ul>
 * <p>
 * <b>Note</b>: {@code FormattedPageIOOptions} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link BaseIOOptions} in this {@code FormattedPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see DateTimeFormatIOOption
 * @see PageableIOOption
 */
public abstract class FormattedPageIOOptions<T extends FormattedPageIOOptions<T>>
    extends TitledPageIOOptions<FormattedPageIOOptions<T>> 
    implements DateTimeFormatIOOption<T>, PageableIOOption<T>
{
    static protected final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    static protected final int DefaultPageWidthPx = (int) (8.5 * 72);
    static protected final int DefaultPageHeightPx = (int) (11 * 72);

    private enum Options implements OptionEnum 
    {
        DateTimeFormat,
        IsPaged,
        IsPageNumbers,
        PageWidth,
        PageHeight,
        IsStickyRowLabels,
        IsStickyColumnLabels,
    }

    protected abstract T clone(final FormattedPageIOOptions<T> model);
    
    protected FormattedPageIOOptions(final IOFileFormat format,
            final boolean rowLabels, 
            final boolean colLabels, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final String dateTimeFormat,
            final boolean paged,
            final boolean pageNumbers,
            final int pageWidthPx,
            final int pageHeightPx,
            final int colWidthPx,
            final boolean stickyRowLabels,
            final boolean stickyColLabels,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(format, (rowLabels || stickyRowLabels), (colLabels || stickyColLabels), 
              ignoreEmptyRows, ignoreEmptyCols, colWidthPx, defaultFontSize, defaultFontFamily);

        if (dateTimeFormat != null && dateTimeFormat.trim().length() > 0)
            set(Options.DateTimeFormat, dateTimeFormat.trim());

        set(Options.IsPaged, paged);
        set(Options.IsPageNumbers, pageNumbers);
        set(Options.PageWidth, pageWidthPx);
        set(Options.PageHeight, pageHeightPx);
        set(Options.IsStickyRowLabels, stickyRowLabels);
        set(Options.IsStickyColumnLabels, stickyColLabels);
    }

    protected FormattedPageIOOptions(final FormattedPageIOOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(final TitledPageIOOptions<FormattedPageIOOptions<T>> model)
    {
        return clone((FormattedPageIOOptions<T>)model);
    }

    @Override
    public String getDateTimeFormat()
    {
        return (String)get(Options.DateTimeFormat);
    }

    @Override
    public boolean hasDateTimeFormat()
    {
        String dateTimeFormat = getDateTimeFormat();
        return dateTimeFormat != null && dateTimeFormat.trim().length() > 0;
    }

    protected void setDateTimeFormat(final String t)
    {
        set(Options.DateTimeFormat, t);
    }

    @Override
    public T withDateTimeFormat(String pattern)
    {
        final T newOptions = clone(this);
        newOptions.setDateTimeFormat(pattern);
        return newOptions;
    }
    
    @Override
    public boolean isPaged()
    {
        return isTrue(Options.IsPaged);
    }

    protected void setPaged(Boolean b)
    {
        set(Options.IsPaged, b);
    }

    @Override
    public T withPages()
    {
        return withPages(true);
    }

    @Override
    public T withPages(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setPaged(enabled);
        return newOptions;
    }
    
    @Override
    public boolean isPageNumbers()
    {
        return isTrue(Options.IsPageNumbers);
    }

    protected void setPageNumbers(boolean enabled)
    {
        set(Options.IsPageNumbers, enabled);
        if (enabled) 
            setPaged(true);      
    }

    @Override
    public T withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public T withPageNumbers(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.setPageNumbers(enabled);
        return newOptions;
    }
    
    @Override
    public int getPageWidth()
    {
        Object d = get(Options.PageWidth);
        return d != null ? (int)d : 0;
    }

    protected void setPageWidth(int i)
    {
        set(Options.PageWidth, i);
    }

    @Override
    public T withPageWidthInInches(double width)
    {
        return withPageWidthInPx((int)(width * 72));
    }

    @Override
    public T withPageWidthInPx(int width)
    {
        final T newOptions = clone(this);
        newOptions.setPageWidth(width);
        return newOptions;
    }

    @Override
    public int getPageHeight()
    {
        Object d = get(Options.PageHeight);
        return d != null ? (int)d : 0;
    }

    protected void setPageHeight(int i)
    {
        set(Options.PageHeight, i);
    }

    @Override
    public T withPageHeightInInches(double height)
    {
        return withPageHeightInPx((int)(height * 72));
    }

    @Override
    public T withPageHeightInPx(int height)
    {
        final T newOptions = clone(this);
        newOptions.setPageHeight(height);
        return newOptions;
    }

    @Override
    public boolean isStickyRowLabels()
    {
        return isTrue(Options.IsStickyRowLabels);
    }

    protected void setStickyRowLabels(boolean b)
    {
        set(Options.IsStickyRowLabels, b);
        if (b)
            setRowLabels(true);
    }

    protected void setRowLabels(final boolean b)
    {
        super.setRowLabels(b);
        
        if (!b)
            set(Options.IsStickyRowLabels, false);
    }
    
    @Override
    public T withStickyRowLabels()
    {
        return withStickyRowLabels(true);
    }

    @Override
    public T withStickyRowLabels(boolean sticky)
    {
        final T newOptions = clone(this);
        newOptions.setStickyRowLabels(sticky);
        return newOptions;
    }

    protected void setColumnLabels(final boolean b)
    {
        super.setColumnLabels(b);
        
        if (!b)
            set(Options.IsStickyColumnLabels, false);
    }
    
    @Override
    public boolean isStickyColumnLabels()
    {
        return isTrue(Options.IsStickyColumnLabels);
    }

    /**
     * Enables sticky {@link org.tms.api.Column Column} labels. This causes {@link org.tms.api.Column Column} labels 
     * to reprint on subsequent export output pages, as needed.
     * @return a new {@link T} with sticky column labels enabled
     */
    protected void setStickyColumnLabels(boolean b)
    {
        set(Options.IsStickyColumnLabels, b);
        if (b)
            setColumnLabels(true);
    }
    
    @Override
    public T withStickyColumnLabels()
    {
        return withStickyColumnLabels(true);
    }

    @Override
    public T withStickyColumnLabels(boolean sticky)
    {
        final T newOptions = clone(this);
        newOptions.setStickyColumnLabels(sticky);
        return newOptions;
    }
}
