package org.tms.api.exceptions;

import org.tms.tds.TableElement;

public final class InvalidPropertyException extends TableException
{
    private static final long serialVersionUID = -2000599138464354586L;

    public InvalidPropertyException(TableElement te, String key)
    {
        super(te.getTableElementType(), 
              String.format("Invalid property: %s", key != null && key.trim().length() > 0 ? key.trim() : "<not specified>"), 
              TableErrorClass.Invalid);
    }
    
    public InvalidPropertyException(TableElement te)
    {
        super(te.getTableElementType(), 
              "Property not specified", 
              TableErrorClass.Invalid);
    }
}
