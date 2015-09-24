package org.tms.api;

import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;

public interface Cell extends TableElement, Derivable
{
    public boolean setCellValue(Object value);
    public Object getCellValue();
    public String getFormattedCellValue();
    
    public boolean isNumericValue();
    public boolean isStringValue();
    
    public ErrorCode getErrorCode();
    public boolean isErrorValue();
    
    public boolean isWriteProtected();
    public boolean isReadOnly();
    
    public Class<? extends Object> getDataType();
    
    public Row getRow();
    public Column getColumn();
    
    public String getUnits();
    public void setUnits(String units);

    public String getDisplayFormat();
    public void setDisplayFormat(String format);
    public boolean isFormatted();

    public TableCellValidator getValidator();
    public void setValidator(TableCellValidator validator);
    public void setTransformer(TableCellTransformer transformer);
}
