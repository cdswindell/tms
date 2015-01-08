package org.tms.api;

public interface Derivable
{
    public Table getTable();
    public ElementType getElementType();
    public boolean isReadOnly();
}
