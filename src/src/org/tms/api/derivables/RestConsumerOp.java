package org.tms.api.derivables;

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

abstract public class RestConsumerOp implements Operator
{
    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }
    
    abstract public String getUrl();
    abstract public LinkedHashMap<String, Object> getUrlParamsMap();
    
    private String m_label;
    private String m_baseUrl;
    private LinkedHashMap<String, Object> m_urlParamsMap;
    private StringBuffer m_constantUrlParamsStr;
    private Class<?>[] m_argTypes;
    private String[] m_argKeys;
    private String m_resultKey;
    private Class<?> m_resultType;
    
    protected RestConsumerOp(String label, String resultKey)
    {
        this(label, resultKey, Object.class);
    }
    
    protected RestConsumerOp(String label, String resultKey, Class<?> resultType)
    {
        m_label = label;
        m_resultKey = resultKey;
        m_resultType = resultType != null ? resultType : Object.class;

        initialize();
    }
    
    private void initialize() 
    {
        m_argTypes = null;
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
                        if (m_constantUrlParamsStr.length() > 0)
                            m_constantUrlParamsStr.append('&');
                        m_constantUrlParamsStr.append(e.getKey()).append('=');
                        m_constantUrlParamsStr.append(URLEncoder.encode(e.getValue().toString(), "UTF-8"));
                    }
                }
                
                if (!argTypes.isEmpty()) {
                    if (argTypes.size() != argKeys.size())
                        throw new InvalidOperatorException("Inconsistant URL Params Map");
                    m_argTypes = argTypes.toArray(new Class[] {});
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
    
    @Override
    public String getLabel()
    {
        return m_label;
    }
    
    @Override
    public TokenType getTokenType()
    {
        return TokenType.GenericFunc;
    }

    @Override
    public Class<?> getResultType()
    {
        return m_resultType;
    }
    
    @Override
    public Class<?>[] getArgTypes()
    {
        return m_argTypes;
    }

    @Override
    public Token evaluate(Token... args)
    {
        try {
            // harvest args
            Object [] mArgs = new Object [numArgs()];
            for (int i = 0; i < numArgs(); i++) {
                mArgs[i] = args[i].getValue();
            }
            
            // build final URL params string
            StringBuffer urlParams = new StringBuffer(m_constantUrlParamsStr);
            
            boolean needPrefix = urlParams.length() > 0;
            for (int i = 0; i < numArgs(); i++) {
                if (needPrefix)
                    urlParams.append('&');
                urlParams.append(m_argKeys[i]).append('=');
                if (mArgs[i] != null)
                    urlParams.append(URLEncoder.encode(mArgs[i].toString(), "UTF-8"));           
            }
            
            RestEvaluator re = new RestEvaluator(Derivation.getTransactionID(), m_baseUrl, urlParams);
            
            return Token.createPendingToken(re);
        }
        catch (Exception e)
        {
            return Token.createErrorToken(e.getMessage());
        }
    }
    
    protected class RestEvaluator implements Runnable
    {
        private UUID m_transId;
        private String m_urlString;
        
        public RestEvaluator(UUID transId, String baseUrl, StringBuffer urlParams)
        {
            m_transId = transId;
            m_urlString = baseUrl + (urlParams != null ? "?" + urlParams.toString() : "");
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
            if (m_resultType.isAssignableFrom(leaf.getClass()))
                return leaf;
            
            // all we can really do here is coerce strings to numbers...
            if (Number.class.isAssignableFrom(m_resultType)) 
                return Double.parseDouble(leaf.toString());
            else if (m_resultType.isPrimitive())                
                return Double.parseDouble(leaf.toString());
            
            throw new InvalidOperandsException(String.format("Invalid result type, found: %s required: %s", 
                    leaf.getClass().getSimpleName(), m_resultType.getSimpleName()));
        }
    }
}
