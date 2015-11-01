package org.tms.api.io.options;



public class TMSOptions extends BaseIOOptions<TMSOptions>
{

    public static final TMSOptions Default = new TMSOptions(true, true, false, false);

    private TMSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols)
    {
        super(IOFileFormat.TMS, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    private TMSOptions (final TMSOptions format)
    {
        super(format);
    }    

    @Override
    protected TMSOptions clone(final BaseIOOptions<TMSOptions> model)
    {
        return new TMSOptions((TMSOptions)model);
    }
    
    @Override
    public TMSOptions withRowLabels()
    {
        return withRowLabels(true);
    }
    
    @Override
    public TMSOptions withRowLabels(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setRowLabels(b);
        return newOptions;
    }
    
    @Override
    public TMSOptions withColumnLabels()
    {
        return withColumnLabels(true);
    }
    
    @Override
    public TMSOptions withColumnLabels(final boolean b)
    {
        TMSOptions newOptions = new TMSOptions(this);
        newOptions.setColumnLabels(b);
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
