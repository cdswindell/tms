package org.tms.io.options;

public interface PageableOption
{
    public boolean isPaged();
    public boolean isPageNumbers();
    public int getPageWidth();
    public int getPageHeight();
    public boolean isStickyRowNames();
    public boolean isStickyColumnNames();
}
