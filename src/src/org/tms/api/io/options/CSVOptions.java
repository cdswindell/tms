package org.tms.api.io.options;


public class CSVOptions extends IOOptions
{

    public static final CSVOptions Default = new CSVOptions(true, true, false, false, Constants.COMMA, Constants.DOUBLE_QUOTE_CHAR, true);

    private enum Options implements OptionEnum {
        DelimiterChar,
        QuoteChar,
        IsIgnoreSurrountingSpaces;        
    }
    
    private CSVOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final char delimiter,
                      final Character quoteCharacter,
                      final boolean ignoreSurroundingSpaces)
    {
        super(org.tms.api.io.options.IOOptions.FileFormat.CSV, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
        set(Options.DelimiterChar, delimiter);
        set(Options.QuoteChar, quoteCharacter);
        set(Options.IsIgnoreSurrountingSpaces, ignoreSurroundingSpaces);
    }
    
    private CSVOptions (final CSVOptions format)
    {
        super(format);
    }
    
    @Override
    public CSVOptions withRowNames()
    {
        return withRowNames(true);
    }
    
    @Override
    public CSVOptions withRowNames(final boolean b)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }
    
    @Override
    public CSVOptions withColumnNames()
    {
        return withColumnNames(true);
    }
    
    @Override
    public CSVOptions withColumnNames(final boolean b)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public CSVOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    @Override
    public CSVOptions withIgnoreEmptyRows(final boolean b)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public CSVOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public CSVOptions withIgnoreEmptyColumns(final boolean b)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 
    
    public boolean isIgnoreSuroundingSpaces()
    {
        return isTrue(Options.IsIgnoreSurrountingSpaces);
    }
    
    public CSVOptions withIgnoreSuroundingSpaces()
    {
        return withIgnoreSuroundingSpaces(true);
    }

    public CSVOptions withIgnoreSuroundingSpaces(final boolean b)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.IsIgnoreSurrountingSpaces, b);
        return newOptions;
    } 
    
    public char getDelimiter() 
    {
        return (char)get(Options.DelimiterChar);
    }

    public CSVOptions withDelimiter(final char delimiter) 
    {
        if (delimiter == 0)
            throw new IllegalArgumentException("Delimiter character must be provided");
        
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.DelimiterChar, delimiter);
        return newOptions;
    }
    
    public Character getQuote() 
    {
        return (Character)get(Options.QuoteChar);
    }

    public CSVOptions withQuote(final char c) 
    {
        return withQuote(Character.valueOf(c));
    }

    public CSVOptions withQuote(final Character quoteCharacter) 
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.QuoteChar, quoteCharacter);
        return newOptions;
    }
}
