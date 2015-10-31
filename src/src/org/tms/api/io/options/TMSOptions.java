package org.tms.api.io.options;


public class TMSOptions extends IOOptions
{

    public static final TMSOptions Default = new TMSOptions(true, true, false, false);

    private TMSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols)
    {
        super(org.tms.api.io.options.IOOptions.FileFormat.TMS, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    private TMSOptions (final TMSOptions format)
    {
        super(format);
    }
    
    @Override
    public TMSOptions withRowNames()
    {
        return withRowNames(true);
    }
    
    @Override
    public TMSOptions withRowNames(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }
    
    @Override
    public TMSOptions withColumnNames()
    {
        return withColumnNames(true);
    }
    
    @Override
    public TMSOptions withColumnNames(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public TMSOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }

    @Override
    public TMSOptions withIgnoreEmptyRows(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public TMSOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public TMSOptions withIgnoreEmptyColumns(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 
}
