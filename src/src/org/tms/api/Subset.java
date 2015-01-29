package org.tms.api;

public interface Subset extends TableElement
{
    public boolean add(TableElement... tableElements);
    public boolean remove(TableElement... tableElements);
    public boolean contains(TableElement tableElement);
}
