package org.tms.api.io;

import org.tms.io.options.ArchivalIOOptions;
import org.tms.io.options.OptionEnum;

public class TCOptions extends TMSOptions 
{
    public static final TCOptions Default = new TCOptions(true, true, false, false, true, true, true);

    private enum Options implements OptionEnum 
    {
        PersistantTables,
        NonPersistantTables,
        Constants,
        Operators,
    }
    
    private TCOptions(boolean rowNames, boolean colNames, boolean ignoreEmptyRows,
                      boolean ignoreEmptyCols, boolean withDerivations, boolean withValidators,
                      boolean withState)
    {
        super(rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols, withDerivations,
                withValidators, withState);
        
        set(Options.PersistantTables, true);
        set(Options.NonPersistantTables, true);
        set(Options.Constants, true);
        set(Options.Operators, true);
    }
        
    private TCOptions (final TCOptions format)
    {
        super(format);
    }    

    @Override
    protected TCOptions clone(final ArchivalIOOptions<TMSOptions> model)
    {
        return new TCOptions((TCOptions)model);
    } 
    
    @Override
    public boolean canImport()
    {
        return false;
    }

    @Override
    public boolean canExport()
    {
        return false;
    }
    
    public boolean isPersistantTables()
    {
        return isSet(Options.PersistantTables);
    }
    
    public boolean isNonPersistantTables()
    {
        return isSet(Options.NonPersistantTables);
    }
    
    public boolean isConstants()
    {
        return isSet(Options.Constants);
    }
    
    public boolean isOperators()
    {
        return isSet(Options.Operators);
    }
}
