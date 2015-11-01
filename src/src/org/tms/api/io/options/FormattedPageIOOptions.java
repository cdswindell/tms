package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

/**
 * The base class that {@link BaseIOOptions} that support formatted and paged output extend.
 * Format and paging elements
 * that are supported include:
 * <ul>
 * <li>page widths,</li> 
 * <li>page heights,</li>
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
 * @see FormattedPageIOOptions
 * @see PageableIOOption
 */
public abstract class FormattedPageIOOptions<T extends FormattedPageIOOptions<T>>
    extends TitledPageIOOptions<FormattedPageIOOptions<T>> 
    implements DateTimeFormatIOOption, PageableIOOption
{
    static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    static final int DefaultPageWidthPx = (int) (8.5 * 72);
    static final int DefaultPageHeightPx = (int) (11 * 72);

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

    protected abstract T clone(FormattedPageIOOptions<T> model);
    
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
    protected T clone(TitledPageIOOptions<FormattedPageIOOptions<T>> model)
    {
        return clone((FormattedPageIOOptions<T>)model);
    }

    /**
     * {@inheritDoc} 
     */
    public String getDateTimeFormat()
    {
        return (String)get(Options.DateTimeFormat);
    }

    /**
     * {@inheritDoc} 
     */
    public boolean hasDateTimeFormat()
    {
        String dateTimeFormat = getDateTimeFormat();
        return dateTimeFormat != null && dateTimeFormat.trim().length() > 0;
    }

    protected void setDateTimeFormat(String t)
    {
        set(Options.DateTimeFormat, t);
    }

    /**
     * Set the date-time format pattern to use to display the time and date the
     * in the page footnotes of the export. The date-time format pattern follows the conventions 
     * described in {@link java.text.SimpleDateFormat SimpleDateFormat}. 
     * To disable the display of the date-time in page footers, set {@code pattern} to {@code null}.
     * <p>
     * The default value is <b>MM/dd/yyyy hh:mm a</b>
     * @param pattern the new date-time format pattern or {@code null} to disable
     * @return a new {@link T} with the date-time format pattern set
     * @see java.text.SimpleDateFormat SimpleDateFormat
     */
    public T withDateTimeFormat(String pattern)
    {
        T newOptions = clone(this);
        newOptions.setDateTimeFormat(pattern);
        return newOptions;
    }
    
    /**
     * {@inheritDoc} 
     */
    public boolean isPaged()
    {
        return isTrue(Options.IsPaged);
    }

    protected void setPaged(Boolean b)
    {
        set(Options.IsPaged, b);
    }

    /**
     * Enable paged output in exports. {@link org.tms.api.Table Table} data is output in discrete pages,
     * optionally, with column headings repeated on the top of each page, and page numbers and the date-time
     * that the export was performed output on the bottom of each page.
     * @return a new {@link T} with output paging enabled
     * @see FormattedPageIOOptions#withDateTimeFormat withDateTimeFormat
     * @see FormattedPageIOOptions#withPageNumbers withPageNumbers
     * @see FormattedPageIOOptions#withStickyColumnLabels withStickyColumnLabels
     */
    public T withPages()
    {
        return withPages(true);
    }

    /**
     * Enable or disable paged output in exports. When {@code true},
     * {@link org.tms.api.Table Table} data is output in discrete pages,
     * optionally, with column headings repeated on the top of each page, and page numbers and the date-time
     * that the export was performed output on the bottom of each page.
     * When {@code false}, the output is not paged.
     * @param enabled {@code true} to enable paging, {@code false} to disable paging
     * @return a new {@link T} with output paging enabled or disabled
     * @see FormattedPageIOOptions#withDateTimeFormat withDateTimeFormat
     * @see FormattedPageIOOptions#withPageNumbers withPageNumbers
     * @see FormattedPageIOOptions#withStickyColumnLabels withStickyColumnLabels
     */
    public T withPages(boolean enabled)
    {
        T newOptions = clone(this);
        newOptions.setPaged(enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc} 
     */
    public boolean isPageNumbers()
    {
        return isTrue(Options.IsPageNumbers);
    }

    protected void setPageNumbers(Boolean b)
    {
        set(Options.IsPageNumbers, b);
        if (b) 
            setPaged(true);      
    }

    /**
     * Enable the generation of page numbers in the page footer in the export output.
     * @return a new {@link T} with page numbers enabled
     */
    public T withPageNumbers()
    {
        return withPageNumbers(true);
    }

    /**
     * Enable or disable page numbers in the footer section of generated export output.
     * @param enabled {@code true} to enable page numbers, {@code false} to disable
     * @return a new {@link T} with page numbers enabled or disabled
     */
    public T withPageNumbers(boolean enabled)
    {
        T newOptions = clone(this);
        newOptions.setPageNumbers(enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc} 
     */
    public int getPageWidth()
    {
        Object d = get(Options.PageWidth);
        return d != null ? (int)d : 0;
    }

    protected void setPageWidth(int i)
    {
        set(Options.PageWidth, i);
    }

    /**
     * Set the page width, in inches, in the generated export output.
     * @param width the new page width of the export output, in inches
     * @return a new {@link T} with the specified page width, in inches
     */
    public T withPageWidthInInches(double width)
    {
        return withPageWidthInPx((int)(width * 72));
    }

    /**
     * Set the page width, in pixels, in the generated export output.
     * @param width the new page width of the export output, in pixels
     * @return a new {@link T} with the specified page width, in pixels
     */
    public T withPageWidthInPx(int width)
    {
        T newOptions = clone(this);
        newOptions.setPageWidth(width);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     */
    public int getPageHeight()
    {
        Object d = get(Options.PageHeight);
        return d != null ? (int)d : 0;
    }

    protected void setPageHeight(int i)
    {
        set(Options.PageHeight, i);
    }

    /**
     * Set the page height, in inches, in the generated export output.
     * @param height the new page height of the export output, in inches
     * @return a new {@link T} with the specified page height, in inches
     */
    public T withPageHeightInInches(double height)
    {
        return withPageHeightInPx((int)(height * 72));
    }

    /**
     * Set the page height, in pixels, in the generated export output.
     * @param height the new page height of the export output, in pixels
     * @return a new {@link T} with the specified page height, in pixels
     */
    public T withPageHeightInPx(int height)
    {
        T newOptions = clone(this);
        newOptions.setPageHeight(height);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     */
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

    @Override
    protected void setRowLabels(final boolean b)
    {
        super.setRowLabels(b);
        
        if (!b)
            set(Options.IsStickyRowLabels, false);
    }
    
    /**
     * Enables sticky {@link org.tms.api.Row Row} labels. This causes {@link org.tms.api.Row Row} labels 
     * to reprint on subsequent export output pages
     * when column data spans more than one page.
     * @return a new {@link T} with sticky row labels enabled
     */
    public T withStickyRowLabels()
    {
        return withStickyRowLabels(true);
    }

    /**
     * Enables or disables sticky {@link org.tms.api.Row Row} labels. When enabled,
     * {@link org.tms.api.Row Row} labels are reprinted on subsequent export output pages
     * when column data spans more than one page.
     * @param sticky {@code true} to enable sticky row labels, {@code false} to disable
     * @return a new {@link T} with sticky row labels enabled or disabled
     */
    public T withStickyRowLabels(boolean sticky)
    {
        T newOptions = clone(this);
        newOptions.setStickyRowLabels(sticky);
        return newOptions;
    }

    @Override
    protected void setColumnLabels(final boolean b)
    {
        super.setColumnLabels(b);
        
        if (!b)
            set(Options.IsStickyColumnLabels, false);
    }
    
    /**
     * {@inheritDoc} 
     */
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
    
    /**
     * Enables sticky {@link org.tms.api.Column Column} labels. This causes {@link org.tms.api.Column Column} labels 
     * are reprinted at the top of subsequent export output pages
     * when there are more rows in the exported {@link org.tms.api.Table Table} than can fit on a single page.
     * @return a new {@link T} with sticky column labels enabled
     */
    public T withStickyColumnLabels()
    {
        return withStickyColumnLabels(true);
    }

    /**
     * Enables or disables sticky {@link org.tms.api.Column Column} labels. When enabled,
     * {@link org.tms.api.Column Column} labels are reprinted at the top of subsequent export output pages
     * when there are more rows in the exported {@link org.tms.api.Table Table} than can fit on a single page.
     * @param sticky {@code true} to enable sticky column labels, {@code false} to disable
     * @return a new {@link T} with sticky column labels enabled or disabled
     */
    public T withStickyColumnLabels(boolean sticky)
    {
        T newOptions = clone(this);
        newOptions.setStickyColumnLabels(sticky);
        return newOptions;
    }
}
