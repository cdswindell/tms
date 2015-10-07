package org.tms.io.options;

public class DocXOptions extends FormattedPageOptions<DocXOptions> 
{
    public static final DocXOptions Default = new DocXOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, "Helvetica");
    
    private DocXOptions(final boolean rowNames, 
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
        super(org.tms.io.options.IOOptions.FileFormat.DOCX, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize, defaultFontFamily);
    }

    private DocXOptions(final DocXOptions format)
    {
        super(format);
    }

    @Override
    protected DocXOptions clone(FormattedPageOptions<?> model)
    {
        return new DocXOptions((DocXOptions)model);
    }
}
