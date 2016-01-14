package org.tms.api.utils;

import java.util.UUID;

import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.Token;
import org.tms.teq.AbstractOperator;

abstract public class AsynchronousOp extends AbstractOperator
{
	public AsynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, argTypes, resultType);
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
	        
	        // save away the args and the transaction id
	        CalculationRunner cr = new CalculationRunner(mArgs, Derivation.getTransactionID());
	        
			// return the pending token
	        return Token.createPendingToken(cr);
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
	            Derivation.postResult(m_transactionId, t);
			}
			catch (Exception e) {
	            Token errToken = Token.createErrorToken(e);
	            Derivation.postResult(m_transactionId, errToken);
			}
		}
	}
}
