package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.tms.api.Table;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;

public class EquationStack extends ArrayDeque<Token> implements Iterable<Token>
{
    private static final long serialVersionUID = 7494817050310908470L;

    static public EquationStack createInfixStack()
    {
        EquationStack s = new EquationStack(StackType.Infix );
        return s;
    }
    
    static public EquationStack createInfixStack(Table primaryTable)
    {
        EquationStack s = new EquationStack(StackType.Infix, primaryTable);
        return s;
    }
    
    static public EquationStack createPostfixStack()
    {
        EquationStack s = new EquationStack(StackType.Postfix );
        return s;
    }
    
    static public EquationStack createPostfixStack(Table primaryTable)
    {
        EquationStack s = new EquationStack(StackType.Postfix, primaryTable);
        return s;
    }
    
    static public EquationStack createOpStack()
    {
        EquationStack s = new EquationStack(StackType.Op );
        return s;
    }
    
    private StackType m_stackType;
    private Table m_primaryTable;
    private TokenMapper m_tokenMapper;
	
    EquationStack(StackType st) 
    {
        this(st, (Table)null);
    }
    
    EquationStack(StackType st, Table primaryTable) 
    {
        super();
        m_stackType = st;
        m_primaryTable = primaryTable;
    }
    
    protected EquationStack(EquationStack eqS) 
    {
        super();
        m_stackType = eqS.getStackType();
        
        this.addAll(eqS);
    }
    
    public Table getPrimaryTable()
    {
        return m_primaryTable;
    }
    
	public TokenMapper getTokenMapper()
	{
		return m_tokenMapper;
	}
	
	public void setTokenMapper(TokenMapper tm) 
	{
		m_tokenMapper = tm;
	}
	
    @Override
	public boolean add(Token t) 
    {
		return super.add(t);
	}

	@Override
	public void addFirst(Token t) 
	{
		super.addFirst(t);
	}

	@Override
	public void addLast(Token t) 
	{
		super.addLast(t);
	}

	@Override
	public boolean offer(Token t) 
	{
		return super.offer(t);
	}

	@Override
	public boolean offerFirst(Token t)
	{
		return super.offerFirst(t);
	}

	@Override
	public boolean offerLast(Token t) 
	{
		return super.offerLast(t);
	}

    @Override
    public void push(Token t) 
    {
        super.push(t);
    }

    public void push (double value)
    {
        Token t = new Token(TokenType.Operand);
        t.setOperator(BuiltinOperator.NOP);
        t.setValue(value);
        
        push(t);
    }

    public void push(TokenType tType, Operator oper)
    {
        Token t = new Token(tType);
        t.setOperator(oper);
       
        push(t);
    }

    public void push(TokenType tType, Operator oper, Object value)
    {
        Token t = new Token(tType);
        t.setOperator(oper);
        t.setValue(value);
        
        push(t);
    }

    public void push(TokenType tType, Object value)
    {
        Token t = new Token(tType);
        t.setOperator(BuiltinOperator.NOP);
        t.setValue(value);
       
        push(t);
    }

    public void push(Operator oper)
    {
        Token t = new Token(oper.getTokenType());
        t.setOperator(oper);
        
        push(t);
    }
    
	public boolean isLeading()
    {
        if (isEmpty())
            return true;
        
        Token t = this.peek();
        return t != null ? t.isLeading() : true;
    }
    
	/**
	 * Generate a free-text expression from this stack
	 * @return a free-text mathematical expression
	 */
    public String toExpression()
    {
        return toExpression(m_stackType == StackType.Postfix, m_primaryTable);
    }
    
