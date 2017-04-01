package org.tms.api.derivables;

import java.util.List;

import org.tms.api.TableElement;

public interface Derivable extends TableElement
{    
    /**
     * Return the element's derivation, as a {@link Derivation}, if one is defined.
     * @return the element's derivation
     */
    public Derivation getDerivation();
    
    /**
     * Set the derivation assigned to this element, using the supplied expression.
     * @param expression the element's derivation, as an algebraic string
     * @return the new derivation
     */
    public Derivation setDerivation(String expression);
    
    public Derivation setDerivation(Object ... terms);

    /**
     * Clear the derivation assigned to this element, if one is defined.
     */
    public void clearDerivation();
    
    /**
     * Returns {@code true} if this element is derived
     * @return {@code true} if this element is derived
     */
    public boolean isDerived();
    
    /**
     * Returns a {@link List} of {@code TableElement} 
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
