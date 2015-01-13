package org.tms.teq;

public enum ParserStatusCode
{
    Success,
    EmptyExpression,
    EmptyStack,
    ParenMismatch,
    CircularReference,
    IncompleteExpresion,
    InvalidCellReferemce,
    InvalidColumnReferemce,
    InvalidRangeReferemce,
    InvalidRowReferemce,
    InvalidExpression,
    InvalidExpressionStack,
    InvalidNumericExpression,
    InvalidOperandLocation,
    InvalidCommaLocation,
    InvalidOperatorLocation,
    NoSuchOperator, 
    SingletonQuote
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
