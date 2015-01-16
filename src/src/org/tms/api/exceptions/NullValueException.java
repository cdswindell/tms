package org.tms.api.exceptions;

import org.tms.api.BaseElement;
import org.tms.api.TableProperty;

public final class NullValueException extends TableException
{
    private static final long serialVersionUID = -4353695924589009485L;

    public NullValueException(BaseElement te, TableProperty tp)
    {
        super(te.getElementType(), tp, TableErrorClass.NonNullValueRequired);
    }
}
