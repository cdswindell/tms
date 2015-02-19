package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Table;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.derivables.exceptions.InvalidExpressionException;

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
        m_pfs = EquationStack.createPostfixStack();
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
        // until one with a lessor priority is encountered        
        EquationStack ops = new EquationStack(StackType.Op);
        int infixParens = 0;
        Token t = null;
        Operator oper = null;
        boolean endRight = false;
        int p1, p2;
        
        // process the infix stack in reverse order (not from head, but from tail)
        Iterator<Token> di = ifs.descendingIterator();
        while(di != null && di.hasNext()) {
            Token ift = di.next();
            TokenType tt = ift.getTokenType();
            
            switch (tt) {
                case Constant:
                case ColumnRef:
                case RowRef:
                case SubsetRef:
                case CellRef:
                case TableRef:
                case BuiltIn:
                    pfs.push(tt, ift.getOperator(), ift.getValue());
                    break;
                
                case Operand:
                    pfs.push(TokenType.Operand, ift.getValue());
                    break;
                    
                case LeftParen:
                    infixParens++;
                    ops.push(tt, BuiltinOperator.Paren);
                    break;
                    
                case RightParen:
                    infixParens--;
                    t = ops.pollFirst();
                    while (t != null && !t.isLeftParen()) {
                        pfs.push(t.getTokenType(), t.getOperator());
                        t = ops.pollFirst();
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
                        else if (p1 == p2 && p2 == BuiltinOperator.MAX_PRIORITY)
                            endRight = true;
                        else {
                            endRight = false;
                            t = ops.pop();
                            assert t != null : "Ops stack token is null";
                            pfs.push(t.getTokenType(), t.getOperator());
                        }
                    } while (!endRight);
                    ops.push(tt, oper);
                    break;
                                        
                case Comma: // eat commas, unless the top of the ops stack is a multi-arg function
                    if (!ops.isEmpty()) {
                        t = ops.peek();
                        if (t != null && (t.isFunction() && 
                                         (t.getOperator() != null ? t.getOperator().numArgs() : t.getTokenType().numArgs()) > 1)) 
                        {
                            ops.pop(); // remove the element, as we are about to process it
                            pfs.push(t);
                        }
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
        assert infixParens == 0 : "Parens count mismatch: " + infixParens;
        
        // clear the ops stack
        while (!ops.isEmpty()) {
            t = ops.pop();
            assert t != null : "Ops stack token is null";
            pfs.push(t.getTokenType(), t.getOperator());
        }
        
        return pr;
    }
}
