package org.tms.api.io;

import org.tms.io.options.OptionEnum;
import org.tms.io.options.StyledPageIOOptions;

/**
 * {@code XLSOptions} is a concrete class for representing configuration options to import and export 
 * {@link org.tms.api.Table Table}s from/to MS Excel in xlsx or xls format.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class XLSOptions extends StyledPageIOOptions<XLSOptions> implements StyleableIOOption<XLSOptions>
{
    static final int DefaultColumnWidthPx = 65;
    static final int DefaultFontSizePx = 12;
    
    /**
     * Constant with the most common MS Excel import and export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code false}</b></li>
     * <li>Ignore Empty Columns: <b>{@code false}</b></li>
     * <li>Default Column Width: <b>{@code 65px}</b></li>
     * <li>Default Font Size: <b>{@code 12px}</b></li>
     * <li>File Format: <b>{@code xlsx}</b></li>
     * <li>Interpret Blank Cells as Nulls: <b>{@code true}</b></li>
     * <li>Import/Export Formulas/Derivations: <b>{@code true}</b></li>
     * <li>Import/Export Comments/Descriptions: <b>{@code true}</b></li>
     * </ul>
     * <p>
     * To include these default values when exporting to MS Excel, simply include {@code XLSOptions.Default}
     * in the import factory method or supporting {@link org.tms.api.TableElement TableElement} export method.
     * @see org.tms.api.Table#export(String, BaseIOOption) Table#export(String, BaseIOOption)
     * @see org.tms.api.Table#export(java.io.OutputStream, BaseIOOption) Table#export(java.io.OutputStream, BaseIOOption)
     * @see org.tms.api.Row#export(String, BaseIOOption) Row#export(String, BaseIOOption)
     * @see org.tms.api.Row#export(java.io.OutputStream, BaseIOOption) Row#export(java.io.OutputStream, BaseIOOption)
     * @see org.tms.api.Column#export(String, BaseIOOption) Column#export(String, BaseIOOption)
     * @see org.tms.api.Column#export(java.io.OutputStream, BaseIOOption) Column#export(java.io.OutputStream, BaseIOOption)
     * @see org.tms.api.Subset#export(String, BaseIOOption) Subset#export(String, BaseIOOption)
     * @see org.tms.api.Subset#export(java.io.OutputStream, BaseIOOption) Subset#export(java.io.OutputStream, BaseIOOption)
     */
    public static final XLSOptions Default = new XLSOptions(true, true, false, false, DefaultColumnWidthPx, DefaultFontSizePx);

    private enum Options implements OptionEnum 
    {
        FileFormat,
        BlanksAsNull,
        Derivations,
        Descriptions, 
        CommentAuthor
    }

    private enum ExcelFileFormat  
    {
        XLS,
        XLSX,
    }

    private XLSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final int colWidthPx,
                      final int defaultFontSize)
    {
        super(IOFileFormat.EXCEL, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                colWidthPx, defaultFontSize, null);
        
        set(Options.FileFormat, ExcelFileFormat.XLSX);
        set(Options.BlanksAsNull, true);
        set(Options.Derivations, true);
        set(Options.Descriptions, true);
    }
    
    private XLSOptions (final XLSOptions format)
    {
        super(format);
    }
    
    @Override
    protected XLSOptions clone(final StyledPageIOOptions<XLSOptions> model)
    {
        return new XLSOptions((XLSOptions)model);
    }
    
    public XLSOptions withXlsFormat()
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.FileFormat, ExcelFileFormat.XLS);
        return newOptions;
    }
    
    public XLSOptions withXlsXFormat()
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.FileFormat, ExcelFileFormat.XLSX);
        return newOptions;
    }
    
    public boolean isXlsFormat()
    {
        return get(Options.FileFormat) == ExcelFileFormat.XLS;
    }
    
    public boolean isXlsXFormat()
    {
        return get(Options.FileFormat) == ExcelFileFormat.XLSX;
    }
    
    public boolean isBlanksAsNull()
    {
        return (Boolean)get(Options.BlanksAsNull);
    }
    
    public XLSOptions withBlanksAsNull()
    {
        return withBlanksAsNull(true);
    }
    
    public XLSOptions withBlanksAsNull(boolean b)
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.BlanksAsNull, b);
        return newOptions;
    }
    
    public boolean isDerivations()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    public XLSOptions withDerivations()
    {
        return withDescriptions(true);
    }
    
    public XLSOptions withDerivations(boolean b)
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.Derivations, b);
        return newOptions;
    }
    
    public boolean isDescriptions()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    public XLSOptions withDescriptions()
    {
        return withDescriptions(true);
    }
    
    public XLSOptions withDescriptions(boolean b)
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.Descriptions, b);
        return newOptions;
    }
    
    public boolean isCommentAuthor()
    {
        return get(Options.CommentAuthor) != null;
    }
    
    public String getCommentAuthor()
    {
        return (String)get(Options.CommentAuthor);
    }
    
    public XLSOptions withCommentAuthor(String author)
    {
        XLSOptions newOptions = clone(this);
        newOptions.set(Options.CommentAuthor, author);
        return newOptions;
    }
}
