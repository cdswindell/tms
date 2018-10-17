package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

import org.json.simple.JSONObject;

public class ToLatLongStrOp extends GeocodeBaseOp
{
	public ToLatLongStrOp()
    {
        this("toLatLong");
    }

	public ToLatLongStrOp(String token)
    {
        super(token, "results/geometry/location", JSONObject.class, sf_URL);
    }

	@Override
    protected Object postProcessResult(Object result) 
    {
		if (result != null && result instanceof JSONObject) {
			double lat = (double) ((JSONObject)result).get("lat");
			double lon = (double) ((JSONObject)result).get("lng");
			
			result = String.format("%s,%s", Double.toString(lat), Double.toString(lon));
		}
		
		return result;
    }
	
	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
        LinkedHashMap<String, Object> urlParams = super.getUrlParamsMap();        
        urlParams.put("address", String.class);
        
        return urlParams;            
	}
}
