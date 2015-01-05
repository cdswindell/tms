package org.tms.api;

import org.tms.teq.BuiltinOperator;
import org.tms.teq.Token;
import org.tms.teq.TokenType;

public interface Operator
{
    public TokenType getTokenType();
    
    public int getPriority();

    public String getLabel();

    public int numArgs();
    
    public Class<?> [] getArgTypes();

    public Token evaluate(Token... args);
    
    default BuiltinOperator getBuiltinOperator() 
    {
        return BuiltinOperator.NULL_operator;
    }
}