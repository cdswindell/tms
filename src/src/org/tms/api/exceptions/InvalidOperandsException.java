package org.tms.api.exceptions;

import org.tms.teq.Operator;
import org.tms.teq.PostfixStackEvaluator;
import org.tms.teq.Token;

public class InvalidOperandsException extends TableException 
{
	private static final long serialVersionUID = 7950818427496797681L;

	public InvalidOperandsException(PostfixStackEvaluator pse, Operator oper, Token... tokens)
	{
		// TODO: figure out required arg types
		super(String.format("Operator %s requires %d arg(s) of type: %s", 
				oper.toString(), oper.numArgs(), "???"));
	}
}
