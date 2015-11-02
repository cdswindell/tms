package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;

public class XMLOptions extends BaseIOOptions<XMLOptions> implements BaseIOOption<XMLOptions>
{
    public static final XMLOptions Default = new XMLOptions(true, true, false, false);

    private XMLOptions(final boolean rowNames, 
                       final boolean colNames, 
                       final boolean ignoreEmptyRows, 
                       final boolean ignoreEmptyCols)
    {
        super(IOFileFormat.XML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    private XMLOptions (final XMLOptions format)
    {
        super(format);
    }
    
    @Override
    protected XMLOptions clone(final BaseIOOptions<XMLOptions> model)
    {
        return new XMLOptions((XMLOptions)model);
    }    
}
