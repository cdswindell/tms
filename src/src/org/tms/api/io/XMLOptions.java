package org.tms.api.io;

import org.tms.io.options.ArchivalIOOptions;

public class XMLOptions extends ArchivalIOOptions<XMLOptions> implements ArchivalIOOption<XMLOptions>
{
    public static final XMLOptions Default = new XMLOptions(true, true, false, false, true, false, false);

    private XMLOptions(final boolean rowNames, 
                       final boolean colNames, 
                       final boolean ignoreEmptyRows, 
                       final boolean ignoreEmptyCols,
                       final boolean withDerivations,
                       final boolean withValidators,
                       final boolean withState)
    {
        super(IOFileFormat.XML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols, 
        		withDerivations, withValidators, withState);
    }
    
    private XMLOptions (final XMLOptions format)
    {
        super(format);
    }
    
    @Override
    protected XMLOptions clone(final ArchivalIOOptions<XMLOptions> model)
    {
        return new XMLOptions((XMLOptions)model);
    }    
}
