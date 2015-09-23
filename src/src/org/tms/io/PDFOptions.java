package org.tms.io;

public class PDFOptions extends IOOptions
{
    public static final PDFOptions Default = new PDFOptions(true, true, false, false);

    public PDFOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols)
    {
        super(org.tms.io.IOOptions.FileFormat.PDF, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    public PDFOptions withRowNames(final boolean b)
    {
        return new PDFOptions(b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }
    
    public PDFOptions withColumnNames(final boolean b)
    {
        return new PDFOptions(m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }

    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, b, m_ignoreEmptyCols);
    } 

    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new PDFOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, b);
    } 
}
