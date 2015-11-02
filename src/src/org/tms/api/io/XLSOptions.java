package org.tms.api.io;

import org.tms.io.options.OptionEnum;

public class XLSOptions extends StyledPageIOOptions<XLSOptions>
{
    static final int DefaultColumnWidthPx = 65;
    static final int DefaultFontSizePx = 12;
    
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
