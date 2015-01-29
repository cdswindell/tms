package org.tms.teq;

public enum ParserStatusCode
{
    Success,
    EmptyExpression,
    EmptyStack,
    ParenMismatch,
    ArgumentTypeMismatch,
    ArgumentCountMismatch,
    CircularReference,
    IncompleteExpresion,
    InvalidCellReference,
    InvalidColumnReference,
    InvalidRangeReference,
    InvalidRowReference, 
    InvalidTableReference,
    InvalidExpression,
    InvalidExpressionStack,
    InvalidNumericExpression,
    InvalidOperandLocation,
    InvalidFunctionTarget,
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
