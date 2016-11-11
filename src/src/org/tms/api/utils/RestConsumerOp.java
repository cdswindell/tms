package org.tms.api.utils;

import java.io.BufferedReader;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.InvalidOperandsException;
import org.tms.api.derivables.InvalidOperatorException;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

abstract public class RestConsumerOp extends AbstractOperator
{
    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }
    
    abstract public String getUrl();
    abstract public LinkedHashMap<String, Object> getUrlParamsMap();
    
    private String m_baseUrl;
    private LinkedHashMap<String, Object> m_urlParamsMap;
    private StringBuffer m_constantUrlParamsStr;
    private String[] m_argKeys;
    private String m_resultKey;
    
    protected RestConsumerOp(String label, String resultKey)
    {
        this(label, resultKey, Object.class);
    }
    
    protected RestConsumerOp(String label, String resultKey, Class<?> resultType)
    {
    	super(label, null, resultType != null ? resultType : Object.class);
        m_resultKey = resultKey;

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
        return 10000;
    }
    
    /**
     * Override to perform other request methods
     * @return the Request Method
     */
    protected String getRequestMethod()
    {
        return "GET";
    }
    
    /**
     * Override in implementing class as needed
     * @param code HTTP Response Code
     */
    protected void processResponseCode(int code)
    {
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
    final public String getLabel()
    {
        return super.getLabel();
    }
    
    @Override
	/**
	 * {@inheritDoc}
	 */
    final public TokenType getTokenType()
    {
        return TokenType.numArgsToTokenType(getArgTypes() != null ? getArgTypes().length : 0);
    }

    @Override
	/**
	 * {@inheritDoc}
	 */
    final public Class<?> getResultType()
    {
        return super.getResultType();
    }
    
    @Override
	/**
	 * {@inheritDoc}
	 */
    final public Token evaluate(Token... args)
    {
        try {
            // harvest args
            Object [] mArgs = new Object [numArgs()];
            for (int i = 0; i < numArgs(); i++) {
                mArgs[i] = args[i].getValue();
                
                if (mArgs[i] == null && !isAllowNulls())
                	return Token.createNullToken();
            }
            
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
                    if (needPrefix)
                        urlParams.append('&');
                    urlParams.append(m_argKeys[i]).append('=');
                    if (mArg != null)
                        urlParams.append(URLEncoder.encode(mArg.toString(), "UTF-8"));
                    needPrefix = true;
                }
            }
            
            // create a RestEvaluator; it is a Runnable
            RestEvaluator re = new RestEvaluator(Derivation.getTransactionID(), baseUrl, urlParams);
            
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
                if (m_urlString.indexOf('?') == -1)
                    m_urlString += "?";
                m_urlString += urlParams.toString();
            }
        }
        
        @Override
        public void run()
        {
            try {
                URL myUrl = new URL(m_urlString);
                
                URLConnection urlCon = myUrl.openConnection();
                urlCon.setConnectTimeout(getConnectionTimeout());
                if (urlCon instanceof HttpURLConnection) {
                    HttpURLConnection httpCon = (HttpURLConnection)urlCon;
                    //httpCon.setRequestMethod(getRequestMethod());
                    
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
                // json streams have to be adjusted
                String jsonStr = postProcessInputStream(buffer.toString());

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject)parser.parse(jsonStr);
                
                Token t = Token.createOperandToken(parseJsonResponse(json));
                Derivation.postResult(m_transId, t);
            }
            catch (Exception e)
            {
                Token errToken = Token.createErrorToken(e.getMessage());
                Derivation.postResult(m_transId, errToken);
            }
        }

        private Object parseJsonResponse(JSONObject json)
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
                if (leaf instanceof JSONObject)
                    tree = (JSONObject)leaf;
                else
                    break;
            }
            
            if (leaf == null && tokensProcessed < tokens.length)
                throw new InvalidOperandsException("Result not found: " + tokens[tokensProcessed]);
            
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
