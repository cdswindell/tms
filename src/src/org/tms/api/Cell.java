package org.tms.api;

public interface Cell extends TableElement
{
    public Object getCellValue();
    public boolean isNull();
    
    public Row getRow();
    public Column getColumn();
}
