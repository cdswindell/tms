package org.tms.api;


public interface TableElement extends BaseElement
{
    public TableContext getTableContext();
    public Table getTable();
    public void delete();
    public void fill(Object o);
    public void clear();
    
    public Object getProperty(TableProperty p);
    public int getPropertyInt(TableProperty p);

}
