package org.tms.teq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tms.api.Table;
import org.tms.api.TableContext;

public class TokenMapper
{
    static final private Map<String, Token> sf_TokenMap = new HashMap<String, Token>();
    static {
        for (Operator o : Operator.values()) {
            if (o.isLabeled()) {
                Set<String> labels = o.getAliases();
                for (String label: labels) {
	                Token t = new Token(label, o.getPrimaryTokenType(), o);
	                sf_TokenMap.put(label.toLowerCase(), t);  
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
                            sf_TokenMap.put(label.toLowerCase(), t);  
                        }
                    }
                }               
            }            
        }
    }
    
    private Table m_operTable;
    
    public TokenMapper(Table operTable)
    {
        m_operTable = operTable;
    }
   
    public TokenMapper(TableContext context)
    {
        //TODO: Implement
        m_operTable = null;
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
        
        Token t = sf_TokenMap.get(label.trim().toLowerCase());
        
        if (t == null && operTable != null) {
            
        }
                
        return t;        
    }  
}
