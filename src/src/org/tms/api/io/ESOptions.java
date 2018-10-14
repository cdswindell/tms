package org.tms.api.io;

import org.tms.api.Column;
import org.tms.io.options.BaseIOOptions;
import org.tms.io.options.OptionEnum;

public class ESOptions extends BaseIOOptions<ESOptions> implements IOOption<ESOptions>
{
    public static final ESOptions Default = new ESOptions();

    private enum Options implements OptionEnum 
    {
        Index,
        Type,
        Id;        
    }
    
    /**
     * Constant with the most common ElasticSearch export configuration options already set.
     * <p>
     * To include these default values when exporting to ElasticSearch, simply include {@code ESOptions.Default}
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
    private ESOptions()
    {
        super(IOFileFormat.ES, false, false, true, true);
    }
    
    private ESOptions (final ESOptions format)
    {
        super(format);
    }
    
    @Override
    protected ESOptions clone(final BaseIOOptions<ESOptions> model)
    {
        return new ESOptions((ESOptions)model);
    }    
    
    public String getIndex() 
    {
        return (String)get(Options.Index);
    }

    public ESOptions withIndex(final String index) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.Index, index);
        return newOptions;
    }
    
    public String getType() 
    {
        return (String)get(Options.Type);
    }

    public ESOptions withType(final String dType) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.Type, dType);
        return newOptions;
    }
    
    public Column getId() 
    {
        return (Column)get(Options.Id);
    }

    public ESOptions withId(final Column idCol) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.Id, idCol);
        return newOptions;
    }
}
