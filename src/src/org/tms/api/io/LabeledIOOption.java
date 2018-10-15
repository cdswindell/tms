package org.tms.api.io;

/**
 * The base for all {@code IOOption}, which allow the processing of {@link org.tms.api.Row Row}
 * and {@link org.tms.api.Column Column} labels
 * {@link org.tms.api.Table Table}s when importing and exporting. 
 * {@code LabeledIOOption} and its super-interfaces support methods that determine what and how TMS data
 * is imported and exported. For example, methods exist to set output titles and font sizes, for
 * formats that support titles.
 * {@code IOOption} utilize the Builder design pattern, meaning that
 * methods that modify instance objects all return a new {@code IOOption} instance.
 * This allows different methods to be chained together, such as:
 * <blockquote><pre>
 * PDFOptions.Default.withRowNames(false).withDescriptions(false).withTitle("Active Compounds")
 * </pre></blockquote>
 * Each concrete class implementing {@code IOOption} or one of its super-interfaces defines
 * a {@code public static} instance named {@code Default} which can be further modified, as needed.
 * <p>
 * @param <T> the type of {@link LabeledIOOption} in this {@code IOOption}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface LabeledIOOption<T extends LabeledIOOption<T>> extends IOOption<T> 
{
    /**
     * Returns {@code true} if this {@code T} is configured to import or export
     * TMS {@link org.tms.api.Column Column} labels.
     * @return {@code true} if this {@code T} imports/export {@code Column} labels
     */
    public boolean isColumnLabels();

    /**
     * Returns a new {@link T} with column labels enabled. When
     * exporting, this means that labels assigned to the {@link org.tms.api.Column Column}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output.
     * When importing, this means that column labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Column Column}s read from the file.
     * @return a new {@link T} with column labels enabled
     */
    public T withColumnLabels();

    /**
     * Returns a new {@link T} with column labels enabled or disabled. When
     * exporting, this means that when enabled, labels assigned to the {@link org.tms.api.Column Column}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output. When disabled,
     * column labels in the exported table are ignored and are not included in the output.
     * When importing, this means that when enabled, column labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Column Column}s read from the file.
     * @param enabled {@code true} to include {@code Column} labels, {@code false} to ignore them
     * @return a new {@link T} with column labels enabled or disabled
     */
    public T withColumnLabels(boolean enabled);

    /**
     * Returns {@code true} if this {@code T} is configured to import or export
     * TMS {@link org.tms.api.Row Row} labels.
     * @return {@code true} if this {@code T} imports/export {@code Row} labels
     */
    public boolean isRowLabels();

    /**
     * Returns a new {@link T} with row labels enabled. When
     * exporting, this means that labels assigned to the {@link org.tms.api.Row Row}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output.
     * When importing, this means that row labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Row Row}s read from the file.
     * @return a new {@link T} with row labels enabled
     */
    public T withRowLabels();

    /**
     * Returns a new {@link T} with row labels enabled or disabled, as per the
     * supplied parameter {@code b}. When
     * exporting, this means that when enabled, labels assigned to the {@link org.tms.api.Row Row}s
     * in the exported {@link org.tms.api.Table Table} will be included in the output. When disabled,
     * row labels in the exported table are ignored and are not included in the output.
     * When importing, this means that when enabled, row labels assigned to the data in the import file
     * will be assigned to the {@link org.tms.api.Row Row}s read from the file.
     * @param enabled {@code true} to include {@code Row} labels, {@code false} to ignore them
     * @return a new {@link T} with row labels enabled or disabled
     */
    public T withRowLabels(boolean enabled);
}