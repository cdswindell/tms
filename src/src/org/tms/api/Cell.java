package org.tms.api;

public interface Cell extends TableElement, TableCellsElement
{
    public Object getCellValue();
    public boolean isNumericValue();
    public boolean isStringValue();
    
    public Row getRow();
    public Column getColumn();
}
