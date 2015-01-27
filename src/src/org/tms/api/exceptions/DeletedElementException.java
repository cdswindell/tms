package org.tms.api.exceptions;

import org.tms.api.BaseElement;
import org.tms.api.ElementType;

public class DeletedElementException extends TableException
{
    private static final long serialVersionUID = -5928192195056401129L;

    public DeletedElementException(ElementType tet, String msg)
    {
        super(tet, msg, TableErrorClass.Deleted);
    }

    public DeletedElementException(BaseElement be)
    {
        super(be.getElementType(), "Operations on deleted elements are not allowed", TableErrorClass.Deleted);
    }

    public DeletedElementException(BaseElement be, String msg)
    {
        super(be.getElementType(), msg, TableErrorClass.Deleted);
    }

    public DeletedElementException(String message)
    {
        super(message);
    }
}
