package org.tms.api;

import java.util.Comparator;

/**
 * Marker interface used to denote table elements that are either rows or columns
 */
public interface TableRowColumnElement
{
    public int getIndex();
    
    public void sort();
    public void sort(Comparator<Cell> sorter);
}
