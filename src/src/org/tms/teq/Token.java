package org.tms.teq;


public class Token implements Labeled
{
    static public EquationStack createTokenStack()
    {
        EquationStack s = new EquationStack();
        return s;
    }
    
    private String m_label;
    private TokenType m_tokenType;
    private Operator m_oper;
    private Object m_value;
    
    public Token(TokenType tt)
    {
        setTokenType(tt);
    }

    public Token(TokenType tt, Operator o)
    {
        setTokenType(tt);
        setOperator(o);
    }

    Token(String label, TokenType tt, Operator o)
    {
        m_label = label;
        setTokenType(tt);
        setOperator(o);
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

    public boolean isLeading()
    {
        if (getTokenType() != null)
            return getTokenType().isLeading();
        else
            return false;
    }

    public int getPriority()
    {
        return m_oper != null ? m_oper.getPriority() : 0;
    }
    
    public String getLabel()
    {
        return m_label;
    }
    
    public int getLabelLength()
    {
        return m_label != null ? m_label.length() : 0;
    }
    
    public boolean isLabeled()
    { 
        return m_label != null;
    }
    
    public String toString()
    {
        if (m_value != null)
            return m_value.toString();
        else if (isLabeled())
            return getLabel();
        else if (getOperator() != null)
            return getOperator().toString();
        else
            return "<null>";
    }
}
