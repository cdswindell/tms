package org.tms.api.io;

import org.tms.io.options.TitledPageIOOptions;

/**
 * {@code HTMLOptions} is a concrete class for representing configuration options to export 
 * {@link org.tms.api.Table Table}s in HTML format.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class HTMLOptions extends TitledPageIOOptions<HTMLOptions> implements TitleableIOOption<HTMLOptions>, LabeledIOOption<HTMLOptions>
{
    static final int DefaultHTMLFontSizePx = 12;
    
    /**
     * Constant with the most common HTML export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code false}</b></li>
     * <li>Ignore Empty Columns: <b>{@code false}</b></li>
     * <li>Default Column Width: <b>{@code 65px}</b></li>
     * <li>Default Font Size: <b>{@code 12px}</b></li>
     * <li>Default Font Family: <b>{@code Arial}</b></li>
     * </ul>
     * <p>
     * To include these default values when exporting to HTML, simply include {@code HTMLOptions.Default}
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
        super(IOFileFormat.HTML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
              colWidthPx, defaultFontSize, defaultFontFamily);
    }

    private HTMLOptions(final HTMLOptions format)
    {
        super(format);
    }

    @Override
    protected HTMLOptions clone(final TitledPageIOOptions<HTMLOptions> model)
    {
        return new HTMLOptions((HTMLOptions)model);
    }
}
