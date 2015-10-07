package org.tms.io.options;

public class HTMLOptions extends FormattedPageOptions 
{
    public static final int DefaultHTMLFontSizePx = 12;
    
    public static final HTMLOptions Default = new HTMLOptions(true, true, false, false,
                                                              DefaultColumnWidthPx, DefaultHTMLFontSizePx, "Arial");
    
    private HTMLOptions(final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final int colWidthPx,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(org.tms.io.options.IOOptions.FileFormat.HTML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                null, false, false, 0, 0, colWidthPx,
                false, false, defaultFontSize, defaultFontFamily);
    }

    private HTMLOptions(final HTMLOptions format)
    {
        super(format);
    }

    @Override
    public HTMLOptions withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public HTMLOptions withRowNames(final boolean b)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public HTMLOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public HTMLOptions withColumnNames(final boolean b)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public HTMLOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public HTMLOptions withIgnoreEmptyRows(final boolean b)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public HTMLOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public HTMLOptions withIgnoreEmptyColumns(final boolean b)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public HTMLOptions withTitle(String t)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setTitle(t);
        return newOptions;
    }

    @Override
    public HTMLOptions withDateTimeFormat(String t)
    {
        return this;
    }

    @Override
    public HTMLOptions withPages()
    {
        return withPages(true);
    }

    @Override
    public HTMLOptions withPages(boolean b)
    {
        return this;
    }

    @Override
    public HTMLOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public HTMLOptions withPageNumbers(boolean b)
    {
        return this;
    }

    public HTMLOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    @Override
    public HTMLOptions withPageWidthInPx(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public HTMLOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    @Override
    public HTMLOptions withPageHeightInPx(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public HTMLOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    @Override
    public HTMLOptions withColumnWidthInPx(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public HTMLOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    @Override
    public HTMLOptions withStickyRowNames(boolean b)
    {
        return this;
    }

    @Override
    public HTMLOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    @Override
    public HTMLOptions withStickyColumnNames(boolean b)
    {
        return this;
    }

    @Override
    public HTMLOptions withDefaultFontSize(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
    public HTMLOptions withHeadingFontSize(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
    public HTMLOptions withTitleFontSize(int f)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }

    @Override
    public HTMLOptions withFontFamily(String ff)
    {
        HTMLOptions newOptions = new HTMLOptions(this);
        newOptions.setFontFamily(ff);
        return newOptions;
    }
}
