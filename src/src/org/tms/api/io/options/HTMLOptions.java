package org.tms.api.io.options;


public class HTMLOptions extends TitledPageOptions<HTMLOptions> 
{
    static final int DefaultHTMLFontSizePx = 12;
    
    public static final HTMLOptions Default = new HTMLOptions(true, true, false, false,
                                                              DefaultColumnWidthPx, DefaultHTMLFontSizePx, "Arial");
    
    private HTMLOptions(final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final int colWidthPx,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(org.tms.api.io.options.IOOptions.FileFormat.HTML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
              colWidthPx, defaultFontSize, defaultFontFamily);
    }

    private HTMLOptions(final HTMLOptions format)
    {
        super(format);
    }

    @Override
    protected HTMLOptions clone(final TitledPageOptions<HTMLOptions> model)
    {
        return new HTMLOptions((HTMLOptions)model);
    }
}
