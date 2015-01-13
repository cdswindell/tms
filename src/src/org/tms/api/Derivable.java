package org.tms.api;

import java.util.List;

public interface Derivable
{
    public Table getTable();
    public ElementType getElementType();
    public boolean isReadOnly();
    
    public String getDerivation();
    public void setDerivation(String expression);
    public boolean isDerived();
    public List<Derivable> getAffectedBy();

}
