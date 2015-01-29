package org.tms.tds;

import java.util.Set;

import org.tms.api.Derivable;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;

abstract class TableElementImpl extends BaseElementImpl implements TableElement
{
	abstract public Table getTable();
	abstract public TableContext getTableContext();
    abstract public void delete();
    abstract public void fill(Object o);
    
    abstract protected void registerAffects(Derivable d);
    abstract protected void deregisterAffects(Derivable d);
    
    abstract protected boolean isDataTypeEnforced();
    abstract protected boolean isEnforceDataType();
    abstract protected void setEnforceDataType(boolean enforceDataType); 
    
    abstract protected boolean isNullsSupported();
    abstract protected boolean isWriteProtected();
    
    abstract protected boolean add(RangeImpl rangeImpl);
    abstract protected boolean remove(RangeImpl range);
    abstract protected Set<RangeImpl> getRangesInternal();
    
    protected TableElementImpl(TableElementImpl e)
    {
        super();
        
        // perform base initialization
        initialize(e);
    }

    /**
     * Perform general initializations
     * @param e
     */
    protected void initialize(TableElementImpl e)
    {
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        setEnforceDataType(false);
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Table:
                return getTable();
                
            case Context:
                return getTableContext();
                
            case isEnforceDataType:
                return isEnforceDataType();
                                
            case Affects:
                return getAffects();
                                
            default:
                return super.getProperty(key);
        }
    }
    
    @Override
    protected boolean initializeProperty(TableProperty tp, Object value)
    {
        if (super.initializeProperty(tp, value))
            return true;
        
        boolean initializedProperty = true; // assume success
        switch (tp) {
            case isEnforceDataType:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_ENFORCE_DATA_TYPE_DEFAULT;
                setEnforceDataType((boolean)value);
                break;
                
            case isSupportsNull:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_SUPPORTS_NULL_DEFAULT;
                setSupportsNull((boolean)value);
                break;
                
            default:
                initializedProperty = false;   
                break;
        }
        
        return initializedProperty;
    }

    protected BaseElementImpl getInitializationSource(TableElementImpl e)
    {
        BaseElementImpl source = null;
        if (e != null && e != this)
            source = e;
        else if (getTable() != null && getTable() != this)
            source = (BaseElementImpl) getTable();
        else if (getTableContext() != null)
            source = (BaseElementImpl) getTableContext();
        else
            source = ContextImpl.getDefaultContext();

        return source;
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
