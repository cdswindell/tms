package org.tms.api.exceptions;

import org.tms.api.TableProperty;
import org.tms.tds.BaseElementImpl;

public final class InvalidPropertyException extends TableException
{
    private static final long serialVersionUID = -2000599138464354586L;

    public InvalidPropertyException(BaseElementImpl te, String key)
    {
        super(te.getElementType(), 
              String.format("Invalid property: %s", key != null && key.trim().length() > 0 ? key.trim() : "<not specified>"), 
              TableErrorClass.Invalid);
    }
    
    public InvalidPropertyException(BaseElementImpl te)
    {
        super(te.getElementType(), 
              "Property not specified", 
              TableErrorClass.Invalid);
    }

    public InvalidPropertyException(BaseElementImpl te, TableProperty tp, String msg)
    {
        super(te.getElementType(), tp, TableErrorClass.Invalid, msg);
    }
}
