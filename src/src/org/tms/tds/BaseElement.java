package org.tms.tds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidPropertyException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnimplementedException;

abstract public class BaseElement 
{
    abstract protected boolean isEmpty();
    
    protected static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    
    private ElementType m_tableElementType;
    private Map<String, Object> m_elemProperties;

    private boolean m_supportsNull;
    private boolean m_readOnly;
    
    protected BaseElement(ElementType eType)
    {
        setElementType(eType);
    }
    
    public ElementType getElementType()
    {
        return m_tableElementType;
    }

    protected void setElementType(ElementType tableElementType)
    {
        m_tableElementType = tableElementType;
    }

    private synchronized Map<String, Object> getElemProperties()
    {
        return getElemProperties(false);
    }
    
    private synchronized Map<String, Object> getElemProperties(boolean createIfEmpty)
    {
        if (m_elemProperties == null && createIfEmpty)
            m_elemProperties = new HashMap<String, Object>();
        
        return m_elemProperties;
    }

    protected boolean isImplements(TableProperty tp)
    {
        if (tp == null)
            return false;
        else
            return tp.isImplementedBy(this);
    }
    
    protected void setProperty(TableProperty key, Object value)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        else if (key.isReadOnly())
            throw new ReadOnlyException(this, key);
        
        setProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), value, false);
    }
    
    protected void setProperty(String key, Object value)
    {
        setProperty(key, value, true);
    }
    
    private void setProperty(String key, Object value, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        getElemProperties(true).put(key,  value);
    }

    protected boolean clearProperty(TableProperty key)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        else if (key.isReadOnly())
            throw new ReadOnlyException(this, key);
        
        return clearProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
    }
    
    protected boolean clearProperty(String key)
    {
        return clearProperty(key, true);
    }
    
    private boolean clearProperty(String key, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        Map<String, Object> props;      
        if ((props = getElemProperties()) != null) {
            if (props.remove(key) != null)
                return true;
        }
        
        return false;
    }
    
    protected boolean hasProperty(TableProperty key)
    { 
        if (key.isImplementedBy(this)) {
            if (key.isNonOptional())
                return true;
        
            return hasProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
        }
        
        return false;
    }
    
    protected boolean hasProperty(String key)
    {
        return hasProperty(key, true);
    }
    
    private boolean hasProperty(String key, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        Map<String, Object> props;      
        if ((props = getElemProperties()) != null) {
            if (props.get(key) != null)
                return true;
        }
        
        return false;
    }
    
    protected Object getProperty(TableProperty key)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        
        // Some properties are built into the base element object
        switch (key)
        {
            case isSupportsNull:
                return isSupportsNull();
                
            case isReadOnly:
                return isReadOnly();
                
            case isEmpty:
                return isEmpty();
                
            default:
                if (key.isOptional())
                    return getProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
                else
                    throw new UnimplementedException(this, key);
        }      
    }
    
    protected Object getProperty(String key)
    {
        return getProperty(key, true);
    }
        
    private Object getProperty(String key, boolean vetKey)
    {
        if (vetKey) 
            key = vetKey(key);
        
        Map<String, Object> props;      
        if ((props = getElemProperties()) != null)
            return props.get(key);
        else
            return null;
    }

    private String vetKey(String key)
    {
        if (key == null || (key = key.trim()).length() == 0)
            throw new InvalidPropertyException(this);
        else if (key.startsWith(sf_RESERVED_PROPERTY_PREFIX))
            throw new InvalidPropertyException(this, key);
        
        return key;
    }
    
    protected boolean getPropertyBoolean(TableProperty key)
    {
        if (key.isBooleanValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Boolean)
                return (boolean)value;
            else
                throw new UnimplementedException(this, key, "boolean");
        }
        else
            throw new InvalidPropertyException(this, key, "not boolean value");
    }
    
    protected int getPropertyInt(TableProperty key)
    {
        if (key.isIntValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Integer)
                return (int)value;
            else
                throw new UnimplementedException(this, key, "int");
        }
        else
            throw new InvalidPropertyException(this, key, "not int value");
    }
    
    /**
     * initialize properties defined in BaseElement
     * @param tp
     * @param value
     * @return
     */
    protected boolean initializeProperty(TableProperty tp, Object value)
    {
        boolean initializedProperty = true; // assume success
        switch (tp) {
            case isReadOnly:
                if (!isValidPropertyValueBoolean(value))
                    value = Context.sf_READ_ONLY_DEFAULT;
                setReadOnly((boolean)value);
                break;
                
            case isSupportsNull:
                if (!isValidPropertyValueBoolean(value))
                    value = Context.sf_SUPPORTS_NULL_DEFAULT;
                setSupportsNull((boolean)value);
                break;
                
            default:
                initializedProperty = false;   
                break;
        }
        
        return initializedProperty;
    } 
    
    List<TableProperty> getProperties()
    {
        return getElementType().getProperties();
    }
    
    List<TableProperty> getOptionalProperties()
    {
        return getElementType().getOptionalProperties();
    }
    
    List<TableProperty> getNonOptionalProperties()
    {
        return getElementType().getNonOptionalProperties();
    }
    
    List<TableProperty> getInitializableProperties()
    {
        return getElementType().getInitializableProperties();
    }
    
    List<TableProperty> getReadOnlyProperties()
    {
        return getElementType().getReadOnlyProperties();
    }
    
    protected boolean isValidPropertyValueInt(Object value)
    {
        return value != null && value instanceof Integer && ((int)value) > 0;    
    }
    
    protected boolean isValidPropertyValueBoolean(Object value)
    {
        return value != null && value instanceof Boolean;    
    }
    
    public String getLabel()
    {
        return (String)getProperty(TableProperty.Label);
    }

    public void setLabel(String label)
    {
        setProperty(TableProperty.Label, (label != null ? label.trim() : null));
    }

    public String getDescription()
    {
        return (String)getProperty(TableProperty.Description);
    }

    public void setDescription(String description)
    {
        setProperty(TableProperty.Description, (description != null ? description.trim() : null));
    }
    
    protected boolean isSupportsNull()
    {
        return m_supportsNull;
    }

    protected void setSupportsNull(boolean supportsNull)
    {
        m_supportsNull = supportsNull;
    }

    protected boolean isReadOnly()
    {
        return m_readOnly;
    }

    protected void setReadOnly(boolean readOnly)
    {
        m_readOnly = readOnly;
    }
    
    public String toString()
    {
        String label = (String)getProperty(TableProperty.Label);
        if (label != null)
            label = ": " + label;
        else
            label = "";
        return String.format("[%s%s]", getElementType(), label);
    }
}
