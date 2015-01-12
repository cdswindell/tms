package org.tms.api;

public interface Derivable
{
    public Table getTable();
    public ElementType getElementType();
    public boolean isReadOnly();
    
    public String getDerivation();
    public void setDerivation(String expression);
    public boolean isDerived();
}
