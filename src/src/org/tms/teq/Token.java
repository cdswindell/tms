package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Deque;

public class Token
{
    static public Deque<Token> createTokenStack()
    {
        Deque<Token> s = new ArrayDeque<Token>();
        return s;
    }
    
    private TokenType m_tokenType;
    private Operator m_oper;
    private Object m_value;
    
    public Token(TokenType tt)
    {
        setTokenType(tt);
    }

    public TokenType getTokenType()
    {
        return m_tokenType;
    }

    protected void setTokenType(TokenType tokenType)
    {
        m_tokenType = tokenType;
    }

    public Operator getOperator()
    {
        return m_oper;
    }

    void setOperator(Operator oper)
    {
        m_oper = oper;
    }

    public Object getValue()
    {
        return m_value;
    }

    void setValue(Object value)
    {
        m_value = value;
    }

}
