package org.tms.api.utils;

import java.util.UUID;

import org.tms.api.derivables.Token;
import org.tms.teq.AbstractOp;
import org.tms.teq.NullsNotAllowedException;

abstract public class AsynchronousOp extends AbstractOp
{
	public AsynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, argTypes, resultType);
	}

	public AsynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType, String... categories) 
	{
		super(label, argTypes, resultType, categories);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	final public Token evaluate(Token... args) 
	{
		try {
	        // harvest args
	        Object [] mArgs = unpack(args);
	        
	        // save away the args and the transaction id
	        CalculationRunner cr = new CalculationRunner(mArgs, Token.getTransactionID());
	        
			// return the pending token
	        return Token.createPendingToken(cr);
		}
		catch (NullsNotAllowedException e) {
        	return Token.createNullToken();
		}
		catch (Exception e) {
			return Token.createErrorToken(e);
		}
	}
	
	class CalculationRunner implements Runnable
	{
		private Object [] m_args;
		private UUID m_transactionId;
		
		CalculationRunner(Object [] args, UUID transactionId) 
		{
			m_args = args;
			m_transactionId = transactionId;
		}
			
		@Override
		final public void run() 
		{
			try {
				Object result = performCalculation(m_args);
				
	            Token t = Token.createOperandToken(result);
	            Token.postResult(m_transactionId, t);
			}
			catch (Exception e) {
	            Token errToken = Token.createErrorToken(e);
	            Token.postResult(m_transactionId, errToken);
			}
		}
	}
}
