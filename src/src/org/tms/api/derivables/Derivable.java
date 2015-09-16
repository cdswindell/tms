package org.tms.api.derivables;

import java.util.List;

import org.tms.api.TableElement;

public interface Derivable extends TableElement
{    
    /**
     * Return the element's derivation, as a {@link String}, if one is defined.
     * @return the element's derivation
     */
    public Derivation getDerivation();
    
    /**
     * Get the derivation assigned to this element, if one exists.
     * @param expression
     * @return
     */
    public Derivable setDerivation(String expression);
    public Derivable clearDerivation();
    
    /**
     * Returns {@code true} if this element is derived
     * @return {@code true} if this element is derived
     */
    public boolean isDerived();
    
    /**
     * Returns a {@link List} of the {@code Derivable} elements
     * that affect this {@code Derivable}'s calculation
     * @return a List of the TableElement elements that affect this derivation
     */
    public List<TableElement> getAffectedBy();
   
    
    /**
     * Recalculates the derived element. Once complete, recalculates all dependent elements
     * and their dependencies, etc.
     */
    public void recalculate();
}
