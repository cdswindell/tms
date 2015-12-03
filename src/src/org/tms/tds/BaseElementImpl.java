package org.tms.tds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.BaseElement;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.Taggable;
import org.tms.api.exceptions.DeletedElementException;
import org.tms.api.exceptions.InvalidPropertyException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnimplementedException;

abstract public class BaseElementImpl implements BaseElement
{
    abstract protected boolean isNull();
    abstract protected Map<String, Object> getElemProperties(boolean createIfEmpty);
    abstract protected void resetElemProperties();
    
    public abstract ElementType getElementType();
    
    protected static final String sf_RESERVED_PROPERTY_PREFIX = "~~~";
    
    static final protected int sf_ENFORCE_DATATYPE_FLAG             = 0x01;
    static final protected int sf_READONLY_FLAG                     = 0x02;
    static final protected int sf_SUPPORTS_NULL_FLAG                = 0x04;
    static final protected int sf_AUTO_RECALCULATE_FLAG             = 0x08;
    
    static final protected int sf_AUTO_RECALCULATE_DISABLED_FLAG    = 0x10;
    static final protected int sf_STRONGLY_TYPED_FLAG               = 0x20;
    static final protected int sf_IN_USE_FLAG                       = 0x40;
    static final protected int sf_IS_PENDING_FLAG                   = 0x80;
    
    static final protected int sf_ROW_LABELS_INDEXED_FLAG           = 0x100;
    static final protected int sf_COLUMN_LABELS_INDEXED_FLAG        = 0x200;
    static final protected int sf_CELL_LABELS_INDEXED_FLAG          = 0x400;
    static final protected int sf_TABLE_LABELS_INDEXED_FLAG         = 0x800;
    
    static final protected int sf_SUBSET_LABELS_INDEXED_FLAG        = 0x1000;
    static final protected int sf_HAS_CELL_VALIDATOR_FLAG           = 0x2000;
    static final protected int sf_IS_DERIVED_CELL_FLAG              = 0x4000;
    static final protected int sf_IS_TABLE_PERSISTANT_FLAG          = 0x8000;
    
    static final protected int sf_EVENTS_NOTIFY_IN_SAME_THREAD_FLAG         = 0x100000;
    static final protected int sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_FLAG     = 0x200000;
    static final protected int sf_PENDINGS_ALLOW_CORE_THREAD_TIMEOUT_FLAG   = 0x400000;
    
    static final protected int sf_IS_DEFAULT_FLAG                   = 0x1000000;
    static final protected int sf_IS_DIRTY_FLAG                     = 0x2000000;
    static final protected int sf_HAS_CELL_ERROR_MSG_FLAG           = 0x4000000;
    
    static final protected int sf_IS_INVALID_FLAG                   = 0x10000000;
    
    // to save a little space, all flags are maintained in a 
    // single 32 bit integer
    protected int m_flags;

    protected BaseElementImpl()
    {
        m_flags = 0;
    }
    
    private synchronized Map<String, Object> getElemProperties()
    {
        return getElemProperties(false);
    }
    
    protected boolean isImplements(TableProperty tp)
    {
        if (tp == null)
            return false;
        else
            return tp.isImplementedBy(this);
    }
    
    protected void invalidate()
    {
        m_flags |= sf_IS_INVALID_FLAG;        
        resetElemProperties();
    }
    
    protected void vetElement()
    {
        vetElement(this);
    }    
    
    protected void vetElement(BaseElementImpl be)
    {
        if (be != null && be.isInvalid())
            throw new DeletedElementException(be);
    }
    
    /**
     * Returns true if the element has been deleted, either explicitly or because 
     * a parent element has been deleted
     * @return true if the element has been deleted
     */
    public boolean isInvalid()
    {
        return isSet(sf_IS_INVALID_FLAG);
    }
    
    public boolean isValid()
    {
        return !isInvalid();
    }
    
    protected boolean isSet(int flag)
    {
        return (m_flags & flag) != 0;
    }
    
    protected void unSet(int flag)
    {
        set(flag, false);
    }
    
    protected void set(int flag, boolean state)
    {
        if (state)
            m_flags |= flag;
        else
            m_flags &= ~flag;
    }
    
    public boolean isSupportsNull()
    {
        return isSet(sf_SUPPORTS_NULL_FLAG);
    }

    public void setSupportsNull(boolean supportsNulls)
    {
        set(sf_SUPPORTS_NULL_FLAG, supportsNulls);
    }
    
    @Override
    public boolean isReadOnly()
    {
        return isSet(sf_READONLY_FLAG);
    }

    public void setReadOnly(boolean supportsNulls)
    {
        set(sf_READONLY_FLAG, supportsNulls);
    }
    
    @Override
    public boolean isEnforceDataType()
    {
        return isSet(sf_ENFORCE_DATATYPE_FLAG);
    }
    
