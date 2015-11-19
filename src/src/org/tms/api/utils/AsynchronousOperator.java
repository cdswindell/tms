package org.tms.api.utils;

import java.util.UUID;

import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

abstract public class AsynchronousOperator implements Operator, Runnable 
{
	abstract public Object performCalculation(Object [] m_args);
	
	private String m_label;
	private Class<?>[] m_argTypes;
	private Class<?> m_resultType;
	private Object [] m_args;
	private UUID m_transactionId;
	
	public AsynchronousOperator(String label, Class<?>[] argTypes, Class<?> resultType)
	{
		m_label = label;
		m_argTypes = argTypes;
		m_resultType = resultType;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	final public void run() 
	{
		try {
			Object result = performCalculation(m_args);
			
            Token t = Token.createOperandToken(result);
            Derivation.postResult(m_transactionId, t);
		}
		catch (Exception e) {
            Token errToken = Token.createErrorToken(e.getMessage());
            Derivation.postResult(m_transactionId, errToken);
		}
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
        // harvest args
        Object [] mArgs = new Object [numArgs()];
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = args[i].getValue();
            
            if (mArgs[i] == null && !isAllowNulls())
            	return Token.createNullToken();
        }
        
        // save away the args and the transaction id
        m_args = mArgs;
        m_transactionId = Derivation.getTransactionID();
        
		// return the pending token
        return Token.createPendingToken(this);
	}

	protected boolean isAllowNulls()
	{
		return false;
	}
}
