package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;
import org.tms.io.options.OptionEnum;

public class JSONOptions extends BaseIOOptions<JSONOptions> implements IOOption<JSONOptions>
{
    public static final JSONOptions Default = new JSONOptions(true, true, true, true, true, false);

    private enum Options implements OptionEnum 
    {
        Derivations,
        Recalculate,
        
        VerboseState,
    }
    
    /**
     * Constant with the most common JSON import and export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code true}</b></li>
     * <li>Ignore Empty Columns: <b>{@code true}</b></li>
     * <li>Import/Export Formulas/Derivations: <b>{@code true}</b></li>
     * <li>Import/Export Full State: <b>{@code false}</b></li>
     * </ul>
     * <p>
     * To include these default values when exporting to XML, simply include {@code XMLOptions.Default}
     * in the import factory method or supporting {@link org.tms.api.TableElement TableElement} export method.
     * @see org.tms.api.Table#export(String, IOOption) Table#export(String, IOOption)
     * @see org.tms.api.Table#export(java.io.OutputStream, IOOption) Table#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Row#export(String, IOOption) Row#export(String, IOOption)
     * @see org.tms.api.Row#export(java.io.OutputStream, IOOption) Row#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Column#export(String, IOOption) Column#export(String, IOOption)
     * @see org.tms.api.Column#export(java.io.OutputStream, IOOption) Column#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Subset#export(String, IOOption) Subset#export(String, IOOption)
     * @see org.tms.api.Subset#export(java.io.OutputStream, IOOption) Subset#export(java.io.OutputStream, IOOption)
     */
    private JSONOptions(final boolean rowNames, 
                       final boolean colNames, 
                       final boolean ignoreEmptyRows, 
                       final boolean ignoreEmptyCols,
                       final boolean withDerivations,
                       final boolean withVerboseState)
    {
        super(IOFileFormat.JSON, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
        
        set(Options.Derivations, withDerivations);
        set(Options.VerboseState, withVerboseState);
    }
    
    private JSONOptions (final JSONOptions format)
    {
        super(format);
    }
    
    @Override
    protected JSONOptions clone(final BaseIOOptions<JSONOptions> model)
    {
        return new JSONOptions((JSONOptions)model);
    } 
    
    public boolean isRecalculate()
    {
        return (Boolean)get(Options.Recalculate);
    }
    
    public JSONOptions withRecalculate()
    {
        return withRecalculate(true);
    }
    
    public JSONOptions withRecalculate(boolean enabled)
    {
        final JSONOptions newOptions = clone(this);
        newOptions.set(Options.Recalculate, enabled);
        return newOptions;
    }
    
    /**
     * Returns {@code true} if all table metadata should be included with export.
     * @return {@code true} if all table metadata should be included with export.
     */
    public boolean isVerboseState()
    {
        return isTrue(Options.VerboseState);
    }
    
    /**
     * Export all table metadata
     * @return a new {@link JSONOptions} that is equal to this with the Verbose State property set to {@code true}
     */
    public JSONOptions withVerboseState()
    {
        return withVerboseState(true);
    }

    /**
     * Enable or disable export table metadata
     * @param enabled {@code true} to export all table metadata, {@code false} to omit metadata
     * @return a new {@link JSONOptions} with the Verbose State property set to {@code true} or {@code false}
     */
    public JSONOptions withVerboseState(final boolean enabled)
    {
    	JSONOptions newOptions = new JSONOptions(this);
        newOptions.set(Options.VerboseState, enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDerivations()
    {
        return (Boolean)get(Options.Derivations);
    }
    
    public JSONOptions withDerivations()
    {
        return withDerivations(true);
    }
    
    public JSONOptions withDerivations(boolean enabled)
    {
        final JSONOptions newOptions = clone(this);
        newOptions.set(Options.Derivations, enabled);
        return newOptions;
    }   

}
