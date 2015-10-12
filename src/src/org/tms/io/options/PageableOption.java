package org.tms.io.options;

public interface PageableOption
{
    public boolean isPaged();
    public boolean isPageNumbers();
    public int getPageWidth();
    public int getPageHeight();
    public int getColumnWidth();
    public int getRowNameColumnWidth();
    public boolean isStickyRowNames();
    public boolean isStickyColumnNames();
}
