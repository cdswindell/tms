package org.tms.api;

import org.tms.teq.ErrorCode;

public interface Cell extends TableElement, TableCellsElement, Derivable
{
    public Object getCellValue();
    public ErrorCode getErrorCode();
    public boolean isNumericValue();
    public boolean isStringValue();
    public boolean isErrorValue();
    
    public Row getRow();
    public Column getColumn();
}
