package org.tms.io.options;

public class DOCXOptions extends FormattedPageOptions 
{
    public static final DOCXOptions Default = new DOCXOptions(true, true, false, false, DateTimeFormatPattern, 
            true, true, DefaultPageWidthPx, DefaultPageHeightPx, DefaultColumnWidthPx,
            true, true, DefaultFontSizePx);
    
    private DOCXOptions(final boolean rowNames, 
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

    private DOCXOptions(final DOCXOptions format)
    {
        super(format);
    }

    @Override
    public DOCXOptions withRowNames()
    {
        return withRowNames(true);
    }

    @Override
    public DOCXOptions withRowNames(final boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }

    @Override
    public DOCXOptions withColumnNames()
    {
        return withColumnNames(true);
    }

    @Override
    public DOCXOptions withColumnNames(final boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public DOCXOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public DOCXOptions withIgnoreEmptyRows(final boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public DOCXOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public DOCXOptions withIgnoreEmptyColumns(final boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 

    @Override
    public DOCXOptions withTitle(String t)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setTitle(t);
        return newOptions;
    }

    @Override
    public DOCXOptions withDateTimeFormat(String t)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setDateTimeFormat(t);
        return newOptions;
    }

    @Override
    public DOCXOptions withPages()
    {
        return withPages(true);
    }

    @Override
    public DOCXOptions withPages(boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setPaged(b);
        return newOptions;
    }

    @Override
    public DOCXOptions withPageNumbers()
    {
        return withPageNumbers(true);
    }

    @Override
    public DOCXOptions withPageNumbers(boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setPageNumbers(b);
        return newOptions;
    }

    public DOCXOptions withPageWidthInInches(double f)
    {
        return withPageWidthInPx((int)(f * 72));
    }

    @Override
    public DOCXOptions withPageWidthInPx(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setPageWidth(f);
        return newOptions;
    }

    public DOCXOptions withPageHeightInInches(double f)
    {
        return withPageHeightInPx((int)(f * 72));
    }

    @Override
    public DOCXOptions withPageHeightInPx(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setPageHeight(f);
        return newOptions;
    }

    public DOCXOptions withColumnWidthInInches(double f)
    {
        return withColumnWidthInPx((int)(f * 72));
    }

    @Override
    public DOCXOptions withColumnWidthInPx(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setColumnWidth(f);
        return newOptions;
    }

    @Override
    public DOCXOptions withStickyRowNames()
    {
        return withStickyRowNames(true);
    }

    @Override
    public DOCXOptions withStickyRowNames(boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setStickyRowNames(b);
        return newOptions;
    }

    @Override
    public DOCXOptions withStickyColumnNames()
    {
        return withStickyColumnNames(true);
    }

    @Override
    public DOCXOptions withStickyColumnNames(boolean b)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setStickyColumnNames(b);
        return newOptions;
    }

    @Override
    public DOCXOptions withDefaultFontSize(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setDefaultFontSize(f);
        return newOptions;
    }

    @Override
    public DOCXOptions withHeadingFontSize(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setHeadingFontSize(f);
        return newOptions;
    }

    @Override
    public DOCXOptions withTitleFontSize(int f)
    {
        DOCXOptions newOptions = new DOCXOptions(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
