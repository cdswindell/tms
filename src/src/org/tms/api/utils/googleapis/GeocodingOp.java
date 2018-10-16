package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

import org.json.simple.JSONObject;

public class GeocodingOp extends GeocodeBaseOp
{
	public GeocodingOp()
    {
        this("toGeoResult");
    }

	public GeocodingOp(String token)
    {
        super(token, "results", JSONObject.class, sf_URL);
    }

	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
        LinkedHashMap<String, Object> urlParams = super.getUrlParamsMap();       
        urlParams.put("address", String.class);
        
        return urlParams;            
	}
}
