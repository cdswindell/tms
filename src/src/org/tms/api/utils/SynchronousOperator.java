package org.tms.api.utils;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

abstract public class SynchronousOperator implements Operator 
{
	abstract public Object performCalculation(Object [] m_args);
	
	private String m_label;
	private Class<?>[] m_argTypes;
	private Class<?> m_resultType;
	
	public SynchronousOperator(String label, Class<?>[] argTypes, Class<?> resultType)
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

	@Override
	/**
	 * {@inheritDoc}
	 */
	final public Token evaluate(Token... args) 
	{
		try {
	        // harvest args
	        Object [] mArgs = new Object [numArgs()];
	        for (int i = 0; i < numArgs(); i++) {
	            mArgs[i] = args[i].getValue();
	            
	            if (mArgs[i] == null && !isAllowNulls())
	            	return Token.createNullToken();
	        }
	        
			// perform the calculation and return
			Object result = performCalculation(mArgs);
			
	        Token t = Token.createOperandToken(result);
	        return t;
		}
		catch (Exception e) {
            Token errToken = Token.createErrorToken(e.getMessage());
            return errToken;
		}
	}

	protected boolean isAllowNulls()
	{
		return false;
	}
}
