package org.tms.io.options;


public class PDFOptions extends IOOptions implements TitleableOption, DateTimeFormatOption, PageableOption
{
    public static final String DateTimeFormatPattern = "MM/dd/yyyy hh:mm a";
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, null, DateTimeFormatPattern, true, true, true);

    private String m_title;
    private String m_dateTimeFormat;
    private boolean m_pageNumbers;
    private boolean m_stickyRowNames;
    private boolean m_stickyColNames;
    
    private PDFOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final String title,
                      final String dateTimeFormat,
                      final boolean pageNumbers,
                      final boolean stickyRowNames,
                      final boolean stickyColNames)
    {
        super(org.tms.io.options.IOOptions.FileFormat.PDF, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);
        
        m_title = title;
        m_dateTimeFormat = dateTimeFormat;
        m_pageNumbers = pageNumbers;
        
        m_stickyRowNames = stickyRowNames;
        m_stickyColNames = stickyColNames;
    }
    
    public PDFOptions withRowNames(final boolean b)
    {
        if (!b)
            m_stickyRowNames = false;            
        return new PDFOptions(b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    }
    
    public PDFOptions withColumnNames(final boolean b)
    {
        if (!b)
            m_stickyColNames = false;            
        return new PDFOptions(m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    }

    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, b, m_ignoreEmptyCols, m_title, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    } 

    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, b, m_title, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, t, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, t, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    }
    
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, b, m_stickyRowNames, m_stickyColNames);
    }
    
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, m_pageNumbers, b, m_stickyColNames);
    }
    
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_dateTimeFormat, m_pageNumbers, m_stickyRowNames, b);
    }
}
