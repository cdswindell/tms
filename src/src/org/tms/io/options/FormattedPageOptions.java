package org.tms.io.options;


abstract class FormattedPageOptions extends IOOptions implements TitleableOption, DateTimeFormatOption, PageableOption, FontedOption
{
    public static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    public static final int DefaultPageWidthPx = (int) (8.5 * 72);
    public static final int DefaultPageHeightPx = (int) (11 * 72);
    public static final int DefaultColumnWidthPx = (int) 65;
    public static final int DefaultFontSizePx = 8;

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
    }

    abstract public FormattedPageOptions withTitle(String t);
    
    abstract public FormattedPageOptions withDateTimeFormat(String t);

    abstract public FormattedPageOptions withPages();

    abstract public FormattedPageOptions withPages(boolean b);

    abstract public FormattedPageOptions withPageNumbers();

    abstract public FormattedPageOptions withPageNumbers(boolean b);

    abstract public FormattedPageOptions withPageWidthInPx(int f);

    abstract public FormattedPageOptions withPageHeightInPx(int f);

    abstract public FormattedPageOptions withColumnWidthInPx(int f);

    abstract public FormattedPageOptions withStickyRowNames();

    abstract public FormattedPageOptions withStickyRowNames(boolean b);

    abstract public FormattedPageOptions withStickyColumnNames();

    abstract public FormattedPageOptions withStickyColumnNames(boolean b);
    
    abstract public FormattedPageOptions withDefaultFontSize(int f);
    
    abstract public FormattedPageOptions withHeadingFontSize(int f); 
    
    abstract public FormattedPageOptions withTitleFontSize(int f);

    protected FormattedPageOptions(final org.tms.io.options.IOOptions.FileFormat format,
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
            final int defaultFontSize)
    {
        super(format, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);

        if (dateTimeFormat != null && dateTimeFormat.trim().length() > 0)
            set(Options.DateTimeFormat, dateTimeFormat.trim());

        set(Options.IsPaged, paged);
        set(Options.IsPageNumbers, pageNumbers);
        set(Options.PageWidth, pageWidthPx);
        set(Options.PageHeight, pageHeightPx);
        set(Options.ColumnWidth, colWidthPx);
        set(Options.IsStickyRowNames, stickyRowNames);
        set(Options.IsStickyColumnNames, stickyColNames);
        set(Options.DefaultFontSize, defaultFontSize);
    }

    protected FormattedPageOptions(final FormattedPageOptions format)
    {
        super(format);
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
}
