package org.tms.tds;

import java.util.HashMap;

public class TableElement 
{
    private static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    private static final String sf_TABLE_ELEMENT_LABEL = sf_RESERVED_PROPERTY_PREFIX + "label";
    private static final String sf_TABLE_ELEMENT_DESC = sf_RESERVED_PROPERTY_PREFIX + "description";
    
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

    public void setProperty(String key, Object value)
    {
        setProperty(key, value, true);
    }
    
    private void setProperty(String key, Object value, boolean vetKey) 
    {
        if (vetKey) {
            
        }
        
        getElemProperties(true).put(key,  value);
    }
    
    public boolean clearProperty(String key)
    {
        return clearProperty(key, true);
    }
    
    private boolean clearProperty(String key, boolean vetKey) 
    {
        if (vetKey) {
            
        }
        
        HashMap<String, Object> props;
        
        if ((props = getElemProperties()) != null) {
            if (props.remove(key) != null)
                return true;
        }
        
        return false;
    }
    
    public String getLabel()
    {
        HashMap<String, Object> props;
        
        if ((props = getElemProperties()) != null)
            return (String)props.get(sf_TABLE_ELEMENT_LABEL);
        else
            return null;
    }

    public void setLabel(String label)
    {
        setProperty(sf_TABLE_ELEMENT_LABEL, (label != null ? label.trim() : null), false);
    }

    public String getDescription()
    {
        HashMap<String, Object> props;
        
        if ((props = getElemProperties()) != null)
            return (String)props.get(sf_TABLE_ELEMENT_DESC);
        else
            return null;
    }

    public void setDescription(String description)
    {
        setProperty(sf_TABLE_ELEMENT_DESC, (description != null ? description.trim() : null), false);
    }
}
