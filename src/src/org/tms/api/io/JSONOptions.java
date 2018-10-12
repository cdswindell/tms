package org.tms.api.io;

import org.tms.io.options.ArchivalIOOptions;

public class JSONOptions extends ArchivalIOOptions<JSONOptions> implements ArchivalIOOption<JSONOptions>
{
    public static final JSONOptions Default = new JSONOptions(true, true, true, true, true, false, false);

    /**
     * Constant with the most common JSON import and export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code true}</b></li>
     * <li>Ignore Empty Columns: <b>{@code true}</b></li>
     * <li>Import/Export Formulas/Derivations: <b>{@code true}</b></li>
     * <li>Import/Export Comments/Descriptions: <b>{@code false}</b></li>
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
                       final boolean withValidators,
                       final boolean withState)
    {
        super(IOFileFormat.JSON, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols, 
        		withDerivations, withDerivations, withValidators, withState);
        
        set(Options.Descriptions, true);
        set(Options.DisplayFormats, false);
        set(Options.Units, true);
        set(Options.UUIDs, true);
        set(Options.Tags, true);
    }
    
    private JSONOptions (final JSONOptions format)
    {
        super(format);
    }
    
    @Override
    protected JSONOptions clone(final ArchivalIOOptions<JSONOptions> model)
    {
        return new JSONOptions((JSONOptions)model);
    }    
}
