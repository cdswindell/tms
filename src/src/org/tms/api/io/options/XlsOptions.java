package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

public class XlsOptions extends StyledPageOptions<XlsOptions>
{
    static final int DefaultColumnWidthPx = 65;
    static final int DefaultFontSizePx = 12;
    
    public static final XlsOptions Default = new XlsOptions(true, true, false, false, DefaultColumnWidthPx, DefaultFontSizePx);

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

    private XlsOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final int colWidthPx,
                      final int defaultFontSize)
    {
        super(org.tms.api.io.options.IOOptions.FileFormat.EXCEL, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                colWidthPx, defaultFontSize, null);
        
        set(Options.FileFormat, ExcelFileFormat.XLSX);
        set(Options.BlanksAsNull, true);
        set(Options.Derivations, true);
        set(Options.Descriptions, true);
    }
    
    private XlsOptions (final XlsOptions format)
    {
        super(format);
    }
    
    @Override
    protected XlsOptions clone(final StyledPageOptions<XlsOptions> model)
    {
        return new XlsOptions((XlsOptions)model);
    }
    
    public XlsOptions withXlsFormat()
    {
        XlsOptions newOptions = clone(this);
        newOptions.set(Options.FileFormat, ExcelFileFormat.XLS);
        return newOptions;
    }
    
    public XlsOptions withXlsXFormat()
    {
        XlsOptions newOptions = clone(this);
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
    
    public XlsOptions withBlanksAsNull()
    {
        return withBlanksAsNull(true);
    }
    
    public XlsOptions withBlanksAsNull(boolean b)
    {
        XlsOptions newOptions = clone(this);
        newOptions.set(Options.BlanksAsNull, b);
        return newOptions;
    }
    
    public boolean isDerivations()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    public XlsOptions withDerivations()
    {
        return withDescriptions(true);
    }
    
    public XlsOptions withDerivations(boolean b)
    {
        XlsOptions newOptions = clone(this);
        newOptions.set(Options.Derivations, b);
        return newOptions;
    }
    
    public boolean isDescriptions()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    public XlsOptions withDescriptions()
    {
        return withDescriptions(true);
    }
    
    public XlsOptions withDescriptions(boolean b)
    {
        XlsOptions newOptions = clone(this);
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
    
    public XlsOptions withCommentAuthor(String author)
    {
        XlsOptions newOptions = clone(this);
        newOptions.set(Options.CommentAuthor, author);
        return newOptions;
    }
}
