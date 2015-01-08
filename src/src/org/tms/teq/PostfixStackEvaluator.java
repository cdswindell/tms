package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Column;
import org.tms.api.Operator;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
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
        return evaluate(null, null);
    }
    
    public Token evaluate(Row row, Column col)
    {
        m_opStack = new EquationStack(StackType.Op);
        m_pfsIter = m_pfs.descendingIterator();
        
        return reevaluate(row, col);
    }
    
	public Token reevaluate(Row row, Column col)
	{
		assert m_pfs != null : "Requires Postfix Stack";
		
		if (m_opStack == null)
			m_opStack = new EquationStack(StackType.Op);
		
		if (m_pfsIter == null)
			m_pfsIter = m_pfs.descendingIterator();
		
		Token x;
		Token y;
		int numArgs;
		Token [] args;
		
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
					x = m_opStack.pollFirst();
					if (x == null || !x.isOperand()) // stack is in invalid state
					    return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);					
					m_opStack.push(doUnaryOp(oper, x));					
					break;
					
				case BinaryOp:
				case BinaryFunc:
					y = m_opStack.pollFirst();
					if (y == null || !y.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(y == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    
					
					x = m_opStack.pollFirst();
					if (x == null || !x.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    

                    m_opStack.push(doBinaryOp(oper, x, y));					
					break;
					
                case GenericFunc:
                    args = null;
                    numArgs = oper.numArgs();
                    if (numArgs > 0) {
                        Class<?> [] argTypes = oper.getArgTypes();
                        
                        args = new Token[numArgs];
                        
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = m_opStack.pollFirst();
                            if (x == null || !x.isOperand()) // stack is in invalid state
                                return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);  
                            else if (!x.isA(argTypes[i]))
                                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);  
                            
                            args[i] = x;
                        }
                    }
                    
                    m_opStack.push(doGenericOp(oper, args));                 
                    break;
                    
                case BuiltIn:
                    m_opStack.push(doBuiltInOp(oper, row, col));                 
                    break;
                    
				default:
					throw new UnimplementedException(String.format("Unsupported token type: %s (%s)", tt, t));
			}
		}
		
		// opStack should only have one value at this point
		int stackSize = m_opStack.size();
        if (stackSize < 1)
            return Token.createErrorToken(ErrorCode.StackUnderflow);
        else if (stackSize > 1)
            return Token.createErrorToken(ErrorCode.StackOverflow);
        
        Token stackVal = m_opStack.pollFirst();
		Token retVal = stackVal;
		return retVal;
	}

    public Table getTable()
	{
	    return m_table;
	}
	
    private Token doGenericOp(Operator oper, Token... args)
    {
        Token t = oper.evaluate(args);
        return t;
    }

	private Token doBuiltInOp(Operator oper, Row row, Column col)
    {
	    assert oper.numArgs() == 0 : "Too many arguments";
	    
	    Token result = null;
        BuiltinOperator bio = oper.getBuiltinOperator();
        if (bio != null) {
            switch (bio) {
                case RowIndex:
                    if (row == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(row.getPropertyInt(TableProperty.Index));
                    break;
                    
                case ColumnIndex:
                    if (col == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(col.getPropertyInt(TableProperty.Index));
                    break;
                    
                default:
                    break;
            }
        }
        
        if (result != null)
            return result;
        
        // evaluate the result
        result = oper.evaluate();
        
        return result;
    }

    private Token doBinaryOp(Operator oper, Token x, Token y) 
	{
        if (x.isNull() || y.isNull())
            return Token.createNullToken();
        
		Token result = null;	
		BuiltinOperator bio = oper.getBuiltinOperator();
		if (bio != null) {
    		switch (bio) {
    			case PlusOper:
                case MinusOper:
                case MultOper:
                case DivOper:
                    result = doBuiltInOp(bio, x, y);
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

	private Token doBuiltInOp(BuiltinOperator bio, Token x, Token y)
    {
        if (x.isNumeric() && y.isNumeric())
            return doBuiltInOp(bio, x.getNumericValue(), y.getNumericValue());        
        else if (x.isString() && y.isString())
            return doBuiltInOp(bio, x.getStringValue(), y.getStringValue());
        else if (x.isString() && y.isNumeric())
            return doBuiltInOp(bio, x.getStringValue(), y.getNumericValue());
        
        // if a token mapper exists, look for a supporting overload
        TokenMapper tm = m_pfs.getTokenMapper();
        if (tm != null) {
        	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
        	if (oper != null) {
        		Token t = oper.evaluate(x, y);
        		return t;
        	}
        }
        
        throw new UnimplementedException(String.format("Unimplemented built in operator: %s (%s, %s)", 
                bio, 
                x.getDataType() != null ? x.getDataType().getSimpleName() : "null",
                y.getDataType() != null ? y.getDataType().getSimpleName() : "null"));    
    }

    private Token doBuiltInOp(BuiltinOperator bio, String s1, Double n2)
    {
        switch (bio) {
            case MultOper:
                if (n2 >= 0.5) {
                    StringBuffer sb = new StringBuffer();
                    long max = Math.round(n2);
                    for (int i = 0; i < max; i++) {
                        sb.append(s1);
                    }
                    
                    return new Token(TokenType.Operand, sb.toString());
                }
                else
                   return Token.createErrorToken(ErrorCode.InvalidOperand);
                
            default:
                // if a token mapper exists, look for a supporting overload
                TokenMapper tm = m_pfs.getTokenMapper();
                if (tm != null) {
                	Token x = new Token(TokenType.Operand, s1);
                	Token y = new Token(TokenType.Operand, n2);
                	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
                	if (oper != null) {
                		Token t = oper.evaluate(x, y);
                		return t;
                	}
                }
                throw new UnimplementedException("Unimplemented built in String/Numeric operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, String s1, String s2)
    {
        switch (bio) {
            case PlusOper:
                return new Token(TokenType.Operand, s1.concat(s2));
                
            case MinusOper:
                return new Token(TokenType.Operand, s1.replace(s2, ""));
                
            default:
                // if a token mapper exists, look for a supporting overload
                TokenMapper tm = m_pfs.getTokenMapper();
                if (tm != null) {
                	Token x = new Token(TokenType.Operand, s1);
                	Token y = new Token(TokenType.Operand, s2);
                	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
                	if (oper != null) {
                		Token t = oper.evaluate(x, y);
                		return t;
                	}
                }
                throw new UnimplementedException("Unimplemented built in String operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, double x, double y)
    {
        switch (bio) {
            case PlusOper:
                return new Token(x + y);
                
            case MinusOper:
                return new Token(x - y);
                
            case MultOper:
                return new Token(x * y);
                
            case DivOper:
                if (y == 0.0)
                    return Token.createErrorToken(ErrorCode.DivideByZero);
                else
                    return new Token(x / y);
            default:
                throw new UnimplementedException("Unimplemented built in operator: " + bio);    
        }
    }

    private Token doUnaryOp(Operator oper, Token x) 
	{
		if (x.isNull())
			return Token.createNullToken();
		
		Token val = oper.evaluate(x);
		
		return val;
	}
}
