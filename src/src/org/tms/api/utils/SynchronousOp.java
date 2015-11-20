package org.tms.api.utils;

import org.tms.api.derivables.Token;
import org.tms.teq.AbstractOperator;

abstract public class SynchronousOp extends AbstractOperator
{
	public SynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType) 
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
}
