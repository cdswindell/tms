package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Iterator;

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
    
    public boolean isLeading()
    {
        if (isEmpty())
            return true;
        
        Token t = this.peek();
        return t.isLeading();
    }
    
    public void push (double value)
    {
        Token t = new Token(TokenType.Operand);
        t.setOperator(Operator.NOP);
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
        t.setOperator(Operator.NOP);
        t.setValue(value);
       
        push(t);
    }

    public void push(Operator oper)
    {
        Token t = new Token(oper.getPrimaryTokenType());
        t.setOperator(oper);
        
        push(t);
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
                sb.append(di.next().toString());
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
