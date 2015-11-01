package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

/**
 * The base class that {@link IOOptions} that support display styling extend.
 * Style elements
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
 * @param <S> the type of {@link IOOptions} in this {@code StyledPageOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see StyleableOption
 */
public abstract class StyledPageOptions<S extends StyledPageOptions<S>> 
    extends IOOptions 
    implements StyleableOption
{
    static final int DefaultColumnWidthPx = 65;
    static final int DefaultFontSizePx = 8;
    static final String DefaultFontFamily = "SansSerif";
    
    private enum Options implements OptionEnum 
    {
        ColumnWidth,
        RowLabelColumnWidth,
        DefaultFontSize,
        HeadingFontSize,
        FontFamily;
    }

    protected abstract S clone(StyledPageOptions<S> model);
    
    protected StyledPageOptions(final IOFileFormat format,
            final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final int colWidthPx,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(format, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);

        if (defaultFontFamily != null && defaultFontFamily.trim().length() > 0)
            set(Options.FontFamily, defaultFontFamily.trim());

        set(Options.ColumnWidth, colWidthPx);
        set(Options.DefaultFontSize, defaultFontSize);
    }

    protected StyledPageOptions(final StyledPageOptions<S> format)
    {
        super(format);
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} with row labels enabled
     */
    public S withRowLabels()
    {
        return withRowLabels(true);
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} with row labels enabled or disabled
     */
    public S withRowLabels(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setRowLabels(b);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} with column labels enabled
     */
    public S withColumnLabels()
    {
        return withColumnNames(true);
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} with column labels enabled or disabled
     */
    public S withColumnNames(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setColumnLabels(b);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} where empty rows are ignored
     */
    public S withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} where empty rows are or are not ignored
     */
    public S withIgnoreEmptyRows(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} where empty columns are ignored
     */
    public S withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    /**
     * {@inheritDoc} 
     * @return a new {@link StyledPageOptions} where empty columns are or are not ignored
     */
    public S withIgnoreEmptyColumns(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    /**
     * {@inheritDoc} 
     */
    public int getDefaultColumnWidth()
    {
        Object d = get(Options.ColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setColumnWidth(int i)
    {
        set(Options.ColumnWidth, i);
    }

    /**
     * Set the default width, in inches, used to display table column data in exports
     * @param colWidth the new default column width, in inches
     * @return a new {@link StyledPageOptions} with the default column width set
     */
    public S withDefaultColumnWidthInInches(double colWidth)
    {
        return withDefaultColumnWidthInPx((int)(colWidth * 72));
    }

    /**
     * Set the default width, in pixels, used to display table column data in exports
     * @param colWidth the new default column width, in pixels
     * @return a new {@link StyledPageOptions} with the default column width set
     */
    public S withDefaultColumnWidthInPx(int colWidth)
    {
        S newOptions = clone(this);
        newOptions.setColumnWidth(colWidth);
        return newOptions;
    }
    

    /**
     * {@inheritDoc} 
     */
    public int getRowLabelColumnWidth()
    {
        Object d = get(Options.RowLabelColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setRowLabelColumnWidth(int i)
    {
        set(Options.RowLabelColumnWidth, i);
    }

    /**
     * Set the width, in inches, used to display row labels in exports.
     * @param colWidth the new row label column width, in inches
     * @return a new {@link StyledPageOptions} with the row label column width set
     */
    public S withRowLabelColumnWidthInInches(double colWidth)
    {
        return withRowLabelColumnWidthInPx((int)(colWidth * 72));
    }

    /**
     * Set the width, in pixels, used to display row labels in exports.
     * @param colWidth the new row label column width, in pixels
     * @return a new {@link StyledPageOptions} with the row label column width set
     */
    public S withRowLabelColumnWidthInPx(int colWidth)
    {
        S newOptions = clone(this);
        newOptions.setRowLabelColumnWidth(colWidth);
        return newOptions;
    }
    
    /**
     * {@inheritDoc} 
     */
    public int getDefaultFontSize()
    {
        Object d = get(Options.DefaultFontSize);
        return d != null ? (int)d : DefaultFontSizePx;
    }

    protected void setDefaultFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Default font size must be at least 4px");

        set(Options.DefaultFontSize, i);
    }

    /**
     * Set the default font size, in pixels, used to display table data in exports.
     * @param fontSize the new default font size, in pixels
     * @return a new {@link StyledPageOptions} with the default font size set
     */
    public S withDefaultFontSize(int fontSize)
    {
        S newOptions = clone(this);
        newOptions.setDefaultFontSize(fontSize);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     */
    public int getHeadingFontSize()
    {
        Object d = get(Options.HeadingFontSize);
        return d != null ? (int)d : (int)(getDefaultFontSize() * 1.2);
    }

    protected void setHeadingFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Heading font size must be at least 4px");

        set(Options.HeadingFontSize, i);
    }
    
    /**
     * Set the font size, in pixels, used to display row and column labels in exports.
     * @param fontSize the new heading font size, in pixels
     * @return a new {@link StyledPageOptions} with the heading font size set
     */
    public S withHeadingFontSize(int fontSize)
    {
        S newOptions = clone(this);
        newOptions.setHeadingFontSize(fontSize);
        return newOptions;
    }

    /**
     * {@inheritDoc} 
     */
    public String getFontFamily()
    {
        return (String)get(Options.FontFamily);
    }

    protected void setFontFamily(String ff)
    {
        set(Options.FontFamily, ff);
    }
    
    /**
     * Sets the font family used to display table data in exports. The font family is
     * expressed as the string name of the font, as it appears or is represented in the 
     * native export format. For example, to use the font "Chalkboard" in MS Word, {@code fontFamily}
     * is set to {@code "Chalkboard"}. Note that TMS does not provide any font family name translation services;
     * it is the responsibility of the caller to specify a correct value for each export format.
     * 
     * @param fontFamily the name of the font family used to display table data in exports
     * @return a new {@link StyledPageOptions} with the font family size set
     */
    public S withFontFamily(String fontFamily)
    {
        S newOptions = clone(this);
        newOptions.setFontFamily(fontFamily);
        return newOptions;
    }
}
