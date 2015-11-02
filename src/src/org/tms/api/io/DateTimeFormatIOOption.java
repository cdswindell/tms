package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;
import org.tms.io.options.FormattedPageIOOptions;

/**
 * A {@link BaseIOOptions} where the output can have a date-time displayed in the output footer.
 * <p>
 * <b>Note</b>: {@code DateTimeFormatIOOption} methods only affect export operations.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see FormattedPageIOOptions
 * @see java.text.SimpleDateFormat SimpleDateFormat
 */
public interface DateTimeFormatIOOption<T extends DateTimeFormatIOOption<T>>
{
    /**
     * Returns the format pattern string used to display date-time values in export footers, if one has been defined. 
     * The format string is a pattern describing the date and time format, as defined by {@link java.text.SimpleDateFormat SimpleDateFormat}.
     * @return the format string to be used to display date-time values in export footers
     * @see java.text.SimpleDateFormat
     */
    public String getDateTimeFormat();
    
    /**
     * Returns {@code true} if a custom date-time format pattern has been defined.
     * @return {@code true} if a custom date-time format pattern has been defined
     */
    public boolean hasDateTimeFormat();
    
    /**
     * Set the date-time format pattern to use to display the time and date the
     * in the page footnotes of the export. The date-time format pattern follows the conventions 
     * described in {@link java.text.SimpleDateFormat SimpleDateFormat}. 
     * To disable the display of the date-time in page footers, set {@code pattern} to {@code null}.
     * <p>
     * The default value is <b>MM/dd/yyyy hh:mm a</b>
     * @param pattern the new date-time format pattern or {@code null} to disable
     * @return a new {@link T} with the date-time format pattern set
     * @see java.text.SimpleDateFormat SimpleDateFormat
     */
    public T withDateTimeFormat(String pattern);
}
