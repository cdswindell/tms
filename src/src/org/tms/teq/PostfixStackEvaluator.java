package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Operator;
import org.tms.api.Table;
import org.tms.api.exceptions.InvalidOperandsException;
import org.tms.api.exceptions.UnimplementedException;

public class PostfixStackEvaluator 
{
	private EquationStack m_pfs;
	private Table m_table;
	private EquationStack m_opStack;
	private Iterator<Token> m_pfsIter;
	
	public PostfixStackEvaluator(String expr, Table table)
	{
		PostfixStackGenerator psg = new PostfixStackGenerator(expr, table);
		m_table = table;
		m_pfs = psg.getPostfixStack();
	}
	
    public Token evaluate()
    {
        m_opStack = new EquationStack(StackType.Op);
        m_pfsIter = m_pfs.descendingIterator();
        
        return reevaluate();
    }
    
	public Token reevaluate()
	{
		assert m_pfs != null : "Requires Postfix Stack";
		
		Token retVal = null;
		if (m_opStack == null)
			m_opStack = new EquationStack(StackType.Op);
		
		if (m_pfsIter == null)
			m_pfsIter = m_pfs.descendingIterator();
		
		Token x;
		Token y;
		
		// walk through postfix stack from tail to head
		while(m_pfsIter.hasNext()) {
			Token t = m_pfsIter.next();
			
			TokenType tt = t.getTokenType();
			Operator oper = t.getOperator();
			Object value = t.getValue();
			
			switch (tt) {
				case Operand:
					m_opStack.push(tt, value);
					break;
					
				case UnaryOp:
				case UnaryFunc:
					x = m_opStack.removeFirst();
					if (x == null || !x.isOperand()) // stack is in invalid state
						throw new InvalidOperandsException(this, oper, x);
					
					m_opStack.push(doUnaryOp(oper, x));					
					break;
					
				case BinaryOp:
				case BinaryFunc:
					y = m_opStack.removeFirst();
					if (y == null || !y.isOperand()) // stack is in invalid state
						throw new InvalidOperandsException(this, oper, y);
					
					x = m_opStack.removeFirst();
					if (x == null || !x.isOperand()) // stack is in invalid state
						throw new InvalidOperandsException(this, oper, y, x);

                    m_opStack.push(doBinaryOp(oper, x, y));					
					break;
					
				case BuiltIn:
                    x = m_opStack.peekFirst();
                    if (x != null) // stack is in invalid state
                        throw new InvalidOperandsException(this, oper, x);
                    m_opStack.push(doBuiltInOp(oper));                 
				    break;
					
				default:
					throw new UnimplementedException(String.format("Unsupported token type: %s (%s)", tt, t));
			}
		}
		
		// opStack should only have one value at this point
		x = m_opStack.removeFirst();
		
		retVal = new Token(TokenType.Operand, x.getValue());
		return retVal;
	}

	private Token doBuiltInOp(Operator oper)
    {
	    assert oper.numArgs() == 0 : "Too many arguments";
	    
        // evaluate the result
        Token result = oper.evaluate();
        
        return result;
    }

    private Token doBinaryOp(Operator oper, Token x, Token y) 
	{
        if (x.isNull() || y.isNull())
            return Token.createNullToken();
        
		Object xVal = x.getValue();
		Object yVal = y.getValue();
		Token result = null;
		
		BuiltinOperator bio = oper.getBuiltinOperator();
		if (bio != null) {
    		switch (bio) {
    			case PlusOper:
    				result = addArgs(xVal, yVal);
    				break;
    				
                case MinusOper:
                    result = addArgs(xVal, yVal);
                    break;
                    
                case MultOper:
                    result = addArgs(xVal, yVal);
                    break;
                    
                case DivOper:
                    result = addArgs(xVal, yVal);
                    break;
                    
    			default:
    			    break;
    		}
    		
    		if (result != null)
    		    return result;
		}
		
		// evaluate the result
		result = oper.evaluate(x, y);
		
		return result;
	}

	private Token addArgs(Object xVal, Object yVal) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private Token doUnaryOp(Operator oper, Token x) 
	{
		if (x.isNull())
			return Token.createNullToken();
		
		Token val = oper.evaluate(x);
		
		return val;
	}
}
