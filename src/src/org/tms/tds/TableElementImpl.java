package org.tms.tds;

import org.tms.api.ElementType;
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
    
    abstract protected boolean isDataTypeEnforced();
    
    private boolean m_enforceDataType;

    protected TableElementImpl(ElementType eType, TableElementImpl e)
    {
        super(eType);
        
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
        m_enforceDataType = false;
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
    
    protected boolean isEnforceDataType()
    {
        return m_enforceDataType;
    }

    protected void setEnforceDataType(boolean enforceDataType)
    {
        m_enforceDataType = enforceDataType;
    }

    protected boolean isDataTypeEnforced(TableElementImpl te)
    {
        return te.isDataTypeEnforced();
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
