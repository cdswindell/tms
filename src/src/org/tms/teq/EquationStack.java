package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;

public class EquationStack extends ArrayDeque<Token> implements Iterable<Token>
{
    static public EquationStack createInfixStack()
    {
        EquationStack s = new EquationStack(StackType.Infix );
        return s;
    }
    
    static public EquationStack createPostfixStack()
    {
        EquationStack s = new EquationStack(StackType.Postfix );
        return s;
    }
    
    private static final long serialVersionUID = 112242556423961843L;
    
    static Pattern sf_FuncPattern = Pattern.compile("^[a-zA-Z]+[a-zA-Z0-9]*\\(");
    private StackType m_stackType;

	private TokenMapper m_tokenMapper;

    protected EquationStack(StackType st) 
    {
        super();
        m_stackType = st;
    }
    
    protected EquationStack(EquationStack eqS) 
    {
        super();
        m_stackType = eqS.getStackType();
        
        this.addAll(eqS);
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
    
    public String toExpression()
    {
        if (isEmpty())
            return null;
        else {
            StringBuffer sb = new StringBuffer();
            
            Iterator<Token> di = this.descendingIterator();
            boolean addLeadingSpace = false;
            while (di != null && di.hasNext()) {
                if (addLeadingSpace) sb.append(' ');
                Token t = di.next();
                
                if (t.isString()) sb.append('"');
                sb.append(t.toString());
                if (t.isString()) sb.append('"');
                addLeadingSpace = true;
            }
            
            return sb.toString();
        }
    }
    
    public String toExpression(StackType type)
    {
        if (type == null)
            throw new IllegalArgumentException("StackType required");
        else if (this.getStackType() == type)
            return toExpression();
        else if (type == StackType.Postfix && this.getStackType() == StackType.Infix) {
            PostfixStackGenerator psg = new PostfixStackGenerator(this, null);
            psg.convertInfixToPostfix();
            return psg.getPostfixStack().toExpression();
        }
        else if (type == StackType.Infix && this.getStackType() == StackType.Postfix) {
            EquationStack operands = new EquationStack(StackType.Op);
            
            Iterator<Token> di = this.descendingIterator();
            while (di != null && di.hasNext()) {
                Token t = di.next();
                if (t.isOperand() || t.isReference() || t.isBuiltIn()) {
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
                        expr.append(x.toExpressionValue());
                    }
                    else if (t.getTokenType() == TokenType.UnaryTrailingOp) {
                        Token x = operands.pop();
                        expr.append(x.toExpressionValue());
                        expr.append(op.getLabel());                            
                    }
                    else if (t.getTokenType() == TokenType.BinaryOp) {
                        Token y = operands.pop();
                        Token x = operands.pop();
                        
                        if (putInParens(priority))
                            expr.append('(');
                            
                        expr.append(x.toExpressionValue()).append(' ');
                        expr.append(op.getLabel()).append(' ');
                        expr.append(y.toExpressionValue());
                        
                        if (putInParens(priority))
                            expr.append(')');
                    } 
                    else { // some kind of function
                        Token[] args = new Token[numArgs];
                        
                        for (int i = numArgs - 1; i >= 0; i--) {
                            args[i] = operands.pop();
                        }
                        
                        expr.append(op.getLabel()).append('(');
                        
                        for (int i = 0; i < numArgs; i++) {
                            if (putInParens(args[i]))
                                expr.append('(');
                            
                            expr.append(args[i].toExpressionValue());
                            
                            if (putInParens(args[i]))
                                expr.append(')');
                            
                            if ((i + 1) < numArgs)
                                expr.append(", ");
                        }
                        
                        expr.append(')');
                    }
                    
                    operands.push(new Token(TokenType.Expression, expr.toString()));
                }  
                else {
                    throw new IllegalStateException(String.format("Unhandled token: %s Cannot convert %s stack to %s", t, this.getStackType(), type));
                }
            }
            
            // top of operands stack is our expression
            Token exprT = operands.pop();           
            String expr = exprT.toExpressionValue();
            
            while (expr.startsWith("(") && expr.endsWith(")") && isBalancedParens(expr)) 
            {
                expr = expr.substring(1, expr.length() - 1);
            }
            
            return expr;
        }
        
        throw new IllegalStateException(String.format("Cannot convert %s stack to %s", this.getStackType(), type));
    }
    
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

    private boolean putInParens(Token token)
    {
        return token.isExpression() && 
               !token.toExpressionValue().startsWith("(") ;        
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
}
