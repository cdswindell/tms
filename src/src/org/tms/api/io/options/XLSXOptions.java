package org.tms.api.io.options;

import org.tms.io.options.StyledPageOptions;

public class XLSXOptions extends StyledPageOptions<XLSXOptions>
{
    public static final int DefaultColumnWidthPx = (int) 65;
    public static final int DefaultFontSizePx = 10;
    public static final String DefaultFontFamily = "SansSerif";
    
    public static final XLSXOptions Default = new XLSXOptions(true, true, false, false, DefaultColumnWidthPx, DefaultFontSizePx, DefaultFontFamily);

    private XLSXOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final int colWidthPx,
                      final int defaultFontSize,
                      final String defaultFontFamily)
    {
        super(org.tms.io.options.IOOptions.FileFormat.EXCEL, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                colWidthPx, defaultFontSize, defaultFontFamily);
    }
    
    private XLSXOptions (final XLSXOptions format)
    {
        super(format);
    }
    
    @Override
    protected XLSXOptions clone(final StyledPageOptions<XLSXOptions> model)
    {
        return new XLSXOptions((XLSXOptions)model);
    }
    
    @Override
    public XLSXOptions withRowNames()
    {
        return withRowNames(true);
    }
    
    @Override
    public XLSXOptions withRowNames(final boolean b)
    {
        XLSXOptions newOptions = new XLSXOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }
    
    @Override
    public XLSXOptions withColumnNames()
    {
        return withColumnNames(true);
    }
    
    @Override
    public XLSXOptions withColumnNames(final boolean b)
    {
        XLSXOptions newOptions = new XLSXOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public XLSXOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    @Override
    public XLSXOptions withIgnoreEmptyRows(final boolean b)
    {
        XLSXOptions newOptions = new XLSXOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public XLSXOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public XLSXOptions withIgnoreEmptyColumns(final boolean b)
    {
        XLSXOptions newOptions = new XLSXOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    }
}
