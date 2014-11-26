package org.tms.tds;

import java.util.HashMap;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidPropertyException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnimplementedException;

public class BaseElement 
{
    protected static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    
    private ElementType m_tableElementType;
    private HashMap<String, Object> m_elemProperties;

    protected BaseElement(ElementType eType)
    {
        setTableElementType(eType);
    }
    
    public ElementType getTableElementType()
    {
        return m_tableElementType;
    }

    protected void setTableElementType(ElementType tableElementType)
    {
        m_tableElementType = tableElementType;
    }

    private synchronized HashMap<String, Object> getElemProperties()
    {
        return getElemProperties(false);
    }
    
    private synchronized HashMap<String, Object> getElemProperties(boolean createIfEmpty)
    {
        if (m_elemProperties == null && createIfEmpty)
            m_elemProperties = new HashMap<String, Object>();
        
        return m_elemProperties;
    }

    public boolean isImplements(TableProperty tp)
    {
        if (tp == null)
            return false;
        else
            return tp.isImplementedBy(this);
    }
    
    public void setProperty(TableProperty key, Object value)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        else if (key.isReadOnly())
            throw new ReadOnlyException(this, key);
        
        setProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), value, false);
    }
    
    public void setProperty(String key, Object value)
    {
        setProperty(key, value, true);
    }
    
    private void setProperty(String key, Object value, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        getElemProperties(true).put(key,  value);
    }

    public boolean clearProperty(String key)
    {
        return clearProperty(key, true);
    }
    
    public boolean clearProperty(TableProperty key)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        else if (key.isReadOnly())
            throw new ReadOnlyException(this, key);
        
        return clearProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
    }
    
    private boolean clearProperty(String key, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        HashMap<String, Object> props;      
        if ((props = getElemProperties()) != null) {
            if (props.remove(key) != null)
                return true;
        }
        
        return false;
    }
    
    public boolean hasProperty(String key)
    {
        return hasProperty(key, true);
    }
    
    public boolean hasProperty(TableProperty key)
    { 
        if (key.isImplementedBy(this)) {
            if (key.isReadOnly())
                return true;
        
            return hasProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
        }
        
        return false;
    }
    
    private boolean hasProperty(String key, boolean vetKey) 
    {
        if (vetKey) 
            key = vetKey(key);
        
        HashMap<String, Object> props;      
        if ((props = getElemProperties()) != null) {
            if (props.get(key) != null)
                return true;
        }
        
        return false;
    }
    
    public Object getProperty(TableProperty key)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        
        return getProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
    }
    
    public Object getProperty(String key)
    {
        return getProperty(key, true);
    }
        
    private Object getProperty(String key, boolean vetKey)
    {
        if (vetKey) 
            key = vetKey(key);
        
        HashMap<String, Object> props;      
        if ((props = getElemProperties()) != null)
            return props.get(key);
        else
            return null;
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
    
    private String vetKey(String key)
    {
        if (key == null || (key = key.trim()).length() == 0)
            throw new InvalidPropertyException(this);
        else if (key.startsWith(sf_RESERVED_PROPERTY_PREFIX))
            throw new InvalidPropertyException(this, key);
        
        return key;
    } 
}
