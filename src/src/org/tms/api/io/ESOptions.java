package org.tms.api.io;

import org.tms.io.options.ESIOOptions;

public class ESOptions extends ESIOOptions<ESOptions> 
{
    public static final ESOptions Default = new ESOptions();

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
    protected ESOptions()
    {
        super();
    }
    
    protected ESOptions(final ESOptions format)
    {
        super(format);
    }
    
    @Override
    protected ESOptions clone(final ESIOOptions<ESOptions> model)
    {
        return new ESOptions((ESOptions)model);
    }
}