    @Override
    public void setEnforceDataType(boolean dataTypeEnforced)
    {
        set(sf_ENFORCE_DATATYPE_FLAG, dataTypeEnforced);
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
    
    public boolean hasProperty(TableProperty key)
    { 
        if (key.isImplementedBy(this)) {
            if (key.isNonOptional())
                return true;
            
            if (getProperty(key) != null)
                return true;
            else
                return hasProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
        }
        
        return false;
    }
    
    public boolean hasProperty(String key)
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
    
    public Object getProperty(TableProperty key)
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
                
            case isEnforceDataType:
                return isEnforceDataType();
                                
            default:
                if (key.isOptional())
                    return getProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
                else
                    throw new UnimplementedException(this, key);
        }      
    }
    
    protected Object getPropertyInternal(TableProperty key)
    {
        if (!key.isImplementedBy(this))
            throw new UnimplementedException(this, key);
        
        if (key.isOptional())
            return getProperty(sf_RESERVED_PROPERTY_PREFIX + key.name(), false);
        else
            throw new UnimplementedException(this, key);
    }
        
    public Object getProperty(String key)
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
    
    public String getPropertyString(TableProperty key)
    {
        if (key.isStringValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof String)
                return (String)value;
            else if (key.isOptional())
                return null;
            else
                throw new UnimplementedException(this, key, "String");
        }
        else
            throw new InvalidPropertyException(this, key, "not int value");
    }
    
    public Boolean getPropertyBoolean(TableProperty key)
    {
        if (key.isBooleanValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Boolean)
                return (boolean)value;
            else if (key.isOptional())
                return null;
            else
                throw new UnimplementedException(this, key, "boolean");
        }
        else
            throw new InvalidPropertyException(this, key, "not boolean value");
    }
    
    public Integer getPropertyInt(TableProperty key)
    {
        if (key.isIntValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Integer)
                return (int)value;
            else if (key.isOptional())
                return null;
            else
                throw new UnimplementedException(this, key, "int");
        }
        else
            throw new InvalidPropertyException(this, key, "not int value");
    }
    
    public Long getPropertyLong(TableProperty key)
    {
        if (key.isLongValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Long)
                return (long)value;
            else if (key.isOptional())
                return null;
            else
                throw new UnimplementedException(this, key, "long");
        }
        else
            throw new InvalidPropertyException(this, key, "not long value");
    }
    
    public Double getPropertyDouble(TableProperty key)
    {
        if (key.isDoubleValue()) {
            Object value = getProperty(key);
            if (value != null && value instanceof Double)
                return (double)value;
            else if (key.isOptional())
                return null;
            else
                throw new UnimplementedException(this, key, "double");
        }
        else
            throw new InvalidPropertyException(this, key, "not double value");
    }
    
    protected BaseElement find(Collection<? extends BaseElement> slices, TableProperty key, Object value)
    {
        assert key != null : "TableProperty required (enum)";
        assert value != null : "Value required";
        
        if (slices != null && value != null) {
        	if (key == TableProperty.Tags)
        		return findTaggedElement(slices, value);
            for (BaseElement tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value)) {
                        if (tes instanceof TableSliceElementImpl)
                            ((TableSliceElementImpl)tes).setCurrent();
                        return tes;
                    }
                }
            }
        }
        
        return null;
    }

    private BaseElement findTaggedElement(Collection<? extends BaseElement> slices, Object tagObjs) 
    {
    	if (this instanceof TableCellsElementImpl) {
    		String [] tagStrs = harvestTagStrs(tagObjs);
    		if (tagStrs != null && tagStrs.length > 0) {
	    		ContextImpl tc = ((TableCellsElementImpl)this).getTableContext();
		    	Set<Tag> tags = Tag.encodeTags(tagStrs, tc, false);
		    	if (tags != null && tags.size() == tagStrs.length) {		    	
			        for (BaseElement tes : slices) {
			            if (tes != null && tes instanceof Taggable) {
			            	Taggable te = (Taggable)tes;
			            	if (te.isTagged(tagStrs))
			            		return tes;
			            }
			        }
		    	}
    		}
    	}
        
        return null;
	}
    
	private String[] harvestTagStrs(Object tagObjs) 
	{
		if (tagObjs instanceof String)
			return new String [] { (String)tagObjs };
		else if (tagObjs instanceof Object[]) {
			Object [] objs = (Object[])tagObjs;
			List<String> strs = new ArrayList<String>(objs.length);
			
			for (Object o : objs) {
				if (o instanceof String)
					strs.add((String)o);
			}
			
			return strs.toArray(new String [] {});
		}
		else
			return null;
	}
	
	protected BaseElement find(Collection<? extends BaseElement> slices, String key, Object value)
    {
        assert key != null : "TableProperty required (String)";
        assert value != null : "Value required";
        
        if (slices != null && value != null) {
            for (BaseElement tes : slices) {
                if (tes != null) {
                    Object p = tes.getProperty(key);
                    if (p != null && p.equals(value)) {
                        if (tes instanceof TableSliceElementImpl)
                            ((TableSliceElementImpl)tes).setCurrent();
                        return tes;
                    }
                }
            }
        }
        
        return null;
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
                
            case isEnforceDataType:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_ENFORCE_DATA_TYPE_DEFAULT;
                setEnforceDataType((boolean)value);
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
        return value != null && value instanceof Integer && ((int)value) >= 0;    
    }
    
    protected boolean isValidPropertyValueDouble(Object value)
    {
        return value != null && value instanceof Double && ((double)value) >= 0;    
    }
    
    protected boolean isValidPropertyValueLong(Object value)
    {
        return value != null && value instanceof Long && ((long)value) >= 0;    
    }
    
    protected boolean isValidPropertyValueBoolean(Object value)
    {
        return value != null && value instanceof Boolean;
    }
    
    protected boolean isValidPropertyValueString(Object value)
    {
        return value == null || value instanceof String;
    }
    
    public String getLabel()
    {
        return (String)getProperty(TableProperty.Label);
    }

    public void setLabel(String label)
    {
        if (label == null || (label = label.trim()).length() == 0)
            clearProperty(TableProperty.Label);
        else
            setProperty(TableProperty.Label, label);
    }

    public String getDescription()
    {
        return (String)getProperty(TableProperty.Description);
    }

    public void setDescription(String description)
    {
        setProperty(TableProperty.Description, 
                (description != null && (description = description.trim()).length() > 0 ? description : null));
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
                m_iter = (Iterator<E>) Collections.unmodifiableList(copy).iterator();
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
