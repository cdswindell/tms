package org.tms.api.derivables;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableElement;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.DerivationImpl;
import org.tms.teq.PendingState;


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

    public static Token createPendingToken(Runnable runnable)
    {
        return new Token(TokenType.Pending, runnable);
    }
    
    public static Token createPendingToken(PendingState ps)
    {
        return new Token(TokenType.Pending, ps);
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
        if (value == null && tt != TokenType.Pending) {
            setTokenType(TokenType.NullValue);
            setValue(null);
        }   
        else if (value != null && value.equals(Double.NaN)) {
            setTokenType(TokenType.EvaluationError);
            setValue(ErrorCode.NaN);
        }
        else if (value != null && value.equals(Double.MIN_VALUE)) {
            setTokenType(TokenType.NullValue);
            setOperator(BuiltinOperator.NULL_operator);
            setValue(null);
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

    public void setTokenType(TokenType tokenType)
    {
        m_tokenType = tokenType;
    }

    public Operator getOperator()
    {
        return m_oper;
    }

    public void setOperator(Operator oper)
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

    public Boolean getBooleanValue()
    {
        if (m_value != null && m_value instanceof Boolean)
            return (Boolean)m_value;
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
    
    public TableElement getTableElementValue()
    {
        if (m_value != null && m_value instanceof TableElement)
            return (TableElement)m_value;
        else
            return null;
    }
    
    public TableElement getReferenceValue()
    {
        if (m_value != null && m_value instanceof TableElement)
            return (TableElement)m_value;
        else
            return null;
    }
    
    public PendingState getPendingState()
    {
        if (m_value != null && m_value instanceof PendingState)
            return (PendingState)m_value;
        else
            return null;
    }
    
    public void setValue(Object value)
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
        return isA(targetClazz, true);
    }
    
    public boolean isA(Class<?> targetClazz, boolean isFuzzy)
    {
        Class<?> dataType = getDataType();
        if (dataType == null)
            return true;
        
        if (targetClazz.isAssignableFrom(dataType))
            return true;
        else if (targetClazz.isPrimitive() && !dataType.isPrimitive()) {
            if (targetClazz == double.class && dataType == Double.class ||
                targetClazz == float.class && (dataType == Float.class || (isFuzzy && dataType == Double.class)) ||
                targetClazz == int.class && (dataType == Integer.class || (isFuzzy && dataType == Double.class)) ||
                targetClazz == short.class && (dataType == Short.class || (isFuzzy && dataType == Double.class)) ||
                targetClazz == long.class && (dataType == Long.class || (isFuzzy && dataType == Double.class)) ||
                targetClazz == boolean.class && dataType == Boolean.class)
                return true;
        }        
        else if (!targetClazz.isPrimitive() && dataType.isPrimitive()) {
            if (targetClazz == Double.class && dataType == double.class ||
                (targetClazz == Float.class || (isFuzzy && targetClazz == Double.class)) && dataType == float.class ||
                (targetClazz == Integer.class || (isFuzzy && targetClazz == Double.class)) && dataType == int.class ||
                (targetClazz == Short.class || (isFuzzy && targetClazz == Double.class)) && dataType == short.class ||
                (targetClazz == Long.class || (isFuzzy && targetClazz == Double.class)) && dataType == long.class ||
                targetClazz == Boolean.class && dataType == boolean.class)
                return true;
        }
        
        return false;
    }

    public boolean isA(Token y)
    {
        if (!isNull() && y != null && !y.isNull())
            return isA(y.getDataType());
        else
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
        return getTokenType() != null && (getTokenType().isOperand() || getTokenType().isNull());
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
    

    public boolean isBoolean()
    {
        if (isOperand()) {
            Object val = getValue();
            if (val != null)
                return Boolean.class.isAssignableFrom(val.getClass());
        }
        
        return false;
    }

    public boolean isPending()
    {
        if (this.getTokenType() != null) 
            return this.getTokenType().isPending();
        
        return false;
    }
    
    public boolean isFunction()
    {
        if (this.getTokenType() != null) 
            return this.getTokenType().isFunction();
        
        return false;
    }    

    public boolean isReference()
    {
        return getTokenType() != null && getTokenType().isReference() && getValue() != null && getValue() instanceof TableElement;
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

    public void postResult(Object value)
    {
        DerivationImpl.postResult(value);        
    }
}
