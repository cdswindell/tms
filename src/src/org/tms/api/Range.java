package org.tms.api;

public interface Range extends TableCellsElement
{
    public boolean add(TableCellsElement... tableElements);
    public boolean remove(TableCellsElement... tableElements);
    public boolean contains(TableCellsElement tableElement);
}
