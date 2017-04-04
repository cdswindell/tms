package org.tms.api.utils;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.teq.NullsNotAllowedException;

abstract public class AbstractOperator implements Operator 
{
	private String m_label;
	private Class<?>[] m_argTypes;
	private Class<?> m_resultType;
	private String [] m_categories;
	private TokenType m_tokenType;
	
	public AbstractOperator(String label, Class<?>[] argTypes, Class<?> resultType)
	{
		this(label, argTypes, resultType, (String [])null);
	}
	
	public AbstractOperator(String label, Class<?>[] argTypes, Class<?> resultType, String... categories)
	{
		m_label = label;
		m_argTypes = argTypes;
		m_resultType = resultType;
		m_categories = categories;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String getLabel() 
	{
		return m_label;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public TokenType getTokenType() 
	{
		if (m_tokenType == null)
			m_tokenType = TokenType.numArgsToTokenType(numArgs());
		
		return m_tokenType;
	}

	protected void setTokenType(final TokenType tType)
	{
		m_tokenType = tType;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
    final public int numArgs()
    {
		// need to call method, as it could have been overridden
        Class<?> [] args = getArgTypes();
        
        return args != null ? args.length : 0;
    }
    
	@Override
	/**
	 * {@inheritDoc}
	 */
	final public Class<?>[] getArgTypes() 
	{
		return m_argTypes;
	}

	protected void setArgTypes(final Class<?>[] argTypes)
	{
		m_argTypes = argTypes;
	}
	
	@Override
    public int getPriority()
    {
        return Operator.DEFAULT_PRIORITY;
    }

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Class<?> getResultType() 
	{
		return m_resultType;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
    public boolean isVariableArgs()
    {
        return false;
    }
    
	@Override
	/**
	 * {@inheritDoc}
	 */
    public boolean isRightAssociative() 
    {
        return false;
    }

	@Override
	/**
	 * {@inheritDoc}
	 */
	public String[] getCategories()
	{
		return m_categories;
	}
	
	protected boolean isAllowNulls()
	{
		return false;
	}
	
	protected Object[] unpack(final Token[] tokens) throws NullsNotAllowedException
	{
        Object [] mArgs = new Object [numArgs()];
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = unpackArg(tokens[i].getValue(), getArgTypes()[i]);
            
            if (mArgs[i] == null && !isAllowNulls())
            	throw new NullsNotAllowedException();
        }
		
		return mArgs;
	}

	protected Object unpackArg(final Object value, final Class<?> requiredType)
	{
		Object convertedValue = value;
		if (value != null && requiredType.isPrimitive()) 
			convertedValue = convertToPrimitive(value, requiredType);
		
		return convertedValue;
	}

	protected Object convertToPrimitive(final Object value, final Class<?> requiredType) 
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
