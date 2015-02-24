package org.tms.api;

import java.util.List;

import org.tms.api.derivables.Derivable;
import org.tms.api.events.Listenable;

public interface TableElement extends BaseElement, Listenable
{
    public TableContext getTableContext();
    public Table getTable();
    
    public void delete();
    public boolean fill(Object o);
    public boolean clear();
    
    public int getNumCells();
    public Iterable<Cell> cells();
    
    public boolean isNull();
    public boolean isInvalid();
    public boolean isValid();
    public boolean isPendings();
    
    public String getLabel();
    public void setLabel(String label);
    public boolean isLabelIndexed();
    
    public int getNumSubsets();
    /**
     * Returns a {@link java.collections.List<Derivable>} of the {@code Derivable} elements
     * that this {@code TableElement} impacts when modified
     * @return a List of the Derivable elements that this element affects
     */
    public List<Derivable> getAffects();
       
    public List<Derivable> getDerivedElements();
    
    public List<Subset> getSubsets();
}
