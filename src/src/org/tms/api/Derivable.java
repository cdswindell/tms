package org.tms.api;

import java.util.List;

public interface Derivable extends BaseElement, TableCellsElement
{    
    public String getDerivation();
    public void setDerivation(String expression);
    public void clearDerivation();
    
    public boolean isDerived();
    
    /**
     * Returns a {@link java.collections.List<Derivable>} of the {@code Derivable} elements
     * that affect this {@code Derivable}'s calculation
     * @return a List of the Derivable elements that affect this derivation
     */
    public List<Derivable> getAffectedBy();
    
    public List<Derivable> getAffects();
    
    /**
     * Recalculates the derived element. Once complete, recalculates all dependent elements
     * and their dependencies, etc.
     */
    public void recalculate();
}
