package org.tms.io.options;


public class PDFOptions extends IOOptions implements TitleableOption, DateTimeFormatOption, PageableOption
{
    public static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    public static final int DefaultPageWidthPx = (int) (8.5 * 72);
    public static final int DefaultPageHeightPx = (int) (11 * 72);
    public static final int DefaultColumnWidthPx = (int) 65;
    
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, null, DateTimeFormatPattern, 
                                                            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
                                                            true, true);

    private String m_title;
    private String m_dateTimeFormat;
    private boolean m_pageNumbers;
    private int m_pageWidthPx;
    private int m_pageHeightPx;
    private int m_columnWidthPx;
    private boolean m_stickyRowNames;
    private boolean m_stickyColNames;
    private boolean m_paged;
    
    private PDFOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final String title,
                      final String dateTimeFormat,
                      final boolean paged,
                      final boolean pageNumbers,
                      final int pageWidthPx,
                      final int pageHeightPx,
                      final int colWidthPx,
                      final boolean stickyRowNames,
                      final boolean stickyColNames)
    {
        super(org.tms.io.options.IOOptions.FileFormat.PDF, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);
        
        m_title = title;
        m_dateTimeFormat = dateTimeFormat;
        
        m_paged = paged;
        m_pageNumbers = pageNumbers;
        m_pageWidthPx = pageWidthPx;
        m_pageHeightPx = pageHeightPx;
        m_columnWidthPx = colWidthPx;
        
        m_stickyRowNames = stickyRowNames;
        m_stickyColNames = stickyColNames;
    }
    
    public PDFOptions withRowNames(final boolean b)
    {
        if (!b)
            m_stickyRowNames = false;            
        return new PDFOptions(b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    public PDFOptions withColumnNames(final boolean b)
    {
        if (!b)
            m_stickyColNames = false;            
        return new PDFOptions(m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }

    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, b, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    } 

    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, b, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    } 
    
    @Override
    public String getTitle()
    {
        return m_title;
    }
    
    @Override
    public boolean hasTitle()
    {
        return m_title != null && m_title.trim().length() > 0;
    }
    
    public PDFOptions withTitle(String t)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, t, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    @Override
    public String getDateTimeFormat()
    {
        return m_dateTimeFormat;
    }
    
    @Override
    public boolean hasDateTimeFormat()
    {
        return m_dateTimeFormat != null && m_dateTimeFormat.trim().length() > 0;
    }
    
    public PDFOptions withDateTimeFormat(String t)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, t, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    public boolean isPaged()
    {
        return m_paged;
    }
    
    public PDFOptions withPages()
    {
        return withPages(true);
    }
    
    public PDFOptions withPages(boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            b, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    @Override
    public boolean isPageNumbers()
    {
        return m_pageNumbers;
    }
    
    public PDFOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }
    
    public PDFOptions withPageNumbers(boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, b, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    @Override
    public int getPageWidth()
    {
        return m_pageWidthPx;
    }
    
    public PDFOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }
    
    public PDFOptions withPageWidthInPx(int f)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, f, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
    
    @Override
    public int getPageHeight()
    {
        return m_pageHeightPx;
    }
    
    public PDFOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }
    
    public PDFOptions withPageHeightInPx(int f)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, f, m_columnWidthPx, m_stickyRowNames, m_stickyColNames);
    }
       
    @Override
    public int getColumnWidth()
    {
        return m_columnWidthPx;
    }
    
    public PDFOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }
    
    public PDFOptions withColumnWidthInPx(int f)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, f, m_stickyRowNames, m_stickyColNames);
    }
    
    @Override
    public boolean isStickyRowNames()
    {
        return m_stickyRowNames;
    }
    
    public PDFOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }
    
    public PDFOptions withStickyRowNames(boolean b)
    {
        if (b)
            m_rowNames = true;
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, b, m_stickyColNames);
    }
    
    @Override
    public boolean isStickyColumnNames()
    {
        return m_stickyColNames;
    }
    
    public PDFOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }
    
    public PDFOptions withStickyColumnNames(boolean b)
    {
        if (b)
            m_colNames = true;
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, 
                            m_paged, m_pageNumbers, m_pageWidthPx, m_pageHeightPx, m_columnWidthPx, m_stickyRowNames, b);
    }
}
