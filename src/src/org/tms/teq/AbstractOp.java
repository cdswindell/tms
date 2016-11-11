package org.tms.teq;

import org.tms.api.derivables.TokenType;
import org.tms.api.utils.AbstractOperator;

public abstract class AbstractOp extends AbstractOperator 
{
	abstract public Object performCalculation(Object [] m_args) throws Exception;
	
	public AbstractOp(String label, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, argTypes, resultType);
	}

	public AbstractOp(String label, Class<?>[] argTypes, Class<?> resultType, String... categories) 
	{
		super(label, argTypes, resultType, categories);
	}
	
	@Override
	final public String getLabel()
	{
		return super.getLabel();
	}

	@Override
	final public TokenType getTokenType() 
	{
		return super.getTokenType();
	}
	
	@Override
	final public Class<?> getResultType() 
	{
		return super.getResultType();
	}
	
	@Override
    final public int getPriority()
    {
        return super.getPriority();
    }

	@Override
    final public boolean isVariableArgs()
    {
        return super.isVariableArgs();
    }
    
	@Override
    public boolean isRightAssociative() 
    {
        return super.isRightAssociative();
    }
	
	@Override
	final public String[] getCategories()
	{
		return super.getCategories();
	}
}
