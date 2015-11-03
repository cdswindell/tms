package org.tms.io.options;

import org.tms.api.io.IOFileFormat;
import org.tms.api.io.StyleableIOOption;
import org.tms.api.io.IOOption;

/**
 * The base class that {@link IOOption} that support display styling extend.
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
 * <b>Note</b>: {@code StyleableIOOption} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code StyledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see StyleableIOOption
 */
public abstract class StyledPageIOOptions<T extends StyledPageIOOptions<T>> 
    extends BaseIOOptions<T> 
    implements StyleableIOOption<T>
{
    protected static final int DefaultColumnWidthPx = 65;
    protected static final int DefaultFontSizePx = 8;
    protected static final String DefaultFontFamily = "SansSerif";
    
    private enum Options implements OptionEnum 
    {
        ColumnWidth,
        RowLabelColumnWidth,
        DefaultFontSize,
        HeadingFontSize,
        FontFamily;
    }

    protected abstract T clone(final StyledPageIOOptions<T> model);
    
    protected StyledPageIOOptions(final IOFileFormat format,
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

    protected StyledPageIOOptions(final StyledPageIOOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(final BaseIOOptions<T> model)
    {
        return clone((StyledPageIOOptions<T>) model);
    }

    @Override
    public int getDefaultColumnWidth()
    {
        final Object d = get(Options.ColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setColumnWidth(int i)
    {
        set(Options.ColumnWidth, i);
    }

    @Override
    public T withDefaultColumnWidthInInches(double colWidth)
    {
        return withDefaultColumnWidthInPx((int)(colWidth * 72));
    }

    @Override
    public T withDefaultColumnWidthInPx(int colWidth)
    {
        final T newOptions = clone(this);
        newOptions.setColumnWidth(colWidth);
        return newOptions;
    }   

    @Override
    public int getRowLabelColumnWidth()
    {
        final Object d = get(Options.RowLabelColumnWidth);
        return d != null ? (int)d : 0;
    }

    protected void setRowLabelColumnWidth(int i)
    {
        set(Options.RowLabelColumnWidth, i);
    }

    @Override
    public T withRowLabelColumnWidthInInches(double colWidth)
    {
        return withRowLabelColumnWidthInPx((int)(colWidth * 72));
    }

    @Override
    public T withRowLabelColumnWidthInPx(int colWidth)
    {
        final T newOptions = clone(this);
        newOptions.setRowLabelColumnWidth(colWidth);
        return newOptions;
    }
    
    @Override
    public int getDefaultFontSize()
    {
        final Object d = get(Options.DefaultFontSize);
        return d != null ? (int)d : DefaultFontSizePx;
    }

    protected void setDefaultFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Default font size must be at least 4px");

        set(Options.DefaultFontSize, i);
    }

    @Override
    public T withDefaultFontSize(int fontSize)
    {
        final T newOptions = clone(this);
        newOptions.setDefaultFontSize(fontSize);
        return newOptions;
    }

    @Override
    public int getHeadingFontSize()
    {
        final Object d = get(Options.HeadingFontSize);
        return d != null ? (int)d : (int)(getDefaultFontSize() * 1.2);
    }

    protected void setHeadingFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Heading font size must be at least 4px");

        set(Options.HeadingFontSize, i);
    }
    
    @Override
    public T withHeadingFontSize(int fontSize)
    {
        final T newOptions = clone(this);
        newOptions.setHeadingFontSize(fontSize);
        return newOptions;
    }

    @Override
    public String getFontFamily()
    {
        return (String)get(Options.FontFamily);
    }

    protected void setFontFamily(String ff)
    {
        set(Options.FontFamily, ff);
    }
    
    @Override
    public T withFontFamily(String fontFamily)
    {
        final T newOptions = clone(this);
        newOptions.setFontFamily(fontFamily);
        return newOptions;
    }
}
