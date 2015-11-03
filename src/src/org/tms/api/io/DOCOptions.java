package org.tms.api.io;

import org.tms.io.options.FormattedPageIOOptions;

/**
 * {@code DocOptions} is a concrete class for representing configuration options to export 
 * {@link org.tms.api.Table Table}s in MS Word format.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class DOCOptions extends FormattedPageIOOptions<DOCOptions> 
        implements PageableIOOption<DOCOptions>, DateTimeFormatIOOption<DOCOptions>
{
    /**
     * Constant with the most common MS Word export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code false}</b></li>
     * <li>Ignore Empty Columns: <b>{@code false}</b></li>
     * <li>Date-Time Format Pattern: <b>{@code MM/dd/yyyy hh:mm a}</b></li>
     * <li>Paged: <b>{@code true}</b></li>
     * <li>Page Numbers: <b>{@code true}</b></li>
     * <li>Page Width: <b>{@code 8.5"}</b></li>
     * <li>Page Height: <b>{@code 11"}</b></li>
     * <li>Default Column Width: <b>{@code 65px}</b></li>
     * <li>Sticky Row Labels: <b>{@code true}</b></li>
     * <li>Sticky Column Labels: <b>{@code true}</b></li>
     * <li>Default Font Size: <b>{@code 8px}</b></li>
     * <li>Default Font Family: <b>{@code Arial}</b></li>
     * </ul>
     * <p>
     * To include these default values when exporting to MS Word format, simply include {@code DocOptions.Default}
     * in the import factory method or supporting {@link org.tms.api.TableElement TableElement} export method.
     * @see org.tms.api.Table#export(String, IOOption) Table#export(String, IOOption)
     * @see org.tms.api.Table#export(java.io.OutputStream, IOOption) Table#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Row#export(String, IOOption) Row#export(String, IOOption)
     * @see org.tms.api.Row#export(java.io.OutputStream, IOOption) Row#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Column#export(String, IOOption) Column#export(String, IOOption)
     * @see org.tms.api.Column#export(java.io.OutputStream, IOOption) Column#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Subset#export(String, IOOption) Subset#export(String, IOOption)
     * @see org.tms.api.Subset#export(java.io.OutputStream, IOOption) Subset#export(java.io.OutputStream, IOOption)
     */
    public static final DOCOptions Default = new DOCOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, "Arial");
    
    private DOCOptions(final boolean rowNames, 
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

    private DOCOptions(final DOCOptions format)
    {
        super(format);
    }

    @Override
    protected DOCOptions clone(final FormattedPageIOOptions<DOCOptions> model)
    {
        return new DOCOptions((DOCOptions)model);
    }
}
