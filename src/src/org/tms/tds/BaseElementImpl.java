package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tms.api.BaseElement;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidPropertyException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnimplementedException;

abstract public class BaseElementImpl implements BaseElement
{
    abstract protected boolean isNull();
    
    protected static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    
    private ElementType m_tableElementType;
    private Map<String, Object> m_elemProperties;

    private boolean m_supportsNull;
    private boolean m_readOnly;
    
    protected BaseElementImpl(ElementType eType)
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
                
            case isNull:
                return isNull();
                
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
    
    public int getPropertyInt(TableProperty key)
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
                    value = ContextImpl.sf_READ_ONLY_DEFAULT;
                setReadOnly((boolean)value);
                break;
                
            case isSupportsNull:
                if (!isValidPropertyValueBoolean(value))
                    value = ContextImpl.sf_SUPPORTS_NULL_DEFAULT;
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

    public boolean isReadOnly()
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
    
    /**
     * Helper class to provide Iterable functionality to all list-based
     * elements in the TDS framework. Importantly, BaseElementIterable
     * creates a copy of the list to iterate. This isolates the source
     * list from changes, albeit at the cost of the memory required to
     * maintain a separate copy of the source list.
     *
     * @param <E>
     */
    protected class BaseElementIterable<E extends BaseElement> implements Iterator<E>, Iterable<E>
    {
        private Iterator<E> m_iter;
        
        @SuppressWarnings("unchecked")
        public BaseElementIterable(Collection<? extends BaseElement> elems)
        {
            if (elems != null) {
                List<BaseElement> copy = (List<BaseElement>) new ArrayList<E>(elems.size());
                copy.addAll(elems);
                m_iter = (Iterator<E>) (copy).iterator();
            }
            else
                m_iter = null;
        }

        @Override
        public boolean hasNext()
        {
           if (m_iter != null)
               return m_iter.hasNext();
           else
               return false;
        }

        @Override
        public E next()
        {
            if (m_iter != null)
                return m_iter.next();
            
            return null;
        }

        @Override
        public Iterator<E> iterator()
        {
            return this;
        }        
    }
    
    protected class BaseElementIterableInternal<E extends BaseElement> implements Iterator<E>, Iterable<E>
    {
        private Iterator<E> m_iter;
        
        @SuppressWarnings("unchecked")
        public BaseElementIterableInternal(Collection<? extends BaseElement> elems)
        {
            if (elems != null) 
                m_iter = (Iterator<E>) elems.iterator();
            else
                m_iter = null;
        }

        @Override
        public boolean hasNext()
        {
           if (m_iter != null)
               return m_iter.hasNext();
           else
               return false;
        }

        @Override
        public E next()
        {
            if (m_iter != null) {
                E next = m_iter.next();
                return next;
            }
            
            return null;
        }

        @Override
        public Iterator<E> iterator()
        {
            return this;
        }        
    }
}
