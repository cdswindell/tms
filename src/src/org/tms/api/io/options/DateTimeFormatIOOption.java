package org.tms.api.io.options;

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
public interface DateTimeFormatIOOption
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
}
