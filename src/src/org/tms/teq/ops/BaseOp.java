package org.tms.teq.ops;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

abstract public class BaseOp implements Operator
{
    private String m_label;
    private TokenType m_tokenType;
    private Class<?> m_resType;
    private Class<?> [] m_argTypes;
    
    public BaseOp(String label, TokenType tt, Class<?> [] argTypes, Class<?> resultType)
    {
        m_label = label;
        m_tokenType = tt;
        m_resType = resultType;
        m_argTypes = argTypes;
    }

    @Override
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public TokenType getTokenType()
    {
        return m_tokenType;
    }

    @Override
    public Class<?> getResultType()
    {
        return m_resType;
    }
    
    @Override
    public Class<?>[] getArgTypes()
    {
        return m_argTypes;
    }
    

	protected Object[] unpack(Token[] tokens) 
	{
        Object [] mArgs = new Object [numArgs()];
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = unpackArg(tokens[i].getValue(), m_argTypes[i]);
        }
		
		return mArgs;
	}

	private Object unpackArg(Object value, Class<?> requiredType) 
	{
		if (value != null && requiredType.isPrimitive()) 
			value = convertToPrimitive(value, requiredType);
		
		return value;
	}

	private Object convertToPrimitive(Object value, Class<?> requiredType) 
	{
		if (value instanceof Number) {
			Number n = (Number)value;
			if (requiredType == int.class) 
				return n.intValue();
			else if (requiredType == long.class) 
				return n.longValue();
			else if (requiredType == double.class) 
				return n.doubleValue();
			else if (requiredType == float.class) 
				return n.floatValue();
			else if (requiredType == short.class) 
				return n.shortValue();
			else if (requiredType == byte.class) 
				return n.byteValue();
		}
		
		return value;
	}
}
