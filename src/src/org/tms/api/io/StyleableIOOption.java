package org.tms.api.io;

/**
 * An {@link IOOption} where the output can be Styled. Style elements
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
 * <b>Note</b>: {@code StyleableIOOption} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code StyledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface StyleableIOOption<T extends StyleableIOOption<T>> 
{   
    /**
     * Return the default column width, in pixels, used to display
     * {@link org.tms.api.Column Column} data.
     * @return the default column width, in pixels
     */
    public int getDefaultColumnWidth();
    
    /**
     * Set the default width, in inches, used to display table column data in exports
     * @param colWidth the new default column width, in inches
     * @return a new {@link T} with the default column width set
     */
    public T withDefaultColumnWidthInInches(double colWidth);
    
    /**
     * Set the default width, in pixels, used to display table column data in exports
     * @param colWidth the new default column width, in pixels
     * @return a new {@link T} with the default column width set
     */
    public T withDefaultColumnWidthInPx(int colWidth);
    
      /**
     * The default width, in pixels, used to display the column where 
     * {@link org.tms.api.Row Row} labels are displayed in the exported file.
     * @return the default row label column width, in pixels
     */
    public int getRowLabelColumnWidth();
    
    /**
     * Set the width, in inches, used to display row labels in exports.
     * @param colWidth the new row label column width, in inches
     * @return a new {@link T} with the row label column width set
     */
    public T withRowLabelColumnWidthInInches(double colWidth);
    
    /**
     * Set the width, in pixels, used to display row labels in exports.
     * @param colWidth the new row label column width, in pixels
     * @return a new {@link T} with the row label column width set
     */
    public T withRowLabelColumnWidthInPx(int colWidth);
    
    /**
     * Return the default font size, in pixels, used to display table cell data in exports.
     * @return the default font size, in pixels, used to display table cell data
     */
    public int getDefaultFontSize();
    
    /**
     * Set the default font size, in pixels, used to display table data in exports.
     * @param fontSize the new default font size, in pixels
     * @return a new {@link T} with the default font size set
     */
    public T withDefaultFontSize(int fontSize);
    
    /**
     * Return the font size used to display row and column labels, in pixels.
     * @return the font size used to display row and column labels, in pixels
     */
    public int getHeadingFontSize();
    
    /**
     * Set the font size, in pixels, used to display row and column labels in exports.
     * @param fontSize the new heading font size, in pixels
     * @return a new {@link T} with the heading font size set
     */
    public T withHeadingFontSize(int fontSize);
    
    /**
     * Returns the font family used to generate the table output in the exported file.
     * Allowable values are file format-dependent and must refer to font names that are 
     * defined/native in the file format.
     * @return the font family used to generate the table output in the exported file
     */
    public String getFontFamily();
    
    /**
     * Sets the font family used to display table data in exports. The font family is
     * expressed as the string name of the font, as it appears or is represented in the 
     * native export format. For example, to use the font "Chalkboard" in MS Word, {@code fontFamily}
     * is set to {@code "Chalkboard"}. 
     * <p>
     * Note that TMS does not provide any font family name translation services;
     * it is the responsibility of the caller to specify a correct value for each export format.
     * 
     * @param fontFamily the name of the font family used to display table data in exports
     * @return a new {@link T} with the font family size set
     */
    public T withFontFamily(String fontFamily);
}
