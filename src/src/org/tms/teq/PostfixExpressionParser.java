package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Table;
import org.tms.api.exceptions.InvalidExpressionException;

public class PostfixExpressionParser
{
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private Table m_table;
    
    public PostfixExpressionParser(String infixExpr, Table table)
    {
        InfixExpressionParser ifp = new InfixExpressionParser(infixExpr);
        ParseResult pr = ifp.parseInfixExpression(table);
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);
        
        m_ifs = ifp.getInfixStack();
        m_table = table;
    }

    public PostfixExpressionParser(InfixExpressionParser ife)
    {
       if (ife.getInfixStack() == null || ife.getInfixStack().isEmpty()) {
           ParseResult pr = ife.parseInfixExpression();
           if (pr != null && pr.isFailure())
               throw new InvalidExpressionException(pr);
       }
       
       m_ifs = ife.getInfixStack();
       m_table = ife.getTable();
    }

    public PostfixExpressionParser(EquationStack ifs, Table table)
    {
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
        m_pfs = new EquationStack();
        return convertInfixToPostfix(m_ifs, m_pfs);
    }
    
    protected ParseResult convertInfixToPostfix(EquationStack ifs, EquationStack pfs)
    {
        ParseResult pr = new ParseResult();
        
        if (ifs == null || ifs.isEmpty()) 
            return pr.addIssue(ParserStatusCode.EmptyStack, 0); 
        
        if (pfs == null)
            return pr.addIssue(ParserStatusCode.EmptyStack, 0); 
        
        // create the temporary op stack
        EquationStack ops = new EquationStack();
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
                case RangeRef:
                case BuiltIn:
                    pfs.push(tt, ift.getOperator());
                    break;
                
                case CellRef:
                    break;
                   
                case Operand:
                    pfs.push(ift.getNumericValue());
                    break;
                    
                case LeftParen:
                    infixParens++;
                    ops.push(tt, Operator.Paren);
                    break;
                    
                case RightParen:
                    infixParens--;
                    t = ops.pollFirst();
                    while (t != null && !t.isLeftParen()) {
                        pfs.push(t.getTokenType(), t.getOperator());
                        t = ops.pollFirst();
                    }
                    break;
                    
                case BinaryOp:
                    oper = ift.getOperator();
                    do {
                        if (ops.isEmpty())
                            endRight = true;
                        else if ((t = ops.peek()) != null && t.isLeftParen())
                            endRight = true;
                        else if ((p1 = t.getPriority()) <
                                 (p2 = oper.getPriority()))
                            endRight = true;
                        else if (p1 == p2 && p2 == Operator.MAX_PRIORITY)
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
                    
                case BinaryFunc:
                case Comma:
                case GenericBinaryFunc:
                case GenericBinaryOp:
                case GenericOp:
                case GenericUnaryOp:
                case LAST_TokenType:
                case NULL_TokenType:
                case NullOpValue:
                case RangeOp:
                case StatOp:
                case String:
                case TableRef:
                case UnaryFunc:
                case UnaryOp:
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
