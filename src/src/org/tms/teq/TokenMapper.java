package org.tms.teq;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.tms.api.Operator;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableContextFactory;
import org.tms.api.exceptions.IllegalTableStateException;

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
    
    public void unRegisterOperator(Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        String label = oper.getLabel();
        if (label == null || label.trim().length() == 0)
            throw new IllegalTableStateException("Labeled operator required");
        
        m_userTokenMap.remove(label.trim().toLowerCase());
        
    }
}
