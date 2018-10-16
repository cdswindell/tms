package org.tms.api.utils.googleapis;

import java.util.LinkedHashMap;

public class FromLatLongOp extends GeocodeBaseOp
{
	static final protected String sf_LatLngURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng={lat},{long}";
	public FromLatLongOp()
    {
        this("fromLatLong");
    }

	public FromLatLongOp(String token)
    {
        super(token, "results/formatted_address", String.class, sf_LatLngURL);
    }

	@Override
	public LinkedHashMap<String, Object> getUrlParamsMap() 
	{
		LinkedHashMap<String, Object> urlParams = super.getUrlParamsMap();        
        urlParams.put("{lat}", double.class);
        urlParams.put("{long}", double.class);
        
        return urlParams;            
	}
}
