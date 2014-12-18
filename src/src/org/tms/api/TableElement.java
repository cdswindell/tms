package org.tms.api;


public interface TableElement extends BaseElement
{
    public TableContext getTableContext();
    public Table getTable();
    public void delete();
    public void fill(Object o);

}
