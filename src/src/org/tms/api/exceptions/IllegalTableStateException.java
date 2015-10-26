package org.tms.api.exceptions;

public class IllegalTableStateException extends TableException
{
    private static final long serialVersionUID = -7312870837884434589L;

    public IllegalTableStateException(String msg) 
    {
        super(msg, TableErrorClass.Illegal);
    }

    public IllegalTableStateException(Exception e) 
    {
        super(e, TableErrorClass.Illegal);
    }
}
