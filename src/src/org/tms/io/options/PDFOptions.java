package org.tms.io.options;


public class PDFOptions extends FormattedPageOptions 
{
    public static final PDFOptions Default = new PDFOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx, DefaultFontFamily);
    
    private PDFOptions(final boolean rowNames, 
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
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(org.tms.io.options.IOOptions.FileFormat.PDF, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                dateTimeFormat, paged, pageNumbers, pageWidthPx, pageHeightPx, colWidthPx,
                stickyRowNames, stickyColNames, defaultFontSize, defaultFontFamily);
    }

    private PDFOptions(final PDFOptions format)
    {
        super(format);
    }

    @Override
    public PDFOptions withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public PDFOptions withRowNames(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public PDFOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public PDFOptions withColumnNames(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public PDFOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public PDFOptions withIgnoreEmptyRows(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public PDFOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public PDFOptions withIgnoreEmptyColumns(final boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public PDFOptions withTitle(String t)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setTitle(t);
        return newOptions;
    }

    @Override
    public PDFOptions withDateTimeFormat(String t)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }

    @Override
    public PDFOptions withPages()
    {
        return withPages(true);
    }

    @Override
    public PDFOptions withPages(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPaged(b);
        return newOptions;
    }

    @Override
    public PDFOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public PDFOptions withPageNumbers(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageNumbers(b);
        return newOptions;
    }

    public PDFOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    @Override
    public PDFOptions withPageWidthInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public PDFOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    @Override
    public PDFOptions withPageHeightInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public PDFOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    @Override
    public PDFOptions withColumnWidthInPx(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public PDFOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    @Override
    public PDFOptions withStickyRowNames(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setStickyRowNames(b);
        return newOptions;
    }

    @Override
    public PDFOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    @Override
    public PDFOptions withStickyColumnNames(boolean b)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setStickyColumnNames(b);
        return newOptions;
    }

    @Override
    public PDFOptions withDefaultFontSize(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
    public PDFOptions withHeadingFontSize(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
    public PDFOptions withTitleFontSize(int f)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }

    @Override
    public PDFOptions withFontFamily(String ff)
    {
        PDFOptions newOptions = new PDFOptions(this);
        newOptions.setFontFamily(ff);
        return newOptions;
    }
}
