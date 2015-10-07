package org.tms.api.io.options;

import org.tms.io.options.FormattedPageOptions;

public class RTFOptions extends FormattedPageOptions<RTFOptions> 
{
    public static final RTFOptions Default = new RTFOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, "Helvetica");
    
    private RTFOptions(final boolean rowNames, 
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
        super(org.tms.io.options.IOOptions.FileFormat.RTF, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize, defaultFontFamily);
    }

    private RTFOptions(final RTFOptions format)
    {
        super(format);
    }

    @Override
    protected RTFOptions clone(FormattedPageOptions<?> model)
    {
        return new RTFOptions((RTFOptions)model);
    }
}
