package org.tms.api.io.options;

import org.tms.io.options.IOFileFormat;
import org.tms.io.options.OptionEnum;

public abstract class FormattedPageOptions<E extends FormattedPageOptions<E>>
    extends TitledPageOptions<FormattedPageOptions<E>> 
    implements DateTimeFormatOption, PageableOption
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
        IsStickyRowNames,
        IsStickyColumnNames,
    }

    protected abstract E clone(FormattedPageOptions<E> model);
    
    protected FormattedPageOptions(final IOFileFormat format,
            final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final String dateTimeFormat,
            final boolean paged,
            final boolean pageNumbers,
            final int pageWidthPx,
            final int pageHeightPx,
            final int colWidthPx,
            final boolean stickyRowNames,
            final boolean stickyColNames,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(format, (rowNames || stickyRowNames), (colNames || stickyColNames), 
              ignoreEmptyRows, ignoreEmptyCols, colWidthPx, defaultFontSize, defaultFontFamily);

        if (dateTimeFormat != null && dateTimeFormat.trim().length() > 0)
            set(Options.DateTimeFormat, dateTimeFormat.trim());

        set(Options.IsPaged, paged);
        set(Options.IsPageNumbers, pageNumbers);
        set(Options.PageWidth, pageWidthPx);
        set(Options.PageHeight, pageHeightPx);
        set(Options.IsStickyRowNames, stickyRowNames);
        set(Options.IsStickyColumnNames, stickyColNames);
    }

    protected FormattedPageOptions(final FormattedPageOptions<E> format)
    {
        super(format);
    }

    @Override
    protected E clone(TitledPageOptions<FormattedPageOptions<E>> model)
    {
        return clone((FormattedPageOptions<E>)model);
    }

    public E withDateTimeFormat(String t)
    {
        E newOptions = clone(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }

    public E withPages()
    {
        return withPages(true);
    }

    public E withPages(boolean b)
    {
        E newOptions = clone(this);
        newOptions.setPaged(b);
        return newOptions;
    }

    public E withPageNumbers()
    {
        return withPageNumbers(true);
    }

    public E withPageNumbers(boolean b)
    {
        E newOptions = clone(this);
        newOptions.setPageNumbers(b);
        return newOptions;
    }

    public E withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    public E withPageWidthInPx(int f)
    {
        E newOptions = clone(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public E withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    public E withPageHeightInPx(int f)
    {
        E newOptions = clone(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public E withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    public E withStickyRowNames(boolean b)
    {
        E newOptions = clone(this);
        newOptions.setStickyRowNames(b);
        return newOptions;
    }

    public E withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    public E withStickyColumnNames(boolean b)
    {
        E newOptions = clone(this);
        newOptions.setStickyColumnNames(b);
        return newOptions;
    }

    @Override
    protected void setRowNames(final boolean b)
    {
        super.setRowNames(b);
        
        if (!b)
            set(Options.IsStickyRowNames, false);
    }
    
    @Override
    protected void setColumnNames(final boolean b)
    {
        super.setColumnNames(b);
        
        if (!b)
            set(Options.IsStickyColumnNames, false);
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

    protected void setDateTimeFormat(String t)
    {
        set(Options.DateTimeFormat, t);
    }

    public boolean isPaged()
    {
        return isTrue(Options.IsPaged);
    }

    protected void setPaged(Boolean b)
    {
        set(Options.IsPaged, b);
    }

    @Override
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
    public boolean isStickyRowNames()
    {
        return isTrue(Options.IsStickyRowNames);
    }

    protected void setStickyRowNames(boolean b)
    {
        set(Options.IsStickyRowNames, b);
        if (b)
            setRowNames(true);
    }

    @Override
    public boolean isStickyColumnNames()
    {
        return isTrue(Options.IsStickyColumnNames);
    }

    protected void setStickyColumnNames(boolean b)
    {
        set(Options.IsStickyColumnNames, b);
        if (b)
            setColumnNames(true);
    }

}
