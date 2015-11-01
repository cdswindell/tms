package org.tms.teq.exceptions;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.TableException;
import org.tms.teq.PostfixStackEvaluator;

public class InvalidOperandsExceptionImpl extends TableException 
{
    private static final long serialVersionUID = -1059953342112254603L;

	public InvalidOperandsExceptionImpl(PostfixStackEvaluator pse, Operator oper, Token... tokens)
	{
		super(String.format("Operator %s requires %d arg(s) of type: %s", 
				oper.toString(), oper.numArgs(), oper.getArgTypes() == null ? "none" : oper.getArgTypes().toString()));
	}
	
    public InvalidOperandsExceptionImpl(String msg)
    {
        super(msg);
    }
}
