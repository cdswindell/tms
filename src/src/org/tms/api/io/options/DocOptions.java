package org.tms.api.io.options;



public class DocOptions extends FormattedPageOptions<DocOptions> 
{
    public static final DocOptions Default = new DocOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, "Helvetica");
    
    private DocOptions(final boolean rowNames, 
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
        super(IOFileFormat.WORD, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize, defaultFontFamily);
    }

    private DocOptions(final DocOptions format)
    {
        super(format);
    }

    @Override
    protected DocOptions clone(final FormattedPageOptions<DocOptions> model)
    {
        return new DocOptions((DocOptions)model);
    }
}
