package org.tms.api;

/**
 * This interface is the base from which all other TMS Table System interfaces extend. It contains methods and behavior common
 * to all table elements, including {@link Table}s, {@link Row}s, {@link Column}s, {@link Cell}s, and {@link Subset}s.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface BaseElement 
{
	/**
	 * Return the {@link ElementType} of this table element.
	 * @return the ElementType of this table element
	 */
	public ElementType getElementType();
	
	/**
	 * Returns {@code true} if this element has been marked as Read-Only, which indicates it cannot
	 * be modified.
	 * @return true if this element has been marked as Read-Only.
	 */
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
