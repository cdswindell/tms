package org.tms.tds;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

abstract public class TableElement extends BaseElement
{
    private int m_index = -1;
    private Table m_table;

    public TableElement(ElementType eType, Table parentTable)
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
                break;
        }
        
        return super.getProperty(key);
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
    
    public Set<TableProperty> getInitializableProperties()
    {
        Set<TableProperty> initializableProperties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isInitializable())
                initializableProperties.add(tp);
        }
        
        return initializableProperties;
    }
    
    abstract protected void reset();
}
