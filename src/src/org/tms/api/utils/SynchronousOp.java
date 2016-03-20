package org.tms.api.utils;

import org.tms.api.derivables.Token;
import org.tms.teq.AbstractOperator;
import org.tms.teq.NullsNotAllowedException;

abstract public class SynchronousOp extends AbstractOperator
{
	public SynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, argTypes, resultType);
	}

	public SynchronousOp(String label, Class<?>[] argTypes, Class<?> resultType, String... categories) 
	{
		super(label, argTypes, resultType, categories);
	}

	@Override
	final public Token evaluate(Token... args) 
	{
		try {
	        // harvest args
	        Object [] mArgs = unpack(args);
	        
			// perform the calculation and return
			Object result = performCalculation(mArgs);
			
	        Token t = Token.createOperandToken(result);
	        return t;
		}
		catch (NullsNotAllowedException e) {
        	return Token.createNullToken();
		}
		catch (Exception e) {
            Token errToken = Token.createErrorToken(e);
            return errToken;
		}
	}
}
