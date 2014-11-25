package org.tms.api.exceptions;

public class TableException extends RuntimeException
{
    private static final long serialVersionUID = 2907947831287999680L;

    public TableException()
    {
        // TODO Auto-generated constructor stub
    }

    public TableException(String message)
    {
        super(message);
    }

    public TableException(Throwable cause)
    {
        super(cause);
    }

    public TableException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
