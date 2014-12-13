package org.tms.teq;

import java.util.ArrayDeque;
import java.util.Iterator;

public class EquationStack extends ArrayDeque<Token> implements Iterable<Token>
{
    private static final long serialVersionUID = 112242556423961843L;

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
        t.setOperator(Operator.NULL_operator);
        t.setValue(value);
        
        push(t);
    }

    public void push(TokenType tType, Operator oper)
    {
        Token t = new Token(tType);
        t.setOperator(oper);
       
        push(t);
    }

    public void push(Operator oper)
    {
        Token t = new Token(oper.getPrimaryTokenType());
        t.setOperator(oper);
        
        push(t);
    }
    
    public String toString()
    {
        if (isEmpty())
            return "[ <empty> ]";
        else {
            StringBuffer sb = new StringBuffer();
            sb.append("[ ");
            
            Iterator<Token> di = this.descendingIterator();
            while (di != null && di.hasNext()) {
                sb.append("{").append(di.next().toString()).append("} ");
            }
            
            sb.append("]");
            return sb.toString();
        }
    }
}
