package org.tms.api.io.options;

public interface PageableIOOption
{
    public boolean isPaged();
    public boolean isPageNumbers();
    public int getPageWidth();
    public int getPageHeight();
    public boolean isStickyRowNames();
    public boolean isStickyColumnNames();
}
