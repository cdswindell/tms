package org.tms.api.utils;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;

import org.tms.api.derivables.InvalidOperandsException;

public class StockTickerOp extends RestConsumerOp
{
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
    public LinkedHashMap<String, Object> getUrlParamsMap()
    {
        LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(2);
        
        urlParams.put("client", "ig");
        urlParams.put("q", String.class);
        return urlParams;            
    }
 }
