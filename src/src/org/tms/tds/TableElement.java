package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

abstract class TableElement extends BaseElement
{
	abstract protected Table getTable();
	abstract protected Context getContext();
    abstract protected void delete();
    abstract protected void fill(Object o);
    
    private boolean m_enforceDataType;

    protected TableElement(ElementType eType, TableElement e)
    {
        super(eType);
        
        // perform base initialization
        initialize(e);
    }

    /**
     * Perform general initializations
     * @param e
     */
    protected void initialize(TableElement e)
    {
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        m_enforceDataType = false;
    }
    
    @Override
    protected Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Table:
                return getTable();
                
            case Context:
                return getContext();
                
            case isEnforceDataType:
                return isEnforceDataType();
                                
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
                    value = Context.sf_ENFORCE_DATA_TYPE_DEFAULT;
                setEnforceDataType((boolean)value);
                break;
                
            default:
                initializedProperty = false;   
                break;
        }
        
        return initializedProperty;
    }

    protected BaseElement getInitializationSource(TableElement e)
    {
        BaseElement source = null;
        if (e != null && e != this)
            source = e;
        else if (getTable() != null && getTable() != this)
            source = getTable();
        else if (getContext() != null)
            source = getContext();
        else
            source = Context.getDefaultContext();

        return source;
    }
    
    protected boolean isEnforceDataType()
    {
        return m_enforceDataType;
    }

    protected void setEnforceDataType(boolean enforceDataType)
    {
        m_enforceDataType = enforceDataType;
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
