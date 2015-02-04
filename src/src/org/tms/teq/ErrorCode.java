package org.tms.teq;

public enum ErrorCode
{
    DivideByZero, 
    NaN,
    Infinity,
    InvalidOperand, 
    InvalidPendingOperator, 
    InvalidTableOperand, 
    ReferenceRequired, 
    SeeErrorMessage, 
    StackOverflow, 
    StackUnderflow,
    OperandDataTypeMismatch,
    OperandRequired,
    UnimplementedStatistic,
    UnimplementedTransformation,
    Unspecified, 
    
    NoError,
    
    LAST_ErrorCode;
}
