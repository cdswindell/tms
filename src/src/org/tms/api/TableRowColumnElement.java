package org.tms.api;

import java.util.Comparator;

public interface TableRowColumnElement
{
    /**
     * Return the 1-based ordinal index of this Row/Column.
     * @return The 1-based ordinal index of this Row/Column
     */
    public int getIndex();
    
    /**
     * Sort the table by a row/column. Null elements are sorted to the end of the row/column.
     * Numeric cells are given sort order priority. All other cells are sorted by the natural
     * order of the cell value interpreted as a String.
     */
    public void sort();
    
    /**
     * Sort the table by a row/column. Null elements are sorted to the end of the row/column.
     * Numeric cells are given sort order priority. All other cells are sorted by the natural
     * order of the cell value interpreted as a String.
     * 
     * @param cellSorter An instance of {@link Comparator<Cell>} that is used to sort non-null row/column cells.
     */
    public void sort(Comparator<Cell> cellSorter);
}
