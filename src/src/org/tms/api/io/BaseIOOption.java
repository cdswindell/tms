package org.tms.api.io;

public interface BaseIOOption<T extends BaseIOOption<T>>
{

    /**
     * Return the {@link IOFileFormat} enum representing the file format associated with this
     * {@code T}. 
     * @return the {@code IOFileFormat} associated with this {@code T}
     */
    public IOFileFormat getFileFormat();

    /**
     * Returns {@code true} if this {@code T} supports import. Currently,
     * {@link CSVOptions}, {@link XLSOptions}, and {@link XMLOptions} all support import, 
     * meaning that TMS {@link org.tms.api.Table Table}s can be imported from
     * comma separated value (CSV) files, Excel files (both xls and xlsx formatsare supported),
     * and XML documents.
     * @return {@code true} if this {@code T} supports import
     */
    public boolean canImport();

    /**
     * Returns {@code true} if this {@code T} supports export.
     * @return {@code true} if this {@code T} supports export
     */
    public boolean canExport();

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

    /**
     * Returns {@code true} if this {@code T} is configured to ignore empty 
     * {@link org.tms.api.Row Row}s
     * when importing/exporting TMS {@link org.tms.api.Table Table}s.
     * @return {@code true} if empty {@code Row}s are ignored on import/export
     */
    public boolean isIgnoreEmptyRows();

    /**
     * Returns a new {@link T} where empty rows in the imported file are ignored, and where
     * empty {@link org.tms.api.Row Row}s in a {@link org.tms.api.Table Table} are ignored and not
     * included in the export file. 
     * @return a new {@link T} where empty rows are ignored
     */
    public T withIgnoreEmptyRows();

    /**
     * Sets the behavior this {@link T} uses to handle empty rows. When set to {@code true},
     * empty rows in the source {@link org.tms.api.Table Table} are ignored (not written to the output file). 
     * On import, empty rows in the source file are ignored.
     * When set to {@code false}, empty rows are included in the exported file, in a file-format-appropriate
     * manner. On import, empty {@link org.tms.api.Row Row}s are inserted into the new {@link org.tms.api.Table Table}
     * when encountered in the imported file.
     * @param enabled {@code true} to import/export empty {@code Row}s, {@code false} to ignore them
     * @return a new {@link T} where empty rows are or are not ignored
     */
    public T withIgnoreEmptyRows(boolean enabled);

    /**
     * Returns {@code true} if this {@code T} is configured to ignore empty 
     * {@link org.tms.api.Column Column}s
     * when importing/exporting TMS {@link org.tms.api.Table Table}s.
     * @return {@code true} if empty {@code Column}s are ignored on import/export
     */
    public boolean isIgnoreEmptyColumns();

    /**
     * Returns a new {@link T} where empty columns in the imported file are ignored, and where
     * empty {@link org.tms.api.Column Column}s in a {@link org.tms.api.Table Table} are ignored and not
     * included in the export file. 
     * @return a new {@link T} where empty columns are ignored
     */
    public T withIgnoreEmptyColumns();

    /**
     * Sets the behavior this {@link T} uses to handle empty columns. When set to {@code true},
     * empty columns in the source {@link org.tms.api.Table Table} are ignored (not written to the output file). 
     * On import, empty columns in the source file are ignored.
     * When set to {@code false}, empty columns are included in the exported file, in a file-format-appropriate
     * manner. On import, empty {@link org.tms.api.Column Column}s are inserted into the new {@link org.tms.api.Table Table}
     * when encountered in the imported file.
     * @param enabled {@code true} to import/export empty {@code Column}s, {@code false} to ignore them
     * @return a new {@link T} where empty columns are or are not ignored
     */
    public T withIgnoreEmptyColumns(boolean enabled);

}