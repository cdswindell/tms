package org.tms.api.io.options;


abstract class FormattedPageOptions<E extends FormattedPageOptions<?>> 
    extends IOOptions 
    implements TitleableOption, DateTimeFormatOption, PageableOption, FontedOption
{
    public static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    public static final int DefaultPageWidthPx = (int) (8.5 * 72);
    public static final int DefaultPageHeightPx = (int) (11 * 72);
    public static final int DefaultColumnWidthPx = (int) 65;
    public static final int DefaultFontSizePx = 8;
    public static final String DefaultFontFamily = "SansSerif";

    private enum Options implements OptionEnum {
        Title,
        DateTimeFormat,
        IsPaged,
        IsPageNumbers,
        PageWidth,
        PageHeight,
        ColumnWidth,
        IsStickyRowNames,
        IsStickyColumnNames,
        DefaultFontSize,
        HeadingFontSize,
        TitleFontSize,
        FontFamily;
    }

    abstract E clone(FormattedPageOptions<?> model);
    
    protected FormattedPageOptions(final org.tms.api.io.options.IOOptions.FileFormat format,
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
        super(format, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);

        if (dateTimeFormat != null && dateTimeFormat.trim().length() > 0)
            set(Options.DateTimeFormat, dateTimeFormat.trim());

        if (defaultFontFamily != null && defaultFontFamily.trim().length() > 0)
            set(Options.FontFamily, defaultFontFamily.trim());

        set(Options.IsPaged, paged);
        set(Options.IsPageNumbers, pageNumbers);
        set(Options.PageWidth, pageWidthPx);
        set(Options.PageHeight, pageHeightPx);
        set(Options.ColumnWidth, colWidthPx);
        set(Options.IsStickyRowNames, stickyRowNames);
        set(Options.IsStickyColumnNames, stickyColNames);
        set(Options.DefaultFontSize, defaultFontSize);
    }

    protected FormattedPageOptions(final FormattedPageOptions<?> format)
    {
        super(format);
    }

    public E withTitle(String t)
    {
        E newOption = clone(this);
        newOption.set(Options.Title, t);
        return newOption;
    }
    
    public E withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public E withRowNames(final boolean b)
    {
        E newOptions = clone(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public E withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public E withColumnNames(final boolean b)
    {
        E newOptions = clone(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public E withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public E withIgnoreEmptyRows(final boolean b)
    {
        E newOptions = clone(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public E withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public E withIgnoreEmptyColumns(final boolean b)
    {
        E newOptions = clone(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
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

    public E withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    public E withColumnWidthInPx(int f)
    {
        E newOptions = clone(this);
        newOptions.setColumnWidth(f);
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

    public E withDefaultFontSize(int f)
    {
        E newOptions = clone(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    public E withHeadingFontSize(int f)
    {
        E newOptions = clone(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    public E withTitleFontSize(int f)
    {
        E newOptions = clone(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }

    public E withFontFamily(String ff)
    {
        E newOptions = clone(this);
        newOptions.setFontFamily(ff);
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
    public String getTitle()
    {
        return (String)get(Options.Title);
    }

    @Override
    public boolean hasTitle()
    {
        String title = getTitle();
        return title != null && title.trim().length() > 0;
    }

    protected void setTitle(String t)
    {
        set(Options.Title, t);
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
    public int getColumnWidth()
    {
        Object d = get(Options.ColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setColumnWidth(int i)
    {
        set(Options.ColumnWidth, i);
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

    @Override
    public int getDefaultFontSize()
    {
        Object d = get(Options.DefaultFontSize);
        return d != null ? (int)d : DefaultFontSizePx;
    }

    protected void setDefaultFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Default font size must be at least 4px");

        set(Options.DefaultFontSize, i);
    }

    @Override
    public int getHeadingFontSize()
    {
        Object d = get(Options.HeadingFontSize);
        return d != null ? (int)d : (int)(getDefaultFontSize() * 1.2);
    }

    protected void setHeadingFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Heading font size must be at least 4px");

        set(Options.HeadingFontSize, i);
    }

    @Override
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
    
    @Override
    public String getFontFamily()
    {
        return (String)get(Options.FontFamily);
    }

    protected void setFontFamily(String ff)
    {
        set(Options.FontFamily, ff);
    }
}
