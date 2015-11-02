package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;

/**
 * An {@link BaseIOOptions} where the output can be titled.
 * <p>
 * <b>Note</b>: {@code TitleableIOOption} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link BaseIOOptions} in this {@code TitledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface TitleableIOOption<T extends TitleableIOOption<T>> extends StyleableIOOption<T> 
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
     * Assign a title string to display in the output on export.
     * @param title the title string to display in the output
     * @return a new {@link T} with the specified title string
     */
    public T withTitle(String title);
    
    /**
     * Returns the font size, in pixels, to use to display the title string in the output.
     * @return the title font size, in pixels
     */
    public int getTitleFontSize();
    
    /**
     * Set the font size, in pixels, to use when drawing the title string on export.
     * @param fontSize the new title font size, in pixels
     * @return a new {@link T} with the specified title font size
     */
    public T withTitleFontSize(int fontSize);
}
