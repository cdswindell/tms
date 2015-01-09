package org.tms.api;

public interface Column extends TableElement, Derivable
{
    public String getDerivation();
    public void setDerivation(String expression);
    public boolean isDerived();
}
