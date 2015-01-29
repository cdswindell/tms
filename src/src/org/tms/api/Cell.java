package org.tms.api;

import org.tms.teq.ErrorCode;

public interface Cell extends TableElement, Derivable
{
    public boolean setCellValue(Object value);
    public Object getCellValue();
    public boolean isNumericValue();
    public boolean isStringValue();
    
    public ErrorCode getErrorCode();
    public boolean isErrorValue();
    
    public Class<? extends Object> getDataType();
    
    public Row getRow();
    public Column getColumn();
}
