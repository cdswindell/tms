package org.tms.api.io;

import org.tms.io.options.IOConstants;
import org.tms.io.options.LabeledIOOptions;
import org.tms.io.options.OptionEnum;

/**
 * {@code CSVOptions} is a concrete class for representing configuration options to import and export 
 * comma-separated values (CSV) data.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class CSVOptions extends LabeledIOOptions<CSVOptions> implements LabeledIOOption<CSVOptions>
{
    /**
     * Constant with the most common CSV import and export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code false}</b></li>
     * <li>Ignore Empty Columns: <b>{@code false}</b></li>
     * <li>Delimiter Character: {@code '}<b>{@code ,}</b>{@code '}</li>
     * <li>Quote Character: {@code '}<b>{@code "}</b>{@code '}</li>
     * <li>Ignore Surrounding Spaces: <b>{@code true}</b></li>
     * </ul>
     * <p>
     * To include these default values when importing or exporting CSV data, simply include {@code CSVOptions.Default}
     * in the import factory method or supporting {@link org.tms.api.TableElement TableElement} export method.
     * @see org.tms.api.factories.TableFactory#importFile(String, org.tms.api.TableContext, IOOption) TableFactory#importFile(String, org.tms.api.TableContext, IOOption)
     * @see org.tms.api.Table#export(String, IOOption) Table#export(String, BaseIOOptions)
     * @see org.tms.api.Table#export(java.io.OutputStream, IOOption) Table#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Row#export(String, IOOption) Row#export(String, IOOption)
     * @see org.tms.api.Row#export(java.io.OutputStream, IOOption) Row#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Column#export(String, IOOption) Column#export(String, IOOption)
     * @see org.tms.api.Column#export(java.io.OutputStream, IOOption) Column#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Subset#export(String, IOOption) Subset#export(String, IOOption)
     * @see org.tms.api.Subset#export(java.io.OutputStream, IOOption) Subset#export(java.io.OutputStream, IOOption)
     */
    public static final CSVOptions Default = new CSVOptions(true, true, false, false, IOConstants.COMMA, IOConstants.DOUBLE_QUOTE_CHAR, true);

    private enum Options implements OptionEnum 
    {
        DelimiterChar,
        QuoteChar,
        IgnoreSurrountingSpaces, 
    	IgnoreLeadingZeros
    }
    
    private CSVOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final Character delimiter,
                      final Character quoteCharacter,
                      final boolean ignoreSurroundingSpaces)
    {
        super(IOFileFormat.CSV, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
        set(Options.DelimiterChar, delimiter);
        set(Options.QuoteChar, quoteCharacter);
        set(Options.IgnoreSurrountingSpaces, ignoreSurroundingSpaces);
        set(Options.IgnoreLeadingZeros, false);
    }
    
    private CSVOptions (final CSVOptions format)
    {
        super(format);
    }
    
    @Override
    protected CSVOptions clone(LabeledIOOptions<CSVOptions> model)
    {
        return new CSVOptions((CSVOptions)model);
    }
    
    /**
     * Returns {@code true} if white space is ignored in the CSV data.
     * @return {@code true} if white space is ignored
     */
    public boolean isIgnoreSuroundingSpaces()
    {
        return isTrue(Options.IgnoreSurrountingSpaces);
    }
    
    /**
     * Ignore surrounding spaces (white space) when importing CSV data.
     * @return a new {@link CSVOptions} that is equal to this with the Ignore Surrounding Spaces property set to {@code true}
     */
    public CSVOptions withIgnoreSuroundingSpaces()
    {
        return withIgnoreSuroundingSpaces(true);
    }

    /**
     * Enable or disable ignoring white space when importing or exporting
     * @param enabled {@code true} to ignore white space, {@code false} to import the data as is
     * @return a new {@link CSVOptions} with the Ignore Surrounding Spaces property set to {@code true} or {@code false}
     */
    public CSVOptions withIgnoreSuroundingSpaces(final boolean enabled)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.IgnoreSurrountingSpaces, enabled);
        return newOptions;
    } 
    
    public boolean isIgnoreLeadingZeros()
    {
        return isTrue(Options.IgnoreLeadingZeros);
    }
    
    public CSVOptions withIgnoreLeadingZeros()
    {
        return withIgnoreLeadingZeros(true);
    }

    public CSVOptions withIgnoreLeadingZeros(final boolean enabled)
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.IgnoreLeadingZeros, enabled);
        return newOptions;
    } 
    
   /**
     * Returns the delimiter character that separates data values in the imported or exported CSV file.
     * The default delimiter character is a comma ('<b>,</b>')
     * @return the delimiter character that separates data values
     */
    public Character getDelimiter() 
    {
        return (Character)get(Options.DelimiterChar);
    }

    /**
     * Sets the delimiter character used to separate individual data elements in the 
     * imported or exported CSV file.
     * @param delimiter the delimiter character
     * @return a new {@link CSVOptions} that is equal to this with the specified delimiter character
     * @throws IllegalArgumentException if {@code delimiter} is zero (0) or is a reserved line break character
     */
    public CSVOptions withDelimiter(final char delimiter) 
    {
        if (delimiter == 0)
            throw new IllegalArgumentException("Delimiter character must be provided");
        
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.DelimiterChar, delimiter);
        return newOptions;
    }
    
    /**
     * Returns the quote character used to surround textual data and data that includes the {@code delimiter} character 
     * within it.
     * The default quote character is a double quote ('<b>"</b>')
     * @return the quote character that surrounds textual data
     */
    public Character getQuote() 
    {
        return (Character)get(Options.QuoteChar);
    }

    /**
     * Sets the quote character which surrounds textual data and data that contains the {@code delimiter}
     * character.
     * @param quoteChar the quote character as a {@code char}
     * @return a new {@link CSVOptions} that is equal to this with the specified quote character
     */
    public CSVOptions withQuote(final char quoteChar) 
    {
        return withQuote(Character.valueOf(quoteChar));
    }

    /**
     * Sets the quote character which surrounds textual data and data that contains the {@code delimiter}
     * character.
     * @param quoteChar the quote character as a {@code java.lang.Character Character}
     * @return a new {@link CSVOptions} that is equal to this with the specified quote character
     */
    public CSVOptions withQuote(final Character quoteChar) 
    {
        CSVOptions newOptions = new CSVOptions(this);
        newOptions.set(Options.QuoteChar, quoteChar);
        return newOptions;
    }
}
