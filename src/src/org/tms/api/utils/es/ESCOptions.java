package org.tms.api.utils.es;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
    	Replicas,
    	Server,
    	Settings,
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
    	set(Options.Replicas, 1);
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
	public Map<String, Object> getMappings() 
    {
        return (Map<String, Object>)get(Options.Mappings);
    }
    
    public boolean isMappings()
    {
    	Map<String, Object> mappings = getMappings();
    	return mappings != null && !mappings.isEmpty();
    }

    public ESCOptions addMapping(final String field, final String type) 
    {
    	String val = (type != null ? type.trim() : null);
    	return _addMapping(field, (Object) val);
    }
    
    public ESCOptions addMapping(final String field, final JSONObject val) 
    {
    	return _addMapping(field, (Object) val);
    }
       
    @SuppressWarnings("unchecked")
	public ESCOptions addMapping(final String field, final Object... args) 
    {
    	Map<String, Object> config = new LinkedHashMap<String, Object>();
    	try {
	    	for (int i = 0; i < args.length; i++) {
	    		Object o = args[i];
	    		if (o == null)
		    		throw new IllegalArgumentException("Malformed mapping: null token");
	    		
	    		if (o instanceof String && ((String)o).indexOf('=') > 0) 
	    			parseValues(config, (String)o);
	    		else if (o instanceof String && i+1 < args.length) {
	    			String key = ((String)o).trim();
	    			if (key.length() > 0) {
	    				o = args[++i];
	    				if (o instanceof JSONObject) 
	    					config.put(key, (JSONObject)o);
	    				else if (o instanceof Map<?,?>) {
	    					Map<String, Object> m = (Map<String, Object>)o;
	    					config.put(key, new JSONObject(m));
	    				}
	    				else if (o instanceof String[]) {
	    					JSONObject so = new JSONObject();
	    					String [] sa = (String[])o;
	    					for (String s : sa) {
	    		    			parseValues(so, s);
	    					}
	    					
	    					config.put(key,  so);
	    				}
	    				else if (o instanceof String) 
	    					config.put(key,  parseValue((String)o));
	    				else
	    		    		throw new IllegalArgumentException("Malformed mapping: " + key);
	    			}
	    			else
    		    		throw new IllegalArgumentException("Malformed mapping: null token key");
	    		}
	    		else
	    			throw new IllegalArgumentException("Malformed mapping: " + o.getClass().getSimpleName());
	    	}	    	
    	} 
    	catch (ParseException e) {
    		throw new IllegalArgumentException(String.format("Malformed mapping: %s", ((ParseException)e).toString()));
		}
    	
    	return _addMapping(field, config);
    }
    
	private void parseValues(Map<String, Object> config, String val) 
    throws ParseException 
    {
		String [] tokens = val.trim().split("=");
		if (tokens.length != 2) throw new IllegalArgumentException("Malformed mapping: " + val);
		config.put(tokens[0], parseValue(tokens[1]));
	}

	private Object parseValue(String val) 
    throws ParseException 
    {
		if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val))
			return (boolean)Boolean.valueOf(val.toLowerCase());
		else if (val.startsWith("{") && val.endsWith("}"))
			return (JSONObject)(new JSONParser().parse(val));
		else
			return (String)val;
	}

	private ESCOptions _addMapping(final String field, final Object val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	
    	Map<String, Object> mappings = getMappings();
    	if (mappings == null) {
    		mappings = new LinkedHashMap<String, Object>();
    		newOptions.set(Options.Mappings, mappings);
    	}
    	
    	mappings.put(field.trim(), val);
        return newOptions;
    }
       
    /***/
    @SuppressWarnings("unchecked")
	public Map<String, Object> getSettings() 
    {
        return (Map<String, Object>)get(Options.Settings);
    }
    
    public boolean isSettings()
    {
    	Map<String, Object> settings = getSettings();
    	return settings != null && !settings.isEmpty();
    }

    public ESCOptions addSetting(final String field, final String type) 
    {
    	String val = (type != null ? type.trim() : null);
    	return addSetting(field, (Object) val);
    }
    
    public ESCOptions addSetting(final String field, final JSONObject val) 
    {
    	return addSetting(field, (Object) val);
    }
       
    protected ESCOptions addSetting(final String field, final Object val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
    	
    	Map<String, Object> settings = getSettings();
    	if (settings == null) {
    		settings = new LinkedHashMap<String, Object>();
    		newOptions.set(Options.Settings, settings);
    	}
    	
    	settings.put(field.trim(), val);
        return newOptions;
    }
       
    public ESCOptions withSettings(final Map<String, Object> val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
        newOptions.set(Options.Settings, val);
        return newOptions;
    }
       
    public ESCOptions withMappings(final Map<String, Object> val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
        newOptions.set(Options.Mappings, val);
        return newOptions;
    }
       
    public int getReplicas() 
    {
        return (Integer)get(Options.Replicas);
    }

    public ESCOptions withReplicas(final int val) 
    {
    	ESCOptions newOptions = new ESCOptions(this);
        newOptions.set(Options.Replicas, val);
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
		opts = opts.withCompletions(getCompletions());
		opts = opts.withCompletionField(getCompletionField());
		
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
