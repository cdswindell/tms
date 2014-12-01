package org.tms.api.exceptions;

import org.tms.tds.BaseElement;

public final class InvalidPropertyException extends TableException
{
    private static final long serialVersionUID = -2000599138464354586L;

    public InvalidPropertyException(BaseElement te, String key)
    {
        super(te.getElementType(), 
              String.format("Invalid property: %s", key != null && key.trim().length() > 0 ? key.trim() : "<not specified>"), 
              TableErrorClass.Invalid);
    }
    
    public InvalidPropertyException(BaseElement te)
    {
        super(te.getElementType(), 
              "Property not specified", 
              TableErrorClass.Invalid);
    }
}
