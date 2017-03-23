package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.derivables.InvalidExpressionException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;

public class PostfixStackGenerator
{
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private Table m_table;
    
    public PostfixStackGenerator(String infixExpr, Table table)
    {
        InfixExpressionParser ifp = new InfixExpressionParser(infixExpr, table);
        ParseResult pr = ifp.parseInfixExpression(table);
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);
        
        m_ifs = ifp.getInfixStack();
        m_table = table;
    }

    public PostfixStackGenerator(InfixExpressionParser ife)
    {
    	if (ife.getInfixStack() == null || ife.getInfixStack().isEmpty()) {
    		ParseResult pr = ife.parseInfixExpression();
    		if (pr != null && pr.isFailure())
    			throw new InvalidExpressionException(pr);
    	}

    	m_ifs = ife.getInfixStack();
    	if (m_ifs == null) {
    		ParseResult pr = new ParseResult(ParserStatusCode.EmptyStack);
    		throw new InvalidExpressionException(pr);            
    	}

    	m_table = ife.getTable();
    }

    public PostfixStackGenerator(EquationStack ifs, Table table)
    {
    	if (ifs == null) {
            ParseResult pr = new ParseResult(ParserStatusCode.EmptyStack);
            throw new InvalidExpressionException(pr);            
    	}
    	
    	if (ifs.getStackType() != StackType.Infix) {
            ParseResult pr = new ParseResult(ParserStatusCode.InvalidExpressionStack);
            throw new InvalidExpressionException(pr);            
    	}
    	
        m_ifs = ifs;
        m_table = table;
    }
    
    public Table getTable()
    {
        return m_table;
    }
    
    public EquationStack getInfixStack()
    {
        return m_ifs;
    }
    
    public EquationStack getPostfixStack()
    {
        if (m_pfs == null) {
            ParseResult pr = convertInfixToPostfix();
                if (pr != null && pr.isFailure())
                    throw new InvalidExpressionException(pr);
        }
        
        return m_pfs;
    }
    
    protected ParseResult convertInfixToPostfix()
    {
        m_pfs = EquationStack.createPostfixStack(m_table);
        return convertInfixToPostfix(m_ifs, m_pfs);
    }
    
    protected ParseResult convertInfixToPostfix(EquationStack ifs, EquationStack pfs)
    {
        ParseResult pr = new ParseResult();
        
        if (ifs == null || ifs.isEmpty()) 
            return pr.addIssue(ParserStatusCode.EmptyStack, "Infix Stack"); 
        
        if (pfs == null)
            return pr.addIssue(ParserStatusCode.EmptyStack, "Postfix Stack"); 
        
        // set the postfix stac k's token mapper to that of the ifs
        // the token mapper may be used to process operator overloads
        // during evaluation
        pfs.setTokenMapper(ifs.getTokenMapper());
        
        // create the temporary op stack, ops will be pushed onto this stack 
        // until one with a lesser priority is encountered        
        EquationStack ops = new EquationStack(StackType.Op);
        EquationStack operands = new EquationStack(StackType.Op);
        Token t = null;
        Operator oper = null;
        boolean endRight = false;
        int infixParens = 0;
        int p1, p2;
        
        // process the infix stack in reverse order (not from head, but from tail)
        Iterator<Token> di = ifs.descendingIterator();
        while(di != null && di.hasNext()) {
            Token ift = di.next();
            TokenType tt = ift.getTokenType();
            
            switch (tt) {
                case ColumnRef:
                case RowRef:
                case SubsetRef:
                case CellRef:
                case TableRef:
                    pfs.push(tt, ift.getOperator(), ift.getValue());
                    operands.push(tt, ift.getOperator(), ift.getValue());
                    break;
                    
                case BuiltIn:
                    pfs.push(tt, ift.getOperator(), ift.getValue());
                    operands.push(TokenType.OperandDataType, ift.getOperator().getResultType());
                    break;
                
                case Constant:
                case Operand:
                    pfs.push(TokenType.Operand, ift.getValue(), ift.getLabel());
                    operands.push(TokenType.Operand, ift.getValue());
                    break;
                    
                case LeftParen:
                    infixParens++;
                    ops.push(tt, BuiltinOperator.Paren);
                    break;
                    
                case RightParen:
                    infixParens--;
                    t = ops.pollFirst();
                    while (t != null && !t.isLeftParen()) {
                    	if (validateOp(t, pfs, operands, pr)) 
                            t = ops.pollFirst();
                    	else 
                    		return pr;
                    }
                    break;
                    
                case UnaryOp:
                case UnaryFunc:
                case UnaryTrailingOp:
                case BinaryFunc:
                case BinaryOp:
                case GenericFunc:
                case StatOp:
                case TransformOp:
                    oper = ift.getOperator();
                    do {
                        if (ops.isEmpty())
                            endRight = true;
                        else if ((t = ops.peek()) != null && t.isLeftParen())
                            endRight = true;
                        else if ((p1 = t.getPriority()) <
                                 (p2 = oper.getPriority()))
                            endRight = true;
                        else if (p1 == p2 && tt == TokenType.BinaryOp && oper.isRightAssociative())
                            endRight = true;
                        else if (p1 == p2 && p2 == BuiltinOperator.MAX_PRIORITY)
                            endRight = true;
                        else {
                            t = ops.pop();
                            if (t == null || t.getOperator() == null) 
                                return pr.addIssue(ParserStatusCode.EmptyStack, "Ops stack is empty"); 
                            
                        	if (validateOp(t, pfs, operands)) 
                            	endRight = false;
                            else {
                            	ops.push(t.getTokenType(), t.getOperator());
                            	endRight = true;
                            }
                        }
                    } while (!endRight);
                    ops.push(tt, oper);
                    break;
                                        
                case Comma: // eat commas, unless ops stack has expressions in need of evaluation
                    if (!ops.isEmpty()) {                    	
                        do {
                            if (ops.isEmpty())
                                endRight = true;
                            else if ((t = ops.peek()) != null && t.isLeftParen())
                                endRight = true;
                            else {
                                t = ops.pop();
                                if (t == null || t.getOperator() == null) 
                                    return pr.addIssue(ParserStatusCode.EmptyStack, "Ops stack is empty"); 
                                
                            	if (validateOp(t, pfs, operands)) 
                                	endRight = false;
                                else {
                                	ops.push(t.getTokenType(), t.getOperator());
                                	endRight = true;
                                }
                            }
                        } while (!endRight);
                    }
                    break;
                    
                case LAST_TokenType:
                case NULL_TokenType:
                case NullOpValue:
                case Variable:
                default:
                    break;
            }            
        }

        // parens counter should be 0
        if (infixParens != 0) {
        	pr.addIssue(ParserStatusCode.ParenMismatch, "Unbalanced Parenthesis: " + 
        							(infixParens > 0 ? "too few \")\"" : "too many \")\""));
        }
        
        // clear the ops stack
        while (!ops.isEmpty()) {
            t = ops.pop();
            if (t == null) {
            	pr.addIssue(ParserStatusCode.InvalidExpressionStack, "Operator Needed");
            	break;
            }
            
            pfs.push(t.getTokenType(), t.getOperator());
        }
        
        if (pr.isSuccess())
            validatePostfixStack(pfs, pr);
        
        return pr;
    }

    private boolean validateOp(Token t, EquationStack pfs, EquationStack operands) 
    {
    	return validateOp(t, pfs, operands, null);
    }
    
    private boolean validateOp(Token t, EquationStack pfs, EquationStack operands, ParseResult pr) 
    {
    	Operator op = t.getOperator();
    	if (op == null) {
    		if (pr != null)
    			pr.addIssue(ParserStatusCode.InvalidOperandLocation, "Operator Missing");
    		return false;
    	}
    	
    	int numArgs = op.numArgs();
    	if (operands.size() < numArgs) {
    		if (pr != null)
    			pr.addIssue(ParserStatusCode.ArgumentCountMismatch, "Argument Count Mismatch");
    		return false;
    	}
    	
    	EquationStack tmpOperands = new EquationStack(StackType.Op);
    	if (!validateOp(op.getArgTypes(), operands, tmpOperands)) {
    		// restore operandStack
    		while (!tmpOperands.isEmpty())
    			operands.push(tmpOperands.pop());
    		
    		if (pr != null)
    			pr.addIssue(ParserStatusCode.ArgumentTypeMismatch, "Argument Type Mismatch");
    		return false;
    	}
        
        // we're good, push the op and record it's type on the stack
        pfs.push(t.getTokenType(), op);
        operands.push(TokenType.OperandDataType, op.getResultType());
        return true;
	}

	private boolean validateOp(Class<?>[] argTypes, EquationStack operands, EquationStack tmpOperands) 
	{
		for (int i = argTypes.length - 1; i >= 0; i--) {
			Class<?> reqType = argTypes[i];
			Token t = operands.pop();
			tmpOperands.push(t);
			
			if (!isA(t, reqType))
				return false;
		}
		
		return true;
	}

	private boolean isA(Token t, Class<?> reqType) 
	{
		if (t.isA(reqType, true) || t.isReference())
			return true;
		else if (t.getTokenType() == TokenType.OperandDataType && (t.getDataType() == Object.class || reqType == Object.class))
			return true;
		else
			return false;
	}

	private void validatePostfixStack(EquationStack pfs, ParseResult pr)
    {
        // process the infix stack in reverse order (not from head, but from tail)
        EquationStack opStack = new EquationStack(StackType.Op);
        Iterator<Token> di = pfs.descendingIterator();
        while(di != null && di.hasNext()) {
            Token t = di.next();
            TokenType tt = t.getTokenType();
            Operator oper = t.getOperator();
            Class<?> returnType = oper != null ? oper.getResultType() : Object.class;
            BuiltinOperator bio = oper != null && oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
            boolean isMathOper = bio != null ? bio.isMathOper() : false;
            Class<?> firstArgType = null;
            Token x;
            
            int numArgs;
            switch (tt) {
                case RowRef:
                case ColumnRef:
                case SubsetRef:
                case TableRef:
                case CellRef:
                case Operand:
                    Object value = t.getValue();
                    opStack.push(new ValidationToken(tt, value));
                    break;
                    
                case BuiltIn:
                    opStack.push(new ValidationToken(returnType));
                    break;
                    
                case UnaryOp:
                case UnaryFunc:
                case UnaryTrailingOp:
                case BinaryOp:
                case BinaryFunc:
                case GenericFunc:
                case TransformOp:
                case StatOp:
                    numArgs = oper.numArgs();
                    Class<?> [] argTypes = oper != null ? oper.getArgTypes() : null;    
                    if (numArgs > 0) {
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(opStack, argTypes[i]);
                            if (x == null) // stack is in invalid state
                                pr.addIssue(ParserStatusCode.ArgumentCountMismatch, oper.getLabel());
                            else if (!x.isA(argTypes[i]))
                                pr.addIssue(ParserStatusCode.ArgumentTypeMismatch, oper.getLabel());
                            else if (i == 0 && isMathOper)
                                firstArgType = x.getDataType();
                        }
                    }
                    
                    // args are good, push onto op stack an operand of the correct type
                    if (pr.isSuccess()) {
                        if (firstArgType != null)
                            opStack.push(new ValidationToken(firstArgType));
                        else
                            opStack.push(new ValidationToken(returnType));
                    }
                    break;
                    
                default:
                    break;
            }
            
            // if we encountered an error, return
            if (pr.isFailure())
                return;
        }
        
        // opStack should only have one value at this point
        int stackSize = opStack.size();
        if (stackSize < 1)
            pr.addIssue(ParserStatusCode.InvalidExpression, "Nothing Returned");
        else if (stackSize > 1)
            pr.addIssue(ParserStatusCode.IncompleteExpresion, "Incomplete Expression");
    }

    private Token asOperand(EquationStack opStack, Class<?> requiredArgType)
    {
        Token t = opStack.pollFirst();
        if (t == null)
            return t;        
        else if (t.isOperand() || t.isReference())
            return t;        
        else if (requiredArgType != null && requiredArgType != Object.class && t.isA(requiredArgType))
            return t;        
        else
            return t;
    }
    
    private static class ValidationToken extends Token
    {
        private Class<?> m_dataType;
        
        public ValidationToken(Class<?> dataType)
        {
            super(TokenType.Operand);
            m_dataType = dataType;
        }
        
        public ValidationToken(TokenType tt, Object value)
        {
            super(tt, value);
        }

        @Override
        public Class<? extends Object> getDataType()
        {
            if (m_dataType != null)
                return m_dataType;
            else
                return super.getDataType();
        }
        
        @Override
        public boolean isA(Class<?> targetClazz)
        {
            if (isReference()) 
                return true;
            else if (targetClazz == Object.class || m_dataType == Object.class)
                return true;
            else
                return isA(targetClazz, true);
        }
        
        @Override
        public boolean isReference()
        {
            if (m_dataType != null)
                return TableElement.class.isAssignableFrom(m_dataType);
            
            return super.isReference();
        }
        
        public String toString()
        {
            if (m_dataType != null)
                return String.format("[ %s %s]", m_dataType, getTokenType());
            else
                return super.toString();
        }       
    }
}
