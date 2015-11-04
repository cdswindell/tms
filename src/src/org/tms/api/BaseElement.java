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

    public void setReadOnly(boolean enabled);
    
    /**
     * Returns {@code true} if the {@link Cell} values in this element can be set to {@code null}.
     * @return {@code true} if the cell values in this element can be set to {@code null}
     */
    public boolean isSupportsNull();
	
    public void setSupportsNull(boolean enabled);
    
    /**
     * Return {@code true} if this element supports (has) the specified {@link TableProperty}
     * @param key the TableProperty to check for the existence of
     * @return true of this element supports (has) the specified TableProperty
     */
    public boolean hasProperty(TableProperty key);
    
    /**
     * Return {@code true} if this element supports (has) the property named {@code key}
     * @param key the name of the property to check for the existence of
     * @return true of this element supports (has) the specified named string
     */
    public boolean hasProperty(String key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the element property or null if it does not exist
     */
    public Object getProperty(TableProperty key);
    
    /**
     * Searches for the property with the specified string key in this element's properties map
     * and returns it or {@code null} if the key is not found.
     * @param key the name of the property to check for the existence of
     * @return the element property or null if it does not exist
     */
    public Object getProperty(String key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it as an Integer value or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the Integer element property or null if it does not exist
     */
    public Integer getPropertyInt(TableProperty key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it as a Long value or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the Long element property or null if it does not exist
     */
    public Long getPropertyLong(TableProperty key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it as a Double value or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the Double element property or null if it does not exist
     */
    public Double getPropertyDouble(TableProperty key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it as a String value or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the String element property or null if it does not exist
     */
    public String getPropertyString(TableProperty key);
    
    /**
     * Searches for the property with the specified {@link TableProperty} key in this element's properties map
     * and returns it as a Boolean value or {@code null} if the key is not found.
     * @param key the TableProperty key
     * @return the Boolean element property or null if it does not exist
     */
    public Boolean getPropertyBoolean(TableProperty key);
}
