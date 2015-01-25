package org.tms.api.exceptions;

import org.tms.api.BaseElement;
import org.tms.api.ElementType;

public class InvalidParentException extends TableException
{
    private static final long serialVersionUID = -1954033064885010431L;

    public InvalidParentException(ElementType child, ElementType parent)
    {
        super(String.format("Not child's parent %s->%s", parent, child), TableErrorClass.Invalid );
    }

    public InvalidParentException(BaseElement child, BaseElement parent)
    {
        super(String.format("Not child's parent %s->%s", parent, child), TableErrorClass.Invalid );
    }
}
