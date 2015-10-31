package org.tms.api.io.options;

import org.tms.io.options.OptionEnum;

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

    public S withRowLabels()
    {
        return withRowLabels(true);
    }

    @Override
    public S withRowLabels(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setRowLabels(b);
        return newOptions;
    }

    @Override
    public S withColumnLabels()
    {
        return withColumnNames(true);
    }

    @Override
    public S withColumnNames(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setColumnLabels(b);
        return newOptions;
    }

    @Override
    public S withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public S withIgnoreEmptyRows(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public S withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public S withIgnoreEmptyColumns(final boolean b)
    {
        S newOptions = clone(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public int getColumnWidth()
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
    public int getRowNameColumnWidth()
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
