package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidParentException;

abstract class TableElement extends BaseElement
{
    abstract protected int getNumCells();
    abstract protected void delete();
    
    private int m_index = -1;
    private Table m_table;
    private boolean m_enforceDataType;

    protected TableElement(ElementType eType, TableElement e)
    {
        super(eType);
        if (e != null)
            setTable(e.getTable());
        
        // perform base initialization
        initialize(e);
    }

    /**
     * Perform general initializations
     * @param e
     */
    protected void initialize(TableElement e)
    {
        setIndex(-1);
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
    }
    
    @Override
    protected Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Index:
                return getIndex();
                
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
    
    /**
     * Makes sure the specified object has the same parent table as this object
     * @param e
     * @throws InvalidParentException if the specified element belongs to a different Table
     */
    void vetParent(TableElement... elems)
    {
        if (elems != null) {
            for (TableElement e : elems) {
                if (e == this)
                    continue;               
                else if (e.getTable() == null)
                    e.setTable(this.getTable());              
                else if (e.getTable() != getTable())
                    throw new InvalidParentException(e.getElementType(), this.getElementType());
            }
        }       
    }

    protected int getIndex()
    {
        return m_index ;
    }
    
    void setIndex(int idx)
    {
        m_index = idx;
    }
    
    /**
     * Retrieve the Context associated with this table element; the context is associated with the parent table
     * @return
     */
    protected Context getContext()
    {
        return getTable() != null ? getTable().getContext() : null;
    }
    
    protected Table getTable()
    {
        return m_table;
    }
    
    /**
     * Package-protected method only accessible from other TDS methods
     * @param t
     */
    void setTable(Table t)
    {
        m_table = t;
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
        
        int idx = getIndex();
        
        if (idx > 0)
            return String.format("[%s %d%s]", getElementType(), idx, label);
        else
            return String.format("[%s%s]", getElementType(), label);
    }
}
