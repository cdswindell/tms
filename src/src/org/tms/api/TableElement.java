package org.tms.api;

import java.util.List;

public interface TableElement extends BaseElement
{
    public TableContext getTableContext();
    public Table getTable();
    
    public void delete();
    public void fill(Object o);
    public void clear();
    
    public int getNumCells();
    public Iterable<Cell> cells();
    
    public boolean isNull();
    
    public String getLabel();
    public void setLabel(String label);
    
    /**
     * Returns a {@link java.collections.List<Derivable>} of the {@code Derivable} elements
     * that this {@code TableElement} impacts when modified
     * @return a List of the Derivable elements that this element affects
     */
    public List<Derivable> getAffects();
    
    public List<Subset> getSubsets();
}
