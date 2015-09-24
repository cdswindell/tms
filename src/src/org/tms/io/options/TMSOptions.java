package org.tms.io.options;

public class TMSOptions extends IOOptions
{

    public static final TMSOptions Default = new TMSOptions(true, true, false, false);

    private TMSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols)
    {
        super(org.tms.io.options.IOOptions.FileFormat.TMS, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    public TMSOptions withRowNames(final boolean b)
    {
        return new TMSOptions(b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }
    
    public TMSOptions withColumnNames(final boolean b)
    {
        return new TMSOptions(m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols);
    }

    public TMSOptions withIgnoreEmptyRows(final boolean b)
    {
        return new TMSOptions(m_rowNames, m_colNames, b, m_ignoreEmptyCols);
    } 

    public TMSOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new TMSOptions(m_rowNames, m_colNames, m_ignoreEmptyRows, b);
    } 
}
