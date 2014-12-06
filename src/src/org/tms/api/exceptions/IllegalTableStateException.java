package org.tms.api.exceptions;

public class IllegalTableStateException extends TableException
{
    private static final long serialVersionUID = 6352157576615948893L;

    public IllegalTableStateException(String msg) 
    {
        super(msg, TableErrorClass.Illegal);
    }
}
