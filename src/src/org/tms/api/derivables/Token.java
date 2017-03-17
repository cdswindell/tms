package org.tms.api.derivables;

import java.math.BigDecimal;
import java.util.UUID;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableRowColumnElement;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.DerivationImpl;
import org.tms.teq.InfixExpressionParser;
import org.tms.teq.BaseAsyncState;

/**
 * The Token class is used by the Derivation engine to represent discrete
 * elements in a TableElement derivation. Although the class provides public
 * constructors, it is not intended to be consumed by TMS end users.
 */
public class Token implements Labeled
{
    public static void postResult(UUID transactionId, double rslt)
    {
        DerivationImpl.postResult(transactionId, rslt);
    }

    public static void postResult(UUID transactionId, Token rslt)
    {
        DerivationImpl.postResult(transactionId, rslt);
    }

    public static UUID getTransactionID()
    {
        return DerivationImpl.getTransactionID();
    }
    
    public static Token createNullToken()
    {
        Token t = new Token(TokenType.NullValue, BuiltinOperator.NULL_operator);
        return t;
    }
    
    public static Token createOperandToken(Object operand)
    {
        Token t = new Token(TokenType.Operand, operand);
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

	public static Token createErrorToken(Exception e) 
	{
        return new Token(TokenType.EvaluationError, e.getMessage());
	}
	
    public static Token createPendingToken(Runnable runnable)
    {
        return new Token(TokenType.Pending, runnable);
    }
    
    public static Token createAwaitingToken(String uuid)
    {
        return new Token(TokenType.Awaiting, uuid);
    }
    
    public static Token createPendingToken(BaseAsyncState ps)
    {
        return new Token(TokenType.Pending, ps);
    }
    
    private String m_label;
    private TokenType m_tokenType;
    private Operator m_oper;
    private Object m_value;
    
    /**
     * Create an Operand {@code Token}
     * @param val the operand value
     */
    public Token(Object val)
    {
        this(TokenType.Operand, val);
    }

    protected Token(Token t)
    {
        m_label = t.m_label;
        m_tokenType = t.m_tokenType;
        m_oper = t.m_oper;
        m_value = t.m_value;
    }
    
    /**
     * Create a {@code Token} with the specified {@link TokenType}.
     * @param tokenType the TokenType to create this Token with
     */
    public Token(TokenType tokenType)
    {
        setTokenType(tokenType);
    }

    public Token(String label, TokenType tokenType)
    {
        setTokenType(tokenType);
        setLabel(label);
    }

    /**
     * Create a new {@code Token} given a {@link TokenType} and an operand
     * @param tt the TokenType
     * @param value the Operand
     */
    public Token(TokenType tt, Object value)
    {
    	this(tt, value, (String) null);
    }
    
    public Token(TokenType tt, Object value, String label)
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
        else if (tt == TokenType.Awaiting) {
            setTokenType(tt);
            setValue(value);
            setLabel(label);
        }
        else {
            setTokenType(tt);
            setValue(value);
            setLabel(label);
        }
    }

    public Token(TokenType tt, Operator o)
    {
        setTokenType(tt);
        setOperator(o);
    }

    public Token(Operator o)
    {
        setTokenType(o.getTokenType());
        setOperator(o);
    }

    public Token(String label, TokenType tt, Operator o)
    {
        m_label = label;
        setTokenType(tt);
        setOperator(o);
    }


    public void from(Token rsltToken)
    {
        m_label = rsltToken.m_label;
        m_tokenType = rsltToken.m_tokenType;
        m_oper = rsltToken.m_oper;
        m_value = rsltToken.m_value;
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
        	if (m_value instanceof BigDecimal)
        		return ((BigDecimal)m_value).doubleValue();
        	else
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
        if (getValue() != null && getValue() instanceof Column)
            return (Column)getValue();
        else
            return null;
    }
    
    public Row getRowValue()
    {
        if (getValue() != null && getValue() instanceof Row)
            return (Row)getValue();
        else
            return null;
    }
    
    public TableElement getTableElementValue()
    {
        if (getValue() != null && getValue() instanceof TableElement)
            return (TableElement)getValue();
        else
            return null;
    }
    
    public TableElement getReferenceValue()
    {
        if (getValue() != null && getValue() instanceof TableElement)
            return (TableElement)getValue();
        else
            return null;
    }
    
