package org.tms.io.options;


public class PDFOptions extends IOOptions implements TitleableOption, DateTimeFormatOption, PageableOption
{
    public static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    public static final int DefaultPageWidthPx = (int) (8.5 * 72);
    public static final int DefaultPageHeightPx = (int) (11 * 72);
    public static final int DefaultColumnWidthPx = (int) 65;
    public static final int DefaultFontSizePx = 8;
    
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, DateTimeFormatPattern, 
                                                            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
                                                            true, true, DefaultFontSizePx);

    protected int m_defaultFontSizePx;
    protected int m_headingFontSizePx;
    protected int m_titleFontSizePx;
    
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
    
    private PDFOptions(final boolean rowNames, 
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
        super(org.tms.io.options.IOOptions.FileFormat.PDF, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);
        
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
    
    private PDFOptions (final PDFOptions format)
    {
        super(format);
    }
    
    @Override
    public PDFOptions withRowNames()
    {
        return withRowNames(true);
    }
    
    public PDFOptions withRowNames(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setRowNames(b);
        
        if (!b)
            newOptions.set(Options.IsStickyRowNames, false);
        
        return newOptions;
    }
    
    @Override
    public PDFOptions withColumnNames()
    {
        return withColumnNames(true);
    }
    
    public PDFOptions withColumnNames(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setColumnNames(b);
        
        if (!b)
            newOptions.set(Options.IsStickyColumnNames, false);
        
        return newOptions;
    }

    @Override
    public PDFOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public PDFOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }
    
    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
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
    
    public PDFOptions withTitle(String t)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setTitle(t);
        return newOptions;
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
    
    public PDFOptions withDateTimeFormat(String t)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }
    
    public boolean isPaged()
    {
        return isTrue(Options.IsPaged);
    }
    
    public PDFOptions withPages()
    {
        return withPages(true);
    }
    
    protected void setPaged(Boolean b)
    {
        set(Options.IsPaged, b);
    }
    
    public PDFOptions withPages(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPaged(b);
        return newOptions;
    }
    
    @Override
    public boolean isPageNumbers()
    {
        return isTrue(Options.IsPageNumbers);
    }
    
    protected void setPageNumbers(Boolean b)
    {
        set(Options.IsPageNumbers, b);
    }
    
    public PDFOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }
    
    public PDFOptions withPageNumbers(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageNumbers(b);
        if (b) 
            setPaged(true);
        
        return newOptions;
    }
    
    @Override
    public int getPageWidth()
    {
        Object d = get(Options.PageWidth);
        return d != null ? (int)d : 0;
    }
    
    public PDFOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }
    
    protected void setPageWidth(int i)
    {
        set(Options.PageWidth, i);
    }
    
    public PDFOptions withPageWidthInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageWidth(f);
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
    
    public PDFOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }
    
    public PDFOptions withPageHeightInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
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
    
    public PDFOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }
    
    public PDFOptions withColumnWidthInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public boolean isStickyRowNames()
    {
        return isTrue(Options.IsStickyRowNames);
    }
    
    public PDFOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }
    
    public PDFOptions withStickyRowNames(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.set(Options.IsStickyRowNames, b);
        if (b)
            newOptions.setRowNames(true);
        
        return newOptions;
    }
    
    @Override
    public boolean isStickyColumnNames()
    {
        return isTrue(Options.IsStickyColumnNames);
    }
    
    public PDFOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }
    
    public PDFOptions withStickyColumnNames(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.set(Options.IsStickyColumnNames, b);
        if (b)
            newOptions.setColumnNames(true);
        
        return newOptions;
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
    
    public PDFOptions withDefaultFontSize(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
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
    
    public PDFOptions withHeadingFontSize(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
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
     
     public PDFOptions withTitleFontSize(int f)
     {
         PDFOptions newOptions = new PDFOptions(this);
         newOptions.setTitleFontSize(f);
         return newOptions;
     }
}
