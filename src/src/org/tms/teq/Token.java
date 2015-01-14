package org.tms.teq;

import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.Operator;
import org.tms.api.Row;
import org.tms.api.TableCellsElement;


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
    

    public static Token createErrorToken(String msg)
    {
        return new Token(TokenType.EvaluationError, msg);
    }
    
    private String m_label;
    private TokenType m_tokenType;
    private Operator m_oper;
    private Object m_value;
    
    public Token(Object val)
    {
        this(TokenType.Operand, val);
    }

    public Token(TokenType tt)
    {
        setTokenType(tt);
    }

    public Token(TokenType tt, Object value)
    {
        if (value.equals(Double.NaN)) {
            setTokenType(TokenType.EvaluationError);
            setValue(ErrorCode.NaN);
        }
        else {
            setTokenType(tt);
            setValue(value);
        }
    }

    public Token(TokenType tt, Operator o)
    {
        setTokenType(tt);
        setOperator(o);
    }

    public Token(String label, TokenType tt, Operator o)
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
        if (getTokenType() == TokenType.EvaluationError && getErrorCode() == ErrorCode.NaN)
            return Double.NaN;
        else
            return m_value;
    }

    public Double getNumericValue()
    {
        if (m_value != null && m_value instanceof Number)
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

    public Column getColumnValue()
    {
        if (m_value != null && m_value instanceof Column)
            return (Column)m_value;
        else
            return null;
    }
    
    public Row getRowValue()
    {
        if (m_value != null && m_value instanceof Row)
            return (Row)m_value;
        else
            return null;
    }
    
    public Derivable getDerivableValue()
    {
        if (m_value != null && m_value instanceof Derivable)
            return (Derivable)m_value;
        else
            return null;
    }
    
    public TableCellsElement getReferenceValue()
    {
        if (m_value != null && m_value instanceof TableCellsElement)
            return (TableCellsElement)m_value;
        else
            return null;
    }
    
    void setValue(Object value)
    {
    	// we store numbers as doubles, so do some type conversion
        if (value instanceof Number && value.getClass() != Double.class) {
            if (value instanceof Integer)
                value = ((Integer)value).doubleValue();
            if (value instanceof Long)
                value = ((Long)value).doubleValue();
            else if (value instanceof Float)
                value = ((Float)value).doubleValue();
            else if (value instanceof Short)
                value = ((Short)value).doubleValue();
        }
        
        m_value = value;
    }

    public Class<? extends Object> getDataType()
    {
        if (isNull())
            return null;
        else
            return getValue().getClass();
    }
            
    public boolean isA(Class<?> targetClazz)
    {
        Class<?> dataType = getDataType();
        if (dataType == null)
            return true;
        
        if (targetClazz.isAssignableFrom(dataType))
            return true;
        else if (targetClazz.isPrimitive() && !dataType.isPrimitive()) {
            if (targetClazz == double.class && dataType == Double.class ||
                targetClazz == int.class && dataType == Integer.class ||
                targetClazz == boolean.class && dataType == Boolean.class)
                return true;
        }
        
        return false;
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
    
    public boolean isFunction()
    {
        if (this.getTokenType() != null) {
            switch (getTokenType()) {
                case UnaryFunc:
                case BinaryFunc:
                case GenericFunc:
                    return true;
                    
                default:
                    break;
            }
        }
        
        return false;
    }    

    public boolean isReference()
    {
        return getTokenType() != null && getTokenType().isReference() && getValue() != null && getValue() instanceof TableCellsElement;
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
}
