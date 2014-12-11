package org.tms.teq;

public enum TokenType
{
    NULL_TokenType,
    TableRef,
    ColumnRef,
    RowRef,
    CellRef,
    RangeRef,
    Constant,
    BuiltIn,
    Variable,
    Operand,
    String,
    GroupOp,
    StatOp,
    BinaryOp,
    BinaryFunc,
    UnaryOp,
    Comma,
    LeftParen,
    RightParen,
    NullOpValue,
    GenericUnaryOp,
    GenericBinaryFunc,
    GenericOp,
    GenericBinaryOp,
    LAST_TokenType
}
