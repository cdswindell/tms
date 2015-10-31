package org.tms.api.io.options;



public class PDFOptions extends FormattedPageOptions<PDFOptions> 
{
    static final String DefaultFontFamilyPDF = "Helvetica";
    
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, DefaultFontFamilyPDF);
    
    private PDFOptions(final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final String dateTimeFormat,
            final boolean paged,
            final boolean pageNumbers,
            final int pageWidthPx,
            final int pageHeightPx,
            final int colWidthPx,
            final boolean stickyRowNames,
            final boolean stickyColNames,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(IOFileFormat.PDF, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize, defaultFontFamily);
    }

    private PDFOptions(final PDFOptions format)
    {
        super(format);
    }

    @Override
    protected PDFOptions clone(final FormattedPageOptions<PDFOptions> model)
    {
        return new PDFOptions((PDFOptions)model);
    }
}
