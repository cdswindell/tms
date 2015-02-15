package org.tms.teq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.tms.api.Operator;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.factories.TableContextFactory;

public class TokenMapper
{
    static final private Map<String, Token> sf_BuiltInTokenMap = new HashMap<String, Token>();    
    static {
        for (BuiltinOperator o : BuiltinOperator.values()) {
            if (o.isLabeled()) {
                Set<String> labels = o.getAliases();
                for (String label: labels) {
	                Token t = new Token(label, o.getPrimaryTokenType(), o);
	                sf_BuiltInTokenMap.put(label.toLowerCase(), t);  
                }
            }
            else {
                Set<TokenType> tts = o.getTokenTypes();
                if (tts != null) {
                    for (TokenType tt : tts) {
                        if (!tt.isLabeled()) continue;
                        Set<String> labels = tt.getLabels();
                        for (String label : labels) {                            
                            Token t = new Token(label, tt, o);
                            sf_BuiltInTokenMap.put(label.toLowerCase(), t);  
                        }
                    }
                }               
            }            
        }
    }
    
    static public TokenMapper fetchTokenMapper(Table t)
    {
        TableContext c = t != null ? t.getTableContext() : TableContextFactory.fetchDefaultTableContext();
        return fetchTokenMapper(c);
    }
    
    static public TokenMapper fetchTokenMapper(TableContext c)
    {
        if (c == null)
            c = TableContextFactory.fetchDefaultTableContext();
            
        TokenMapper tm = c.getTokenMapper();
        if (tm != null)
            return tm;
        
        // token mapper not created, create it now
        tm = new TokenMapper(c);
        
        return tm;
    }
       
    public static TokenMapper cloneTokenMapper(TokenMapper source, TableContext c)
    {
        if (c == null)
            c = TableContextFactory.fetchDefaultTableContext();
            
        // token mapper not created, create it now
        TokenMapper tm = new TokenMapper(c);
        
        // copy the user content
        for (Entry<String, Token> e: source.m_userTokenMap.entrySet()) {
            tm.m_userTokenMap.put(e.getKey(), e.getValue());
        }
        
        return tm;
    }
    
    private Map<String, Token> m_userTokenMap = new HashMap<String, Token>();
    private Map<OverloadKey, Token> m_userOverloadedOps = new HashMap<OverloadKey, Token>();
    private Table m_operTable;
    private TableContext m_context;
    
    private TokenMapper(Table operTable)
    {
        m_operTable = operTable;
    }
   
    private TokenMapper(TableContext context)
    {
        if (context == null)
            throw new IllegalTableStateException("Table Context Required");
            
        m_operTable = null;
        m_context = context;
    }
   
    public Token lookUpToken(char label)
    {
        return lookUpToken(Character.toString(label), m_operTable);
    }
    
    public Token lookUpToken(String label)
    {
        return lookUpToken(label, m_operTable);
    }
    
    public Token lookUpToken(char label, Table operTable)
    {
        return lookUpToken(Character.toString(label), operTable);
    }
    
    public Token lookUpToken(String label, Table operTable)
    {
        if (label == null)
            return null;
        
        Token t = m_userTokenMap.get(label.trim().toLowerCase());
        if (t != null)
            return t;
        
        t = sf_BuiltInTokenMap.get(label.trim().toLowerCase());
        
        if (t == null && operTable != null) {
            
        }
                
        return t;        
    }  
    
    public TableContext getTableContext()
    {
        return m_context;
    }
    
    public void registerOperator(Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        TokenType tt = oper.getTokenType();
        if (tt == null)
            throw new IllegalTableStateException("Operator TokenType required");
        
        String label = oper.getLabel();
        if (label == null || label.trim().length() == 0)
            throw new IllegalTableStateException("Labeled operator required");
        
        switch(tt) {
            case UnaryFunc:
            case BinaryFunc:
            case GenericFunc:
                break;
                
            default:
                throw new IllegalTableStateException("TokenType not supported");
        }
        
        Token t = new Token(tt, oper);
        m_userTokenMap.put(label.trim().toLowerCase(),  t);
    }
    
