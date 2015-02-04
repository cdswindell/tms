package org.tms.api;

import org.tms.teq.BuiltinOperator;
import org.tms.teq.Token;
import org.tms.teq.TokenType;

public interface Operator
{
    public String getLabel();

    public TokenType getTokenType();
    
    public Class<?> [] getArgTypes();

    public Token evaluate(Token... args);
    
    default public int numArgs()
    {
        Class<?> [] args = getArgTypes();
        
        return args != null ? args.length : 0;
    }
    
    default public int getPriority()
    {
        return 5;
    }

    default BuiltinOperator getBuiltinOperator() 
    {
        return BuiltinOperator.NULL_operator;
    }
}