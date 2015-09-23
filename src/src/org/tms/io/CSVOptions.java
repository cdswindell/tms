package org.tms.io;

public class CSVOptions extends IOOptions
{

    public static final CSVOptions Default = new CSVOptions(Constants.COMMA, Constants.DOUBLE_QUOTE_CHAR, true, true, false, false, true);

    private char m_delimiter;
    private Character m_quoteCharacter;
    private boolean m_ignoreSuroundingSpaces;
    
    public CSVOptions(final char delimiter,
                      final Character quoteCharacter,
                      final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols, 
                      final boolean ignoreSurroundingSpaces)
    {
        super(org.tms.io.IOOptions.FileFormat.CSV, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
        m_delimiter = delimiter;
        m_quoteCharacter = quoteCharacter;
        m_ignoreSuroundingSpaces = ignoreSurroundingSpaces;
    }
    
    public CSVOptions withRowNames(final boolean b)
    {
        return new CSVOptions(m_delimiter, m_quoteCharacter, b, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }
    
    public CSVOptions withColumnNames(final boolean b)
    {
        return new CSVOptions(m_delimiter, m_quoteCharacter, m_rowNames, b, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }

    public CSVOptions withIgnoreEmptyRows(final boolean b)
    {
        return new CSVOptions(m_delimiter, m_quoteCharacter, m_rowNames, m_colNames, b, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    } 

    public CSVOptions withIgnoreEmptyColumns(final boolean b)
    {
        return new CSVOptions(m_delimiter, m_quoteCharacter, m_rowNames, m_colNames, m_ignoreEmptyRows, b, m_ignoreSuroundingSpaces);
    } 
    
    public boolean isIgnoreSuroundingSpaces()
    {
        return m_ignoreSuroundingSpaces;
    }
    
    public CSVOptions withIgnoreSuroundingSpaces()
    {
        return withIgnoreSuroundingSpaces(true);
    }

    public CSVOptions withIgnoreSuroundingSpaces(final boolean b)
    {
        return new CSVOptions(m_delimiter, m_quoteCharacter, m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, b);
    } 
    
    public char getDelimiter() 
    {
        return m_delimiter;
    }

    public CSVOptions withDelimiter(final char delimiter) 
    {
        return new CSVOptions(delimiter, m_quoteCharacter, m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }
    
    public Character getQuote() 
    {
        return m_quoteCharacter;
    }

    public CSVOptions withQuote(final char c) 
    {
        return withQuote(Character.valueOf(c));
    }

    public CSVOptions withQuote(final Character quoteCharacter) 
    {
        return new CSVOptions(m_delimiter, quoteCharacter, m_rowNames, m_colNames, m_ignoreEmptyRows, m_ignoreEmptyCols, m_ignoreSuroundingSpaces);
    }
}
