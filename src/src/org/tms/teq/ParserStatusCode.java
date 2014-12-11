package org.tms.teq;

public enum ParserStatusCode
{
    Success,
    EmptyExpression,
    ParenMismatch,
    IncompleteExpresion,
    InvalidExpression,
    ;
    
    public boolean isSuccess()
    {
        return this == Success;
    }
    
    public boolean isFailure()
    {
        return !isSuccess();
    }
}
