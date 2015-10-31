package org.tms.api.io.options;

/**
 * An {@link IOOptions} where the output can be titled.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface TitleableOption
{
    /**
     * Return the title string to apply to the output.
     * @return the title string to apply to the output
     */
    public String getTitle();
    
    /**
     * Returns  {@code true} if this {@link IOOptions} has been assigned a title string.
     * @return {@code true} if this {@code IOOptions} has been assigned a title string
     */
    public boolean hasTitle();
    
    /**
     * Returns the font size, in pixels, to use to display the title string in the output.
     * @return the title font size
     */
    public int getTitleFontSize();
}
