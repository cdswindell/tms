package org.tms.api.exceptions;

public enum TableErrorClass
{
    ReadOnly,
    NonNullValueRequired,
    Unimplemented,
    Invalid,
    Required,
    ConstraintViolation,
    NotUnique,
    Deleted,
    Illegal,
    UnsupportedImplementation,
    IO,
    Unknown;
}
