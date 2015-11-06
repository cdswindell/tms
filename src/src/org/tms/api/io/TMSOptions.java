package org.tms.api.io;

import org.tms.io.options.ArchivalIOOptions;

public class TMSOptions extends ArchivalIOOptions<TMSOptions> implements ArchivalIOOption<TMSOptions>
{

    public static final TMSOptions Default = new TMSOptions(true, true, false, false, true, true);

    private TMSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final boolean withDerivations,
                      final boolean withValidators)
    {
        super(IOFileFormat.TMS, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols, withDerivations, withValidators);
    }
    
    private TMSOptions (final TMSOptions format)
    {
        super(format);
    }    

    @Override
    protected TMSOptions clone(final ArchivalIOOptions<TMSOptions> model)
    {
        return new TMSOptions((TMSOptions)model);
    }   
}