    /**
     * For internal use only; controls spaces between tokens and
     * defines the primary table; references to TableElements in
     * other tables are appropriately referenced
     * 
     * @param addExtraSpaces
     * @param primaryTable
     * @return
     */
    String toExpression(boolean addExtraSpaces, Table primaryTable)
    {
        if (isEmpty())
            return null;
        else {
            StringBuffer sb = new StringBuffer();
            
            Iterator<Token> di = this.descendingIterator();
            boolean addLeadingSpace = false;
            TokenType lastTT = null;
            Operator lastO = null;
            while (di != null && di.hasNext()) {
                Token t = di.next();
                TokenType tt = t.getTokenType();
                
                if ((addExtraSpaces && addLeadingSpace) || 
                        (lastO != BuiltinOperator.NegOper && lastTT == TokenType.UnaryFunc && tt != TokenType.LeftParen)) 
                    sb.append(' ');
                
                if (!addExtraSpaces && t.isBasicOperator()) sb.append(' ');                
                sb.append(t.toExpressionValue(primaryTable));
                if (!addExtraSpaces && (t.isBasicOperator() || tt == TokenType.Comma )) sb.append(' ');
                
                addLeadingSpace = true;
                lastTT = tt;
                lastO = t.getOperator();
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Derive a clear-text expression of the specified type 
     * from the given EquationStack. 
     * 
     * @param type the desired stack type (InFix or PostFix)
     * @return clear-text expression
     */
    public String toExpression(StackType type)
    {
        if (type == null)
            throw new IllegalArgumentException("StackType required");
        else if (this.getStackType() == type)
            return toExpression();
        else if (type == StackType.Postfix && this.getStackType() == StackType.Infix) {
            PostfixStackGenerator psg = new PostfixStackGenerator(this, m_primaryTable);
            psg.convertInfixToPostfix();
            return psg.getPostfixStack().toExpression();
        }
        else if (type == StackType.Infix && this.getStackType() == StackType.Postfix) {
            EquationStack operands = new EquationStack(StackType.Op, m_primaryTable);
            
            Iterator<Token> di = this.descendingIterator();
            while (di != null && di.hasNext()) {
                Token t = di.next();
                if (t.isOperand() || t.isReference() || t.isBuiltIn() || t.isExpression()) {
                    operands.push(t);
                    continue;
                }
                else if (t.isOperator()) {
                    Operator op = t.getOperator();
                    int numArgs = t.getOperator().numArgs();
                    int priority = op.getPriority();
                    
                    StringBuffer expr = new StringBuffer();
                    if (t.getTokenType() == TokenType.UnaryOp) {
                        Token x = operands.pop();
                        expr.append(op.getLabel());                            
                        expr.append(x.toExpressionValue(m_primaryTable));
                    }
                    else if (t.getTokenType() == TokenType.UnaryTrailingOp) {
                        Token x = operands.pop();
                        expr.append(x.toExpressionValue(m_primaryTable));
                        expr.append(op.getLabel());                            
                    }
                    else if (t.getTokenType() == TokenType.BinaryOp) {
                        Token y = operands.pop();
                        Token x = operands.pop();
                        
                        if (putInParens(priority))
                            expr.append('(');
                            
                        expr.append(x.toExpressionValue(m_primaryTable)).append(' ');
                        expr.append(op.getLabel()).append(' ');
                        expr.append(y.toExpressionValue(m_primaryTable));
                        
                        if (putInParens(priority))
                            expr.append(')');
                    } 
                    else { // some kind of function
                        Token[] args = new Token[numArgs];
                        
                        for (int i = numArgs - 1; i >= 0; i--) {
                            args[i] = operands.pop();
                        }
                        
                        // special case neg func
                        if (op == BuiltinOperator.NegOper && numArgs == 1)
                            expr.append("-").append(args[0].toExpressionValue(m_primaryTable));
                        else {
                            expr.append(op.getLabel()).append('(');
                            
                            for (int i = 0; i < numArgs; i++) {
                                if (putInParens(args[i], numArgs))
                                    expr.append('(');
                                
                                expr.append(args[i].toExpressionValue(m_primaryTable));
                                
                                if (putInParens(args[i], numArgs))
                                    expr.append(')');
                                
                                if ((i + 1) < numArgs)
                                    expr.append(", ");
                            }
                            
                            expr.append(')');
                        }
                    }
                    
                    operands.push(new Token(TokenType.Expression, expr.toString()));
                }  
                else {
                    throw new IllegalStateException(String.format("Unhandled token: %s Cannot convert %s stack to %s", t, this.getStackType(), type));
                }
            }
            
            // top of operands stack is our expression
            Token exprT = operands.pop();           
            String expr = exprT.toExpressionValue(m_primaryTable);
            
            while (expr.startsWith("(") && expr.endsWith(")") && isBalancedParens(expr)) 
            {
                expr = expr.substring(1, expr.length() - 1);
            }
            
            return expr;
        }
        
        throw new IllegalStateException(String.format("Cannot convert %s stack to %s", this.getStackType(), type));
    }
    
    /**
     * Used by toExpression to help remove extra sets of surrounding
     * parens to make generated expressions more readable
     * @param expr
     * @return
     */
    protected boolean isBalancedParens(String expr)
    {
        int parenCnt = 0;
        expr = expr.substring(1, expr.length() - 1);
        for (char c : expr.toCharArray()) {
            if (c == '(')
                parenCnt++;
            else if (c == ')') {
                if (parenCnt == 0)
                    return false;
                parenCnt--;
            }
        }
        
        return parenCnt == 0;
    }

    private boolean putInParens(int priority)
    {
        return priority <= 2;
    }

    private boolean putInParens(Token token, int numArgs)
    {
        return numArgs > 1 && 
               token.isExpression() && 
               !token.toExpressionValue(m_primaryTable).startsWith("(") ;        
    }

    public String toString()
    {
        if (isEmpty())
            return "[ <empty> ]";
        else {
            StringBuffer sb = new StringBuffer();
            sb.append(m_stackType.toString()).append(" Stack: ").append("[ ");
            
            Iterator<Token> di = this.descendingIterator();
            while (di != null && di.hasNext()) {
                sb.append("{").append(di.next().toString()).append("} ");
            }
            
            sb.append("]");
            return sb.toString();
        }
    }

	public StackType getStackType() 
	{
		return m_stackType;
	}

	/**
	 * Reverse stack elements
	 * @param numArgs
	 */
    public void reverse(int numArgs)
    {
        if (numArgs > 0 && numArgs <= this.size()) {
            Token [] args = new Token [numArgs];
            
            for (int i = 0; i < numArgs; i++) {
                args[i] = this.pop();
            }
            
            for (int i = 0; i < numArgs; i++) {
                this.push(args[i]);
            }
        }       
    }
}
