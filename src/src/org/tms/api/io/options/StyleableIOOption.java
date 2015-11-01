package org.tms.api.io.options;

/**
 * An {@link BaseIOOptions} where the output can be Styled. Style elements
 * that are supported include:
 * <ul>
 * <li>column widths,</li> 
 * <li>row label column widths,</li>
 * <li>table cell value font size,</li>
 * <li>heading font size, and</li>
 * <li>font family.</li>
 * </ul>
 * All widths and sizes are in pixels, unless otherwise noted.
 * <p>
 * <b>Note</b>: {@code StyleableOption} methods only affect export operations.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see StyledPageIOOptions
 */
public interface StyleableIOOption
{   
    /**
     * Return the default column width, in pixels, used to display
     * {@link org.tms.api.Column Column} data.
     * @return the default column width, in pixels
     */
    public int getDefaultColumnWidth();
    
    /**
     * The default width, in pixels, used to display the column where 
     * {@link org.tms.api.Row Row} labels are displayed in the exported file.
     * @return the default row label column width, in pixels
     */
    public int getRowLabelColumnWidth();
    
    /**
     * Return the default font size, in pixels, used to display table cell data in exports.
     * @return the default font size, in pixels, used to display table cell data
     */
    public int getDefaultFontSize();
    
    /**
     * Return the font size used to display row and column labels, in pixels.
     * @return the font size used to display row and column labels, in pixels
     */
    public int getHeadingFontSize();
    
    /**
     * Returns the font family used to generate the table output in the exported file.
     * Allowable values are file format-dependent and must refer to font names that are 
     * defined/native in the file format.
     * @return the font family used to generate the table output in the exported file
     */
    public String getFontFamily();
}
