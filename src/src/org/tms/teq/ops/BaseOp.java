package org.tms.teq.ops;

import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.utils.AbstractOperator;

abstract public class BaseOp extends AbstractOperator
{
    private TokenType m_tokenType;
    
    public BaseOp(String label, TokenType tt, Class<?> [] argTypes, Class<?> resultType)
    {
    	super(label, argTypes, resultType);
        m_tokenType = tt;
    }

    @Override
    public TokenType getTokenType()
    {
        return m_tokenType;
    }

	protected Object[] unpack(Token[] tokens) 
	{
        Object [] mArgs = new Object [numArgs()];
        Class<?> [] argTypes = getArgTypes();
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = unpackArg(tokens[i].getValue(), argTypes[i]);
        }
		
		return mArgs;
	}
}
