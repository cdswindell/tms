package org.tms.api;

public interface TableCellsElement extends TableElement
{
    public int getNumCells();
    public Iterable<Cell> cells();
    
    public boolean isNull();
}