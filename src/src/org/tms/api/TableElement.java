package org.tms.api;

import java.util.List;


public interface TableElement extends BaseElement
{
    public TableContext getTableContext();
    public Table getTable();
    public void delete();
    public void fill(Object o);
    public void clear();
    
    public Object getProperty(String p);
    public Object getProperty(TableProperty p);
    public int getPropertyInt(TableProperty p);
    
    /**
     * Returns a {@link java.collections.List<Derivable>} of the {@code Derivable} elements
     * that this {@code TableElement} impacts when modified
     * @return a List of the Derivable elements that this element affects
     */
    public List<Derivable> getAffects();
}
