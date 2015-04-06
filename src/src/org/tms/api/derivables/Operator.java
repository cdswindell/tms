package org.tms.api.derivables;

import org.tms.teq.BuiltinOperator;

public interface Operator extends Labeled
{
    public TokenType getTokenType();
    
    public Class<?> [] getArgTypes();

    public Token evaluate(Token... args);
    
    default public Class<?> getResultType()
    {
        return Object.class;
    }
    
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