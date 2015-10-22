package org.tms.api;

import java.util.List;

import org.tms.api.derivables.Derivable;
import org.tms.api.events.Listenable;
import org.tms.api.utils.Taggable;

public interface TableElement extends BaseElement, Listenable, Taggable
{
    /**
     * Returns the parent {@link TableContext}.
     * @return the parent TableContext
     */
    public TableContext getTableContext();

    /**
     * Returns the parent {@link Table}.
     * @return the parent Table
     */
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
    
    public String getDescription();
    public void setDescription(String description);
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList} of {@link Subset}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table subsets
     */
    public List<Subset> getSubsets();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList} of {@link Subset}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table subsets
     */
    public Iterable<Subset> subsets();
       
    /** 
     * Return the number of {@link Subset}s that this element is contained within.
     * @return the number of Subsets that this element is contained within
     */
    public int getNumSubsets();
    
    /**
     * Returns a {@link List} of the {@code Derivable} elements
     * that this {@code TableElement} impacts when modified
     * @return a List of the Derivable elements that this element affects
     */
    public List<Derivable> getAffects();
       
    public List<Derivable> getDerivedElements();
}
