package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

import org.json.simple.JSONObject;

public class ToLatLongOp extends GeocodeBaseOp
{
	public ToLatLongOp()
    {
        this("toLatLong");
    }

	public ToLatLongOp(String token)
    {
        super(token, "results/geometry/location", JSONObject.class, sf_URL);
    }

	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
        LinkedHashMap<String, Object> urlParams = super.getUrlParamsMap();        
        urlParams.put("address", String.class);
        
        return urlParams;            
	}
}
