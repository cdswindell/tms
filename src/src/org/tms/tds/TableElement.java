package org.tms.tds;

import java.util.HashMap;

import org.tms.api.TableElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.TableException;

public class TableElement 
{
    private static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    
    private TableElementType m_tableElementType;
    private HashMap<String, Object> m_elemProperties;
    
    protected TableElement(TableElementType eType)
    {
        setTableElementType(eType);
    }
    
    public TableElementType getTableElementType()
    {
        return m_tableElementType;
    }

    protected void setTableElementType(TableElementType tableElementType)
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

    public void setProperty(TableProperty key, Object value)
    {
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
        return hasProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
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
            throw new TableException();
        else if (key.startsWith(sf_RESERVED_PROPERTY_PREFIX))
            throw new TableException();
        
        return key;
    }
    
}
