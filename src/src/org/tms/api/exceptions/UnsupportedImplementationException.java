package org.tms.api.exceptions;

import org.tms.api.BaseElement;
import org.tms.api.ElementType;

public final class UnsupportedImplementationException extends TableException
{
	private static final long serialVersionUID = 3055599052880100824L;

	public UnsupportedImplementationException(BaseElement te)
    {
        this(te.getElementType(), null);
    }

	public UnsupportedImplementationException(BaseElement te, String msg)
    {
        super(te.getElementType(), msg, TableErrorClass.UnsupportedImplementation);
    }

    public UnsupportedImplementationException(ElementType et, String msg)
    {
        super(et, msg, TableErrorClass.UnsupportedImplementation);
    }
}
