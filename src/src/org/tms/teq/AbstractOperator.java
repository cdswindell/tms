package org.tms.teq;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.TokenType;

abstract public class AbstractOperator implements Operator 
{
	abstract public Object performCalculation(Object [] m_args);
	
	private String m_label;
	private Class<?>[] m_argTypes;
	private Class<?> m_resultType;
	
	public AbstractOperator(String label, Class<?>[] argTypes, Class<?> resultType)
	{
		m_label = label;
		m_argTypes = argTypes;
		m_resultType = resultType;
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
		return TokenType.numArgsToTokenType(getArgTypes() != null ? getArgTypes().length : 0);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
    final public int numArgs()
    {
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

	protected boolean isAllowNulls()
	{
		return false;
	}
}
