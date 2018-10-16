package org.tms.api.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tms.api.derivables.InvalidOperandsException;
import org.tms.api.derivables.InvalidOperatorException;
import org.tms.api.derivables.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

abstract public class RestConsumerOp extends AbstractOperator
{
    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }
    
    abstract public LinkedHashMap<String, Object> getUrlParamsMap();
    
    private LinkedHashMap<String, Object> m_urlParamsMap;
    private StringBuffer m_constantUrlParamsStr;
    private String[] m_argKeys;
    private String m_resultKey;
    private String m_baseUrl;
    
    protected RestConsumerOp(String label, String resultKey, String url)
    {
        this(label, resultKey, Object.class, url);
    }
    
    protected RestConsumerOp(String label, String resultKey, Class<?> resultType, String url)
    {
    	super(label, null, resultType != null ? resultType : Object.class);
        m_resultKey = resultKey;
        m_baseUrl = url;

        initialize();
    }
    
    private void initialize() 
    {
        m_argKeys = null;
        m_constantUrlParamsStr = new StringBuffer();
        m_baseUrl = getUrl();
        m_urlParamsMap = getUrlParamsMap();       
        
        try {
            if (m_urlParamsMap != null) {
                List<Class<?>> argTypes = new ArrayList<Class<?>>(m_urlParamsMap.size());
                List<String> argKeys = new ArrayList<String>(m_urlParamsMap.size());
                
                for (Map.Entry<String, Object> e : m_urlParamsMap.entrySet()) {
                    if (e.getValue() instanceof Class) {
                        argKeys.add(e.getKey());
                        argTypes.add((Class<?>)e.getValue());
                    }
                    else {
                        String key = e.getKey();
                        Object value = e.getValue();
                        
                        /*
                         * There can be 2 kinds of URL parameters; those that are appended to the end of the 
                         * provided URL, with the familiar "key=value" notation, and those that are "in-lined"
                         * as part of the provided URL. We can handle both.
                         */
                        if (key.startsWith("{") && key.endsWith("}")) {
                            m_baseUrl = replace(m_baseUrl, key, value.toString());
                        }
                        else {
                            if (m_constantUrlParamsStr.length() > 0)
                                m_constantUrlParamsStr.append('&');
                            m_constantUrlParamsStr.append(e.getKey()).append('=');
                            m_constantUrlParamsStr.append(URLEncoder.encode(e.getValue().toString(), "UTF-8"));
                        }
                    }
                }
                
                if (!argTypes.isEmpty()) {
                    if (argTypes.size() != argKeys.size())
                        throw new InvalidOperatorException("Inconsistant URL Params Map");
                    
                    setArgTypes(argTypes.toArray(new Class[] {}));
                    m_argKeys = argKeys.toArray(new String [] {});
                }
            }
        }
        catch (UnsupportedEncodingException e)
        {
           throw new InvalidOperatorException(e);
        }
    }
    
    /**
     * Override to specify a complex URL that must be constructed
     * @return Override to specify a complex URL that must be constructed
     */
    public String getUrl()
    {
    	return m_baseUrl;
    }
    
    /**
     * Override to post process Json string in advance of parsing
     * @param data raw server response
     * @return postprocessed string
     */
    protected String postProcessInputStream(String data) 
    {
        return data;
    }
    
    protected boolean isAllowNulls()
    {
    	return false;
    }
    
    protected String preProcessURLParamKeyPrefix(String key) 
	{
		return "&";
	}

    protected String preProcessURLParamKey(String key) 
    {
		return key;
	}

    protected String preProcessURLParamKeyAssignmentOp(String key) 
    {
		return "=";
	}
    
    protected Object postProcessParamValue(String paramName, Object paramValue)
    {
        return paramValue;
    }
       
    /**
     * Override to provide a different timeout
     * @return Connection timeout, in milliseconds
     */
    protected int getConnectionTimeout()
    {
        return 20000;
    }
    
    /**
     * Override to perform other request methods
     * @return the Request Method
     */
    protected String getRequestMethod()
    {
        return "GET";
    }
    
    /*
     * Override in implementing class to set environment before attempting to connect to URL
     */
    protected void beforeOpenConnection()
    {
    }
    
    /**
     * Override in implementing class as needed
     * @param code HTTP Response Code
     */
    protected void processResponseCode(int code)
    {
        if (code != HttpURLConnection.HTTP_OK)
            throw new InvalidOperandsException("Web API Failed: " + code);
    }
    
    protected Object postProcessResult(Object result) 
    {
		return result;
	}

    /**
     * Override in implementing class as needed. Implementers should call
     * super.coerceResult(leaf) at the end of their implementation.
     * @param leaf REST result
     * @return processed result
     * @throws InvalidOperandsException if REST result cannot be coerced 
     */
    protected Object coerceResult(Object leaf)
    {
    	if (leaf == null || leaf.toString().trim().length() <= 0)
    		return null;
    	
        // all we can really do here is coerce strings to numbers...
    	Class<?> resultType = getResultType();
        if (Number.class.isAssignableFrom(resultType)) 
            return Double.parseDouble(leaf.toString());
        else if (resultType.isPrimitive())  {   
            if (resultType == boolean.class)
                return Boolean.parseBoolean(leaf.toString());
            else
                return Double.parseDouble(leaf.toString());
        }
        
        throw new InvalidOperandsException(String.format("Invalid result type, found: %s required: %s", 
                leaf.getClass().getSimpleName(), resultType.getSimpleName()));
    }
    
    @Override
	/**
	 * {@inheritDoc}
	 */
    final public Token evaluate(Token... args)
    {
        try {
            // harvest args
            Object [] mArgs = unpack(args);
            
            // get a copy of the base URL, it may also contain substitution params
            String baseUrl = m_baseUrl;
            
            // build final URL params string
            StringBuffer urlParams = new StringBuffer(m_constantUrlParamsStr);
            
            boolean needPrefix = urlParams.length() > 0;
            for (int i = 0; i < numArgs(); i++) {
                Object mArg = postProcessParamValue(m_argKeys[i], mArgs[i]);
                if (m_argKeys[i].startsWith("{") && m_argKeys[i].endsWith("}")) {
                    baseUrl = replace(baseUrl, m_argKeys[i], mArg == null ? "" : mArgs[i].toString());
                }
                else {
                    if (needPrefix) {
                        String argPrefix = preProcessURLParamKeyPrefix(m_argKeys[i]); // almost always "&"
                        urlParams.append(argPrefix);
                    }
                    
                    String argName = preProcessURLParamKey(m_argKeys[i]);
                    String argAssignOp = preProcessURLParamKeyAssignmentOp(m_argKeys[i]); // almost always "="
                    urlParams.append(argName).append(argAssignOp); // add parameter pair
                    if (mArg != null)
                        urlParams.append(URLEncoder.encode(mArg.toString(), "UTF-8"));
                    needPrefix = true;
                }
            }
            
            // create a RestEvaluator; it is a Runnable
            RestEvaluator re = new RestEvaluator(Token.getTransactionID(), baseUrl, urlParams);
            
            return Token.createPendingToken(re);
        }
        catch (Exception e)
        {
            return Token.createErrorToken(e.getMessage());
        }
    }

	private String replace(String source, String pattern, String replacement)
    {
        int idx = source.indexOf(pattern);
        if (idx > -1) {
            String result = source.substring(0, idx) + 
                            replacement + 
                            source.substring(idx + pattern.length());            
            return result;
        }
        else
            return source;        
    }
    
    protected class RestEvaluator implements Runnable
    {
        private UUID m_transId;
        private String m_urlString;
        
        public RestEvaluator(UUID transId, String baseUrl, StringBuffer urlParams)
        {
            m_transId = transId;
            m_urlString = baseUrl;
            if (urlParams != null) {
            	int qmIdx = m_urlString.indexOf('?');
                if (qmIdx == -1)
                    m_urlString += "?";
                else {
                	// check if we need to add an ampersand
                	if (qmIdx != m_urlString.length()) {
                        if (m_urlString.indexOf('&') == -1)
                            m_urlString += "&";
                	}
                }
                
                m_urlString += urlParams.toString();
            }
        }
        
        @Override
        public void run()
        {
            try {
            	beforeOpenConnection();
                URL myUrl = new URL(m_urlString);
                
                URLConnection urlCon = myUrl.openConnection();
                if (urlCon instanceof HttpURLConnection) {
                    HttpURLConnection httpCon = (HttpURLConnection)urlCon;
                    httpCon.setConnectTimeout(getConnectionTimeout());
                    httpCon.setRequestMethod(getRequestMethod());
                                       
                    int code = httpCon.getResponseCode();
                    processResponseCode(code);
                }
                
                InputStream is = urlCon.getInputStream();
                InputStreamReader isR = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isR);
                
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while( (line = reader.readLine()) != null ) {
                    buffer.append(line);
                }
                
                reader.close();
                
                // allow result to be postprocessed, as some
                // streams have to be adjusted
                String adjustedStr = postProcessInputStream(buffer.toString());
                
                // is the response XML?
                Object result = null;
                if (adjustedStr.toLowerCase().startsWith("<?xml ")) {
                	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                	DocumentBuilder db = dbf.newDocumentBuilder(); 
                	
                	Document doc = db.parse(new InputSource(new ByteArrayInputStream(adjustedStr.getBytes("utf-8"))));
                	NodeList nl = doc.getElementsByTagName(m_resultKey);
                	
                	if (nl != null && nl.getLength() > 0) {
                		Node n = nl.item(0).getFirstChild();
                		if (n != null && n.getNodeType() == Node.TEXT_NODE) {
                    		String nValue = n.getNodeValue();
                    		result = coerceResult(nValue);
                		}
                	}
                }
                else {
                	//assume JSON
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject)parser.parse(adjustedStr);
                    
                    result = parseJsonResponse(json);                   
                }
                
                // postprocess result
                result = postProcessResult(result);
                
                Token t = Token.createOperandToken(result);
                Token.postResult(m_transId, t);
            }
            catch (Exception e)
            {
                Token errToken = Token.createErrorToken(e.getMessage());
                Token.postResult(m_transId, errToken);
            }
        }

		protected Object parseJsonResponse(JSONObject json)
        {
            if (m_resultKey == null || (m_resultKey.trim()).length() <= 0) {
                if (getResultType().isAssignableFrom(json.getClass()))
                    return json;
                
                return coerceResult(json);           
            }
            
            String [] tokens = m_resultKey.split("/");
            
            JSONObject tree = json;
            Object leaf = null;
            int tokensProcessed = 0;
            for (String token : tokens) {
                leaf = tree.get(token);
                tokensProcessed++;
                
                // special case JSONArray; should probably use protected method
                if (leaf != null && leaf instanceof JSONArray) {
                	JSONArray ja = (JSONArray)leaf;
                	if (ja.isEmpty())
                		leaf = null;
                	else
                		leaf = ja.get(0);
                }
                
                if (leaf instanceof JSONObject)
                    tree = (JSONObject)leaf;
                else
                    break;
            }
            
            if (leaf == null && tokensProcessed < tokens.length)  {
            	postProcessResult(leaf);
                throw new InvalidOperandsException("Result not found: " + m_resultKey);
            }
            
            if (leaf == null)
                return null;
            
            if (getResultType().isAssignableFrom(leaf.getClass()))
                return leaf;
            
            // allow implementer to coerce result
            // we provide some defaults            
            return coerceResult(leaf);           
        }
    }
}
