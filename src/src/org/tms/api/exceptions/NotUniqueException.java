package org.tms.api.exceptions;

import org.tms.api.BaseElement;
import org.tms.api.TableProperty;

public class NotUniqueException extends TableException
{
    private static final long serialVersionUID = -6687604320091805056L;

    public NotUniqueException(BaseElement be, TableProperty tp, String value)
    {
        super(be.getElementType(), tp, TableErrorClass.NotUnique, value);
    }
}
