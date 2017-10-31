package org.tms.api.utils;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;

import org.tms.api.derivables.InvalidOperandsException;

public class StockTickerOp extends RestConsumerOp
{
	static final private String sf_URL = "http://ws.cdyne.com/delayedstockquote/delayedstockquote.asmx/GetQuote";
	
	public StockTickerOp()
    {
        super("ticker", "LastTradeAmount", double.class, sf_URL);
    }

    protected StockTickerOp(String label, String resultKey)
    {
        super(label, resultKey, double.class, sf_URL);
    }

    @Override
    protected void processResponseCode(int code)
    {
        if (code == HttpURLConnection.HTTP_BAD_REQUEST || code == HttpURLConnection.HTTP_NOT_FOUND)
            throw new InvalidOperandsException("Ticker symbol not found");
    }
    
    @Override
    /**
     * Yahoo doesn't return "clean" json, so we need to use an
     * extension point on RestConsumerOp to postprocess the
     * returned text to strip away the non-json characters...
     * {@inheritDoc}
     */
    protected String postProcessInputStream(final String data)
    {
        if (data.startsWith("// [") && data.endsWith("]")) {
            String newData = data.substring(4);
            newData = newData.substring(0, newData.length() - 1);
            
            return newData;
        }
        else
            return data;
    }

    @Override
    /**
     * Define the URL Params this Yahoo Web API requires/supports
     * In this case, there are 2; a static param, "client", which
     * is set to "ig", and a variable param, "q", which is supplied
     * when the operator is used. 
     * 
     * By setting the map value for the "q" key to String.class,
     * we tell TMS that the operator tahes 1 argument, and that
     * its type is a String.
     */
    public LinkedHashMap<String, Object> getUrlParamsMap()
    {
        LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(2);
        
        urlParams.put("LicenseKey", "0");
        urlParams.put("StockSymbol", String.class);
        
        return urlParams;            
    }
 }
