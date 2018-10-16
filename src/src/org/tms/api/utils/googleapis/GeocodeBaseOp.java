package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.utils.RestConsumerOp;

abstract public class GeocodeBaseOp extends RestConsumerOp
{
	static final protected String sf_URL = "https://maps.googleapis.com/maps/api/geocode/json";
	
	protected GeocodeBaseOp(String label, String resultKey, Class<?> resultType, String url) 
	{
		super(label, resultKey, resultType, url);
	}
	
	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
		String apiKey = System.getenv("GOOGLE_API_KEY");
		if (apiKey == null)
			apiKey = System.getProperty("GOOGLE_API_KEY");
		if (apiKey == null)
			throw new IllegalTableStateException("Google API Key is required ");
		
        LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(4);       
        urlParams.put("key", apiKey);
        
        return urlParams;            
	}
}
