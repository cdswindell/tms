package org.tms.teq;

import org.tms.api.Operator;


public class Token implements Labeled
{
	public static Token createNullToken()
	{
		Token t = new Token(TokenType.NullValue, BuiltinOperator.NULL_operator);
		return t;
	}
	
    public static Token createErrorToken(ErrorCode eCode)
    {
        return new Token(TokenType.EvaluationError, eCode);
    }
    
    private String m_label;
    private TokenType m_tokenType;
    private Operator m_oper;
    private Object m_value;
    
    public Token(Object val)
    {
        setTokenType(TokenType.Operand);
        setValue(val);
    }

    public Token(TokenType tt)
    {
        setTokenType(tt);
    }

    public Token(TokenType tt, Object value)
    {
        setTokenType(tt);
        setValue(value);
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

    private Token(Token token)
    {
		setTokenType(token.getTokenType());
		setOperator(token.getOperator());
		setValue(token.getValue());
		m_label = token.getLabel() != null ? new String(token.getLabel()) : null;
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

    public Double getNumericValue()
    {
        if (m_value != null && m_value instanceof Double)
            return (Double)m_value;
        else
            return null;
    }

    public String getStringValue()
    {
        if (m_value != null && m_value instanceof String)
            return (String)m_value;
        else
            return null;
    }

    void setValue(Object value)
    {
        m_value = value;
    }

    public Class<? extends Object> getDataType()
    {
        if (isNull())
            return null;
        else
            return getValue().getClass();
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
        else if (getTokenType() != null && getTokenType().getLabel() != null) 
                return getTokenType().getLabel();
        else if (getOperator() != null && getOperator().getLabel() != null) 
            return getOperator().getLabel();
        else if (getOperator() != BuiltinOperator.NOP) 
            return getOperator().toString();
        else if (getTokenType() != null) 
            return getTokenType().toString();
        else
            return "<null>";
    }

    public boolean isLeftParen()
    {
        return getTokenType() != null && getTokenType().isLeftParen();
    }

    public boolean isRightParen()
    {
        return getTokenType() != null && getTokenType().isRightParen();
    }

	public boolean isOperand() 
	{
        return getTokenType() != null && getTokenType().isOperand();
	}

    public boolean isNumeric()
    {
        if (isOperand()) {
            Object val = getValue();
            if (val != null)
                return Number.class.isAssignableFrom(val.getClass());
        }
        
        return false;
    }
    
    public boolean isString()
    {
        if (isOperand()) {
            Object val = getValue();
            if (val != null)
                return String.class.isAssignableFrom(val.getClass());
        }
        
        return false;
    }
    
    public boolean isNull() 
    {
        return (getTokenType() == TokenType.NullValue) || (getTokenType() == TokenType.Operand && getValue() == null);
    }
    
    public boolean isError() 
    {
        return (getTokenType() == TokenType.EvaluationError);
    }
    
    public ErrorCode getErrorCode()
    {
        if (isError()) {
            if (m_value == null)
                return ErrorCode.Unspecified;
            else if (m_value instanceof ErrorCode)
                return (ErrorCode)m_value;
            else
                return ErrorCode.SeeErrorMessage;
        }
        else
            return ErrorCode.NoError;
    }

	@Override
	protected Token clone() 
	{
		return new Token(this);
	}
}
