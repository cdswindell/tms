package org.tms.api;

public interface BaseElement 
{
	public ElementType getElementType();
	
    public boolean isReadOnly();
    public boolean isSupportsNull();
	
    public boolean hasProperty(TableProperty p);
    public boolean hasProperty(String key);
    
    public Object getProperty(TableProperty p);
    public Object getProperty(String p);
    
    public Integer getPropertyInt(TableProperty p);
    public Long getPropertyLong(TableProperty p);
    public Double getPropertyDouble(TableProperty p);
    public String getPropertyString(TableProperty p);
    public Boolean getPropertyBoolean(TableProperty p);
}
