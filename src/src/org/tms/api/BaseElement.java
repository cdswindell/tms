package org.tms.api;

public interface BaseElement 
{
	public ElementType getElementType();
	
    public boolean isReadOnly();
    public boolean isSupportsNull();
	
    public boolean hasProperty(TableProperty p);
    public Object getProperty(String p);
    public Object getProperty(TableProperty p);
    public int getPropertyInt(TableProperty p);
    public boolean getPropertyBoolean(TableProperty p);
}
