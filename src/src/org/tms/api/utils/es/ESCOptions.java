package org.tms.api.utils.es;

import java.util.LinkedHashMap;
import java.util.Map;

import org.tms.api.io.ESIOOption;
import org.tms.api.io.ESOptions;
import org.tms.api.io.IOOption;
import org.tms.io.options.ESIOOptions;
import org.tms.io.options.OptionEnum;

public class ESCOptions extends ESIOOptions<ESCOptions> implements ESIOOption<ESCOptions>
{
    public static final ESCOptions Default = new ESCOptions();

    private enum Options implements OptionEnum 
    {
    	CatchAllField,
    	isRecreateIndex,
    	Mappings,
    	Port,
    	Server,
    	Shards,
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
    private ESCOptions()
    {
        super();
    	
    	set(Options.isRecreateIndex, true);
    	set(Options.Port, 9200);
    	set(Options.Server, "localhost");
    	set(Options.Shards, 5);
    }
    
    private ESCOptions (final ESCOptions format)
    {
        super(format);
    }
    
    protected ESCOptions clone(final ESIOOptions<ESCOptions> model)
    {
        return new ESCOptions((ESCOptions)model);
    }  
    
    public boolean isRecreateIndex() 
    {
        return (boolean)get(Options.isRecreateIndex);
    }

    public ESCOptions withRecreateIndex()
    {
    	return withRecreateIndex(true);
    }
    
    public ESCOptions withRecreateIndex(final boolean opt) 
    {
    	final ESCOptions newOptions = clone(this);
        newOptions.set(Options.isRecreateIndex, opt);
        return newOptions;
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, String> getMappings() 
    {
        return (Map<String, String>)get(Options.Mappings);
    }
    
    public boolean isMappings()
    {
    	Map<String, String> mappings = getMappings();
    	return mappings != null && !mappings.isEmpty();
    }

    public ESCOptions addMapping(final String field, final String type) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	
    	Map<String, String> mappings = getMappings();
    	if (mappings == null) {
    		mappings = new LinkedHashMap<String, String>();
    		newOptions.set(Options.Mappings, mappings);
    	}
    	
    	mappings.put(field.trim(), type.trim());
        return newOptions;
    }
       
    public ESCOptions withMappings(final Map<String, String> val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
        newOptions.set(Options.Mappings, val);
        return newOptions;
    }
       
    public int getShards() 
    {
        return (Integer)get(Options.Shards);
    }

    public ESCOptions withShards(final int val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
        newOptions.set(Options.Shards, val);
        return newOptions;
    }
       
    public int getPort() 
    {
        return (Integer)get(Options.Port);
    }

    public ESCOptions withPort(final int port) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	newOptions.set(Options.Port, port);
    	return newOptions;
    }

    public String getCatchAllField() 
    {
    	return (String)get(Options.CatchAllField);
    }

    public boolean isCatchAllField() 
    {
    	return null != getCatchAllField();
    }

    public ESCOptions withCatchAllField(final String val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	newOptions.set(Options.CatchAllField, val);
    	return newOptions;
    }

    public String getServer() 
    {
    	return (String)get(Options.Server);
    }

    public ESCOptions withServer(final String server) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	newOptions.set(Options.Server, server);
    	return newOptions;
    }

    public ESOptions asESOptions() 
	{
		ESOptions opts = ESOptions.Default;
		
		opts = opts.withIndex(getIndex());
		opts = opts.withType(getType());
		opts = opts.withLowerCaseFieldNames(isLowerCaseFieldNames());
		opts = opts.withIgnoreEmptyCells(isIgnoreEmptyCells());
		
		if (isExceptionOnEmptyIds())
			opts = opts.withExceptionOnEmptyIds(true);
		else if (isOmitRecordsWithEmptyIds())
			opts = opts.withOmitRecordsWithEmptyIds(true);
		
		if (isExceptionOnDuplicatdeIds())
			opts = opts.withExceptionOnDuplicatdeIds(true);
		else if (isOmitRecordsWithDuplicateIds())
			opts = opts.withOmitRecordsWithDuplicateIds(true);
		
		if (getIdColumn() != null)
			opts = opts.withIdColumn(this.getIdColumn());		
		else if (this.isIdUuid())
			opts = opts.withIdUuid(true);
		else if (this.isIdOrdinal())
			opts = opts.withIdOrdinal(true);
			
		return opts;
	}
}