    public boolean deregisterOperator(Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        String label = oper.getLabel();
        if (label == null || label.trim().length() == 0)
            throw new IllegalTableStateException("Labeled operator required");
        
        Token t = m_userTokenMap.remove(label.trim().toLowerCase()); 
        return t != null;
    }
    
    public void deregisterAllOperators()
    {
    	m_userTokenMap.clear();
    }

    public void overloadOperator(String theOp, Operator oper)
    {
        if (theOp == null || theOp.trim().length() == 0)
            throw new IllegalTableStateException("+, -, *, or / required");
        
        theOp = theOp.trim();
        if (theOp != "+" && theOp != "-" && theOp != "*" && theOp != "/")
            throw new IllegalTableStateException("+, -, *, or / required");
        
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        if (oper.numArgs() != 2)
            throw new IllegalTableStateException("Operator must take exactly 2 arguments");
        
        TokenType tt = oper.getTokenType();
        if (tt == null)
            throw new IllegalTableStateException("Operator TokenType required");
                
        switch(tt) {
            case BinaryOp:
                break;
                
            default:
                throw new IllegalTableStateException("TokenType not supported");
        }
        
        Token t = new Token(theOp, tt, oper);
        OverloadKey key = new OverloadKey(theOp, oper.getArgTypes());
        
        m_userOverloadedOps.put(key, t);
    }
    
    public boolean unOverloadOperator(String theOp, Operator oper)
    {
    	if (theOp == null || theOp.trim().length() == 0)
    		throw new IllegalTableStateException("+, -, *, or / required");

    	theOp = theOp.trim();
    	if (theOp != "+" && theOp != "-" && theOp != "*" && theOp != "/")
    		throw new IllegalTableStateException("+, -, *, or / required");

    	if (oper == null)
    		throw new IllegalTableStateException("Operator required");

    	if (oper.numArgs() != 2)
    		throw new IllegalTableStateException("Operator must take exactly 2 arguments");


    	OverloadKey key = new OverloadKey(theOp, oper.getArgTypes());

    	Token t =  m_userOverloadedOps.remove(key);
    	return t != null;
    }
    
    public void unOverloadAllOperators()
    {
    	m_userOverloadedOps.clear();
    }

    public Operator fetchOverload(String theOp, Class<?>... paramTypes) 
    {
    	if (theOp == null || theOp.trim().length() == 0)
    		throw new IllegalTableStateException("+, -, *, or / required");

    	theOp = theOp.trim();
    	if (!BuiltinOperator.isValidBinaryOp(theOp))
    		throw new IllegalTableStateException("+, -, *, or / required");

    	if (paramTypes == null)
    		throw new IllegalTableStateException("Parameter types required");

    	if (paramTypes.length != 2)
    		throw new IllegalTableStateException("Must specify 2 parameter types");

    	OverloadKey key = new OverloadKey(theOp, paramTypes);
    	Token t = m_userOverloadedOps.get(key);
    	
    	if (t != null)
    		return t.getOperator();
    	else
    		return null;   	
    }
    
    static private class OverloadKey 
    {
    	private String m_op;
    	private List<Class<?>> m_argTypes;
    	
    	private OverloadKey(String theOp, Class<?>... paramTypes)
    	{
    		m_op = theOp;
    		m_argTypes = new ArrayList<Class<?>>();
    		
    		if (paramTypes != null) {
    			for (Class<?> pType : paramTypes) {
    				m_argTypes.add(pType);
    			}
    		}    		
    	}

		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result +  m_op.hashCode();
			
			if (m_argTypes != null) {
				for (Class<?> pType : m_argTypes) {
					result = prime * result + pType.hashCode();
				}
			}
			
			return result;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			OverloadKey other = (OverloadKey) obj;
			if (!m_op.equals(other.m_op))
				return false;
			
			int numArgs = m_argTypes.size();
			if (numArgs != other.m_argTypes.size())
				return false;
			
			for (int i = 0; i < numArgs; i++) {
				if (m_argTypes.get(i) != other.m_argTypes.get(i))
					return false;
			}
			
			return true;
		}
    }

}
