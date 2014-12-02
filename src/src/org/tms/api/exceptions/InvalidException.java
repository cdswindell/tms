package org.tms.api.exceptions;

import org.tms.api.ElementType;

public class InvalidException extends TableException
{
    private static final long serialVersionUID = -8126602023980678024L;

    public InvalidException(ElementType tet, String msg)
    {
        super(tet, msg, TableErrorClass.Invalid);
    }

    public InvalidException(String message)
    {
        super(message);
    }
}
