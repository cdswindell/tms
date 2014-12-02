package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

abstract public class TableElement extends BaseElement
{
    private int m_index = -1;
    private Table m_table;

    protected TableElement(ElementType eType, Table parentTable)
    {
        super(eType);
        setTable(parentTable);
    }

    @Override
    public Object getProperty(TableProperty key)
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
                
            default:
                return super.getProperty(key);
        }
    }
    
    @Override
    public int getPropertyInt(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Index:
                return (int)getProperty(key);
                                
            default:
                return super.getPropertyInt(key);
        }
    }
    
    @Override
    public boolean getPropertyBoolean(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            default:
                return (boolean)super.getPropertyBoolean(key);
        }
    }
    
    protected boolean initializeProperty(TableProperty tp, Object value)
    {
        if (super.initializeProperty(tp, value))
            return true;
        
        boolean initializedProperty = true; // assume success
        switch (tp) {
            default:
                initializedProperty = false;   
                break;
        }
        
        return initializedProperty;
    }

    public int getIndex()
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
    
    abstract protected void reset();
}
