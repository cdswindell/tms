package org.tms.api;

import java.util.List;

public interface TableCellsElement extends TableElement
{
    public List<Range> getRanges();
    public Iterable<Range> ranges();
    
    public int getNumCells();
    public Iterable<Cell> cells();
    
    public boolean isNull();
}