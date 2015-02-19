package org.tms.api.derivables.exceptions;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.TableException;
import org.tms.teq.PostfixStackEvaluator;

public class InvalidOperandsException extends TableException 
{
	private static final long serialVersionUID = 7950818427496797681L;

	public InvalidOperandsException(PostfixStackEvaluator pse, Operator oper, Token... tokens)
	{
		// TODO: figure out required arg types
		super(String.format("Operator %s requires %d arg(s) of type: %s", 
				oper.toString(), oper.numArgs(), "???"));
	}
	
    public InvalidOperandsException(String msg)
    {
        super(msg);
    }
}
