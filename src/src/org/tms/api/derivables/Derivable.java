package org.tms.api.derivables;

import java.util.List;

import org.tms.api.BaseElement;
import org.tms.api.TableElement;
import org.tms.teq.Derivation;

public interface Derivable extends BaseElement, TableElement
{    
    public Derivation getDerivation();
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
