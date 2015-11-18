package org.tms.api.utils;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;

import org.tms.api.derivables.InvalidOperandsException;

public class StockTickerOp extends RestConsumerOp
{
    public StockTickerOp()
    {
        super("ticker", "l_cur", double.class);
    }

    public StockTickerOp(String resultKey)
    {
        super("ticker", resultKey, double.class);
    }

    public StockTickerOp(String label, String resultKey)
    {
        super(label, resultKey, double.class);
    }

    @Override
    public String getUrl()
    {
        return "http://finance.google.com/finance/info";
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
        
        urlParams.put("client", "ig");
        urlParams.put("q", String.class);
        return urlParams;            
    }
 }
