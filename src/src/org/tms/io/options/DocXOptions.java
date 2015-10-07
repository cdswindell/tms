package org.tms.io.options;

public class DocXOptions extends FormattedPageOptions 
{
    public static final DocXOptions Default = new DocXOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx);
    
    private DocXOptions(final boolean rowNames, 
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
        super(org.tms.io.options.IOOptions.FileFormat.DOCX, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize);
    }

    private DocXOptions(final DocXOptions format)
    {
        super(format);
    }

    @Override
    public DocXOptions withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public DocXOptions withRowNames(final boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public DocXOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public DocXOptions withColumnNames(final boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public DocXOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public DocXOptions withIgnoreEmptyRows(final boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public DocXOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public DocXOptions withIgnoreEmptyColumns(final boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public DocXOptions withTitle(String t)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setTitle(t);
        return newOptions;
    }

    @Override
    public DocXOptions withDateTimeFormat(String t)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }

    @Override
    public DocXOptions withPages()
    {
        return withPages(true);
    }

    @Override
    public DocXOptions withPages(boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setPaged(b);
        return newOptions;
    }

    @Override
    public DocXOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public DocXOptions withPageNumbers(boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setPageNumbers(b);
        return newOptions;
    }

    public DocXOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    @Override
    public DocXOptions withPageWidthInPx(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public DocXOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    @Override
    public DocXOptions withPageHeightInPx(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public DocXOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    @Override
    public DocXOptions withColumnWidthInPx(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public DocXOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    @Override
    public DocXOptions withStickyRowNames(boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setStickyRowNames(b);
        return newOptions;
    }

    @Override
    public DocXOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    @Override
    public DocXOptions withStickyColumnNames(boolean b)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setStickyColumnNames(b);
        return newOptions;
    }

    @Override
    public DocXOptions withDefaultFontSize(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
    public DocXOptions withHeadingFontSize(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
    public DocXOptions withTitleFontSize(int f)
    {
        DocXOptions newOptions = new DocXOptions(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
