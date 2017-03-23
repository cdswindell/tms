package org.tms.api.derivables;

import org.tms.api.exceptions.TableException;
import org.tms.teq.PostfixStackEvaluator;

public class InvalidOperandsException extends TableException 
{
	private static final long serialVersionUID = -7840335940404871292L;

	public InvalidOperandsException(PostfixStackEvaluator pse, Operator oper, Token... tokens)
	{
		super(String.format("Operator %s requires %d arg(s) of type: %s", 
				oper.toString(), oper.numArgs(), oper.getArgTypes() == null ? "none" : oper.getArgTypes().toString()));
	}
	
    public InvalidOperandsException(String msg)
    {
        super(msg);
    }
}
