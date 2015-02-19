package org.tms.api.exceptions;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class TableCellValidationException extends TableException
{
    private static final long serialVersionUID = -8576278678070745048L;

    public TableCellValidationException()
    {
        super(ElementType.Cell, TableProperty.CellValue, TableErrorClass.Invalid);
    }
}
