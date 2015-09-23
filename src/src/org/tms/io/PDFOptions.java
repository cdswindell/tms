package org.tms.io;

public class PDFOptions extends IOOptions
{
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, null, true, true, true);

    private String m_title;
    private boolean m_pageNumbers;
    private boolean m_stickyRowNames;
    private boolean m_stickyColNames;
    
    public PDFOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final String title,
                      final boolean pageNumbers,
                      final boolean stickyRowNames,
                      final boolean stickyColNames)
    {
        super(org.tms.io.IOOptions.FileFormat.PDF, (rowNames || stickyRowNames), (colNames || stickyColNames), ignoreEmptyRows, ignoreEmptyCols);
        
        m_title = title;
        m_pageNumbers = pageNumbers;
        
        m_stickyRowNames = stickyRowNames;
        m_stickyColNames = stickyColNames;
    }
    
    public PDFOptions withRowNames(final boolean b)
    {
        if (!b)
            m_stickyRowNames = false;            
        return new PDFOptions(b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    }
    
    public PDFOptions withColumnNames(final boolean b)
    {
        if (!b)
            m_stickyColNames = false;            
        return new PDFOptions(m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    }

    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, b, m_ignoreEmptyCols, m_title, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    } 

    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, b, m_title, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
    } 
    
    public String getTitle()
    {
        return m_title;
    }
    
    public PDFOptions withTitle(String t)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, t, m_pageNumbers, m_stickyRowNames, m_stickyColNames);
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, b, m_stickyRowNames, m_stickyColNames);
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_pageNumbers, b, m_stickyColNames);
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
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_title, m_pageNumbers, m_stickyRowNames, b);
    }
}