    public BaseAsyncState getPendingState()
    {
        if (m_value != null && m_value instanceof BaseAsyncState)
            return (BaseAsyncState)m_value;
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

    @SuppressWarnings("unchecked")
	public Class<? extends Object> getDataType()
    {
        if (isNull())
            return null;
        else {
            if (getTokenType() == TokenType.OperandDataType)
            	return (Class<? extends Object>)getValue();
            else if (getTokenType() == TokenType.BuiltIn || isFunction())
            	return getOperator().getResultType();
            else if (getValue() != null)
            	return getValue().getClass();
            else
            	return null;
        }
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
        else if (targetClazz.isPrimitive() && dataType.isPrimitive()) {
            if (targetClazz == double.class && (dataType == float.class || dataType == long.class || dataType == int.class || dataType == short.class || dataType == byte.class || dataType == char.class)  ||
               (targetClazz == long.class && (dataType == int.class || dataType == short.class || dataType == byte.class || dataType == char.class)) ||
               (targetClazz == int.class && (dataType == short.class || dataType == byte.class || dataType == char.class)))
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
    
    void setLabel(String label)
    {
        m_label = label != null && (label = label.trim()).length() > 0 ? label : null;
    }
    
    public String toString()
    {
        if (getValue() != null)
            return getValue().toString();
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

    public boolean isOperator() 
    {
        return getTokenType() != null && getTokenType().isOperator();
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
    
    public boolean isEvaluatesToNumeric()
    {
        boolean result = isNumeric();
        if (!result) {
            if (getOperator() != null && getOperator().getResultType() != null)
                return Number.class.isAssignableFrom(getOperator().getResultType());
        }
        
        return result;
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
    
    public boolean isEvaluatesToString()
    {
        boolean result = isString();
        if (!result) {
            if (getOperator() != null && getOperator().getResultType() != null)
                return String.class.isAssignableFrom(getOperator().getResultType());
        }
        
        return result;
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

    public boolean isEvaluatesToBoolean()
    {
        boolean result = isBoolean();
        if (!result) {
            if (getOperator() != null && getOperator().getResultType() != null)
                return Boolean.class.isAssignableFrom(getOperator().getResultType());
        }
        
        return result;
    }
    
    public boolean isPending()
    {
        if (this.getTokenType() != null) 
            return this.getTokenType().isPending();
        
        return false;
    }    

	public boolean isAwaiting() 
	{
        if (this.getTokenType() != null) 
            return this.getTokenType().isAwaiting();
        
        return false;
	}
	
    public boolean isBasicOperator()
    {
        return getTokenType() != null ? getTokenType().isBasicOperator() : false;
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
    
    public boolean isEvaluatesToReference()
    {
        boolean result = isReference();
        if (!result) {
            if (getOperator() != null && getOperator().getResultType() != null)
                return TableElement.class.isAssignableFrom(getOperator().getResultType());
        }
        
        return result;
    }
    
    public boolean isBuiltIn()
    {
        return getTokenType() != null && getTokenType().isBuiltIn();
    }   

    public boolean hasReferenceArg()
    {
        Operator op = getOperator();
        if (op != null) {
            Class<?> [] argTypes = op.getArgTypes();
            if (argTypes != null) {
                for (Class<?> argType : argTypes) {
                    if (TableElement.class.isAssignableFrom(argType))
                        return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean isNull() 
    {
        return (getTokenType() == TokenType.NullValue) || (getTokenType() == TokenType.Operand && getValue() == null);
    }
    
    public boolean isExpression()
    {
        if (isOperand() && this.getTokenType() == TokenType.Expression) 
            return isString();
        
        return false;
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

    public String toExpressionValue()
    {
        return toExpressionValue(false, null);
    }
    
    public String toExpressionValue(Table primaryTable)
    {
        return toExpressionValue(false, primaryTable);
    }
    
    public String toExpressionValue(boolean preferUUIDs, Table primaryTable)
    {
        if (isNumeric())
            return getNumericValue().toString();
        else if (isBoolean())
            return getBooleanValue().toString();
        else if (isReference()) {
            StringBuffer sb = new StringBuffer();
            switch (getTokenType()) {
                case ColumnRef:
                    sb.append(createRef("col", (TableElement)getValue(), preferUUIDs, primaryTable));
                    break;
                    
                case RowRef:
                    sb.append(createRef("row", (TableElement)getValue(), preferUUIDs, primaryTable));
                    break;
                    
                case CellRef:
                    sb.append(createRef("cell", (TableElement)getValue(), preferUUIDs, primaryTable));
                    break;
                    
                case SubsetRef:
                    sb.append(createRef("subset", (TableElement)getValue(), preferUUIDs, primaryTable));
                    break;
                    
                default:
                    return null;
            }
            
            return sb.toString();
        }
        else if (isBuiltIn())
            return this.getOperator().getLabel();
        else if (isExpression())
            return getStringValue();
        else if (isString())
            return "\"" +getStringValue() + "\"";
        else if (this.getOperator() == BuiltinOperator.NegOper)
            return "-";
        else
            return this.toString();
    }

    private Object createRef(String elemType, TableElement ref, boolean preferUUIDs, Table primaryTable)
    {
        StringBuffer sb = new StringBuffer(elemType + " ");
        
        boolean requiresTrailingQuote = false;
        if (primaryTable != null && primaryTable != ref.getTable()) {
            requiresTrailingQuote = true;
            sb.append('"');
            sb.append(ref.getTable().getLabel());
            sb.append(InfixExpressionParser.sf_TABLE_REF);
        }
        
        String uuid = null;
        Table refTable = ref.getTable();
        boolean useUUID = preferUUIDs &&
        					refTable instanceof ExternalDependenceTableElement && !(ref instanceof ExternalDependenceTableElement);
        if (useUUID)
        	uuid = ref.getUuid();
        
        String label = ref.getLabel();
        if (label == null || label.trim().length() <= 0)
            label = null;
        
        switch (elemType) {
            case "subset":
            case "cell":
                if (!requiresTrailingQuote)
                    sb.append('"');
                if (useUUID)
                	sb.append(uuid);
                else
                    sb.append(label);
                requiresTrailingQuote = true;
                break;
                
            case "row":
            case "col":
            	if (useUUID) {
                    if (!requiresTrailingQuote)
                        sb.append('"');
                	sb.append(uuid);
                    requiresTrailingQuote = true;
            	}
            	else if (label == null)
                    sb.append(((TableRowColumnElement)ref).getIndex());
                else {
                    if (!requiresTrailingQuote) {
                        sb.append('"');
                        requiresTrailingQuote = true;
                    }
                    
                    sb.append(label);
                }
                break;
        }
        
        if (requiresTrailingQuote)
            sb.append('"');
        
        return sb.toString();
    }
}
