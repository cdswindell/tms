package org.tms.api.exceptions;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.tds.BaseElementImpl;

public final class UnimplementedException extends TableException
{
    private static final long serialVersionUID = 4174806292387968676L;

    public UnimplementedException(BaseElementImpl te, TableProperty tp)
    {
        super(te.getElementType(), tp, TableErrorClass.Unimplemented);
    }

    public UnimplementedException(BaseElementImpl te, TableProperty tp, String msg)
    {
        super(te.getElementType(), tp, TableErrorClass.Unimplemented, msg);
    }

    public UnimplementedException(BaseElementImpl te, String msg)
    {
        super(te.getElementType(), msg, TableErrorClass.Unimplemented);
    }

    public UnimplementedException(ElementType et, String msg)
    {
        super(et, msg, TableErrorClass.Unimplemented);
    }
}
