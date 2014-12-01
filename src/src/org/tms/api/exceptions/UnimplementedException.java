package org.tms.api.exceptions;

import org.tms.api.TableProperty;
import org.tms.tds.BaseElement;

public final class UnimplementedException extends TableException
{
    private static final long serialVersionUID = 4174806292387968676L;

    public UnimplementedException(BaseElement te, TableProperty tp)
    {
        super(te.getElementType(), tp, TableErrorClass.Unimplemented);
    }
}
