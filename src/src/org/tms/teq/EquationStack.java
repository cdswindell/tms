package org.tms.teq;

import java.util.ArrayDeque;

public class EquationStack extends ArrayDeque<Token>
{
    private static final long serialVersionUID = 112242556423961843L;

    public boolean isLeading()
    {
        if (isEmpty())
            return true;
        
        Token t = this.peek();
        return t.isLeading();
    }
    
    public void push (TokenType tt, double value)
    {
        Token t = new Token(tt);
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
}
