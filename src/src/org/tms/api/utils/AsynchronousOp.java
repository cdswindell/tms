package org.tms.api.utils;

import java.util.UUID;

import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.Token;
import org.tms.teq.AbstractOperator;

abstract public class AsynchronousOp extends AbstractOperator implements Runnable
{
	private Object [] m_args;
	private UUID m_transactionId;
		
	public AsynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, argTypes, resultType);
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
}
