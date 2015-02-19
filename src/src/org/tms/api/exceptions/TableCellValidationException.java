package org.tms.api.exceptions;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class TableCellValidationException extends TableException
{
    private static final long serialVersionUID = 947266506605358733L;

    public TableCellValidationException(String msg)
    {
        super(ElementType.Cell, TableProperty.CellValue, TableErrorClass.Invalid, msg);
    }
    
    public TableCellValidationException()
    {
        this((String) null);
    }
}
