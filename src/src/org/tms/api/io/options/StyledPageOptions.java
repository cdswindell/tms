package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

/**
 * The base class which {@link IOOptions} that support display styling extend.
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
        RowNameColumnWidth,
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

    @Override
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

    public S withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    public S withColumnWidthInPx(int f)
    {
        S newOptions = clone(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public int getRowLabelColumnWidth()
    {
        Object d = get(Options.RowNameColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setRowNameColumnWidth(int i)
    {
        set(Options.RowNameColumnWidth, i);
    }

    public S withRowNameColumnWidthInInches(double f)
    {
        return withRowNameColumnWidthInPx((int)(f * 72));
    }

    public S withRowNameColumnWidthInPx(int f)
    {
        S newOptions = clone(this);
        newOptions.setRowNameColumnWidth(f);
        return newOptions;
    }
    
    @Override
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

    public S withDefaultFontSize(int f)
    {
        S newOptions = clone(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
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
    
    public S withHeadingFontSize(int f)
    {
        S newOptions = clone(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
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
    
    public S withFontFamily(String ff)
    {
        S newOptions = clone(this);
        newOptions.setFontFamily(ff);
        return newOptions;
    }
}
