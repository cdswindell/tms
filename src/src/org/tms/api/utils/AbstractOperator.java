package org.tms.teq;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

abstract public class AbstractOperator implements Operator 
{
	abstract public Object performCalculation(Object [] m_args) throws Exception;
	
	private String m_label;
	private Class<?>[] m_argTypes;
	private Class<?> m_resultType;
	private String [] m_categories;
	
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
	final public String getLabel() 
	{
		return m_label;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	final public TokenType getTokenType() 
	{
		return TokenType.numArgsToTokenType(numArgs());
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

	@Override
	/**
	 * {@inheritDoc}
	 */
	final public Class<?> getResultType() 
	{
		return m_resultType;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	final public String[] getCategories()
	{
		return m_categories;
	}
	
	protected boolean isAllowNulls()
	{
		return false;
	}
	
	protected Object[] unpack(Token[] tokens) throws NullsNotAllowedException
	{
        Object [] mArgs = new Object [numArgs()];
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = unpackArg(tokens[i].getValue(), getArgTypes()[i]);
            
            if (mArgs[i] == null && !isAllowNulls())
            	throw new NullsNotAllowedException();
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
