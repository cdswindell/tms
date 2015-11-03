package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;

public class TMSOptions extends BaseIOOptions<TMSOptions> implements IOOption<TMSOptions>
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
}
