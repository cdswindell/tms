package org.tms.api.utils;

import java.util.LinkedHashMap;

import org.tms.api.derivables.InvalidOperandsException;

public class StockTickerOp extends RestConsumerOp
{
	static final private String sf_URL = "http://marketdata.websol.barchart.com/getQuote.json";
	
	public StockTickerOp()
    {
        super("ticker", "results/lastPrice/05. price", double.class, sf_URL);
    }

    protected StockTickerOp(String label, String resultKey)
    {
        super(label, resultKey, double.class, sf_URL);
    }

    @Override
    protected void beforeOpenConnection()
    {
    	System.setProperty("jsse.enableSNIExtension", "true") ;
    }
    
    
    
    @Override
	protected Object postProcessResult(Object result) 
    {
    	if (result == null)
    		throw new InvalidOperandsException("Ticker symbol not found");
    	else
    		return super.postProcessResult(result);

	}

	@Override
    /**
     * Define the URL Params this Alphavantage Web API requires/supports
     * In this case, there are 2; a static param, "apikey", which
     * is set tomy specific API key, and a variable param, "symbol", which is supplied
     * when the operator is used. 
     * 
     */
    public LinkedHashMap<String, Object> getUrlParamsMap()
    {
        LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(2);
        
        urlParams.put("symbols", String.class);
        urlParams.put("apikey", "de5a529945661063cc8f9e5fdcf9a2a4");
        
        return urlParams;            
    }
 }
