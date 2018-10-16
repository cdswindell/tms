package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

public class FromLatLongOp extends GeocodeBaseOp
{
	public FromLatLongOp()
    {
        this("fromLatLong");
    }

	public FromLatLongOp(String token)
    {
        super(token, "results/formatted_address", String.class, sf_URL);
    }

	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
		LinkedHashMap<String, Object> urlParams = super.getUrlParamsMap();        
        urlParams.put("lat", double.class);
        urlParams.put("long", double.class);
        
        return urlParams;            
	}
		
    protected String preProcessURLParamKeyPrefix(String key) 
	{
		switch (key.toLowerCase()) {
			case "long":
				return "";
				
			default:
				return super.preProcessURLParamKeyPrefix(key);
		}		
	}
    	
	
	@Override
    protected String preProcessURLParamKey(String key) 
    {
		switch (key.toLowerCase()) {
			case "lat":
				return "latlng";
				
			case "long":
				return "";
				
			default:
				return super.preProcessURLParamKey(key);
		}
	}

	@Override
    protected String preProcessURLParamKeyAssignmentOp(String key) 
    {
		switch (key.toLowerCase()) {
			case "long":
				return ",";
				
			default:
				return super.preProcessURLParamKeyAssignmentOp(key);
		}		
    }
}
