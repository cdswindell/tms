package org.tms.api.derivables;

import org.tms.api.exceptions.TableException;

public class InvalidExpressionException extends TableException
{
    private static final long serialVersionUID = -147462128920016561L;

    public InvalidExpressionException(String message)
    {
        super(message);
    }
}
