package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.tms.api.Operator;

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
    
    private StackType m_stackType;

    protected EquationStack(StackType st) 
    {
    	super();
    	m_stackType = st;
    }
    
    @Override
	public boolean add(Token t) 
    {
		return super.add(t.clone());
	}

	@Override
	public void addFirst(Token t) 
	{
		super.addFirst(t.clone());
	}

	@Override
	public void addLast(Token t) 
	{
		super.addLast(t.clone());
	}

	@Override
	public boolean offer(Token t) 
	{
		return super.offer(t.clone());
	}

	@Override
	public boolean offerFirst(Token t)
	{
		return super.offerFirst(t.clone());
	}

	@Override
	public boolean offerLast(Token t) 
	{
		return super.offerLast(t.clone());
	}

	@Override
	public void push(Token t) 
	{
		super.push(t.clone());
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
