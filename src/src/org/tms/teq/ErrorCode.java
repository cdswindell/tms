package org.tms.teq;

public enum ErrorCode
{
    DivideByZero, 
    NaN,
    Infinity,
    InvalidOperand, 
    InvalidTableOperand, 
    ReferenceRequired, 
    SeeErrorMessage, 
    StackOverflow, 
    StackUnderflow,
    OperandDataTypeMismatch,
    OperandRequired,
    UnimplementedStatistic,
    Unspecified, 
    
    NoError,
    
    LAST_ErrorCode;
}
