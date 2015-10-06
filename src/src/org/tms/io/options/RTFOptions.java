package org.tms.io.options;

public class RTFOptions extends FormattedPageOptions 
{
    public static final RTFOptions Default = new RTFOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx);
    
    private RTFOptions(final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final String dateTimeFormat,
            final boolean paged,
            final boolean pageNumbers,
            final int pageWidthPx,
            final int pageHeightPx,
            final int colWidthPx,
            final boolean stickyRowNames,
            final boolean stickyColNames,
            final int defaultFontSize)
    {
        super(org.tms.io.options.IOOptions.FileFormat.RTF, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize);
    }

    private RTFOptions(final RTFOptions format)
    {
        super(format);
    }

    @Override
    public RTFOptions withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public RTFOptions withRowNames(final boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public RTFOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public RTFOptions withColumnNames(final boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public RTFOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public RTFOptions withIgnoreEmptyRows(final boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public RTFOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public RTFOptions withIgnoreEmptyColumns(final boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public RTFOptions withTitle(String t)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setTitle(t);
        return newOptions;
    }

    @Override
    public RTFOptions withDateTimeFormat(String t)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }

    @Override
    public RTFOptions withPages()
    {
        return withPages(true);
    }

    @Override
    public RTFOptions withPages(boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setPaged(b);
        return newOptions;
    }

    @Override
    public RTFOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public RTFOptions withPageNumbers(boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setPageNumbers(b);
        return newOptions;
    }

    public RTFOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    @Override
    public RTFOptions withPageWidthInPx(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public RTFOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    @Override
    public RTFOptions withPageHeightInPx(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public RTFOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    @Override
    public RTFOptions withColumnWidthInPx(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public RTFOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    @Override
    public RTFOptions withStickyRowNames(boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setStickyRowNames(b);
        return newOptions;
    }

    @Override
    public RTFOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    @Override
    public RTFOptions withStickyColumnNames(boolean b)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setStickyColumnNames(b);
        return newOptions;
    }

    @Override
    public RTFOptions withDefaultFontSize(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
    public RTFOptions withHeadingFontSize(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
    public RTFOptions withTitleFontSize(int f)
    {
        RTFOptions newOptions = new RTFOptions(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
