package org.tms.api.io.options;

/**
 * An {@link BaseIOOptions} where the output can be titled.
 * <p>
 * <b>Note</b>: {@code TitleableIOOption} methods only affect export operations.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see TitledPageIOOptions
 */
public interface TitleableIOOption
{
    /**
     * Return the title string to apply to the output.
     * @return the title string to apply to the output
     */
    public String getTitle();
    
    /**
     * Returns  {@code true} if this {@link BaseIOOptions} has been assigned a title string.
     * @return {@code true} if this {@code BaseIOOptions} has been assigned a title string
     */
    public boolean hasTitle();
    
    /**
     * Returns the font size, in pixels, to use to display the title string in the output.
     * @return the title font size
     */
    public int getTitleFontSize();
}
