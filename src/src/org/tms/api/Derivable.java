package org.tms.api;

public interface Derivable
{
    public Table getTable();
    public ElementType getElementType();
    public boolean isReadOnly();
    public void addToAffects(Derivable elem);
    public void removeFromAffects(Derivable elem);
}
