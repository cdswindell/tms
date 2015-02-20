package org.tms.api.exceptions;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class ConstraintViolationException extends TableException
{
    private static final long serialVersionUID = -5946832609966802813L;

    public ConstraintViolationException(String msg)
    {
        super(ElementType.Cell, TableProperty.CellValue, TableErrorClass.ConstraintViolation, msg);
    }
    
    public ConstraintViolationException()
    {
        this((String) null);
    }
}
