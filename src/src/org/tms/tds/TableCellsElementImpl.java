package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableCellsElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidParentException;

abstract class TableCellsElementImpl extends TableElementImpl implements TableCellsElement
{
    abstract public int getNumCells();
    
    private int m_index = -1;
    private TableImpl m_table;

    protected TableCellsElementImpl(ElementType eType, TableElementImpl e)
    {
        super(eType, e);
        if (e != null)
            setTable((TableImpl)e.getTable());
        
        // perform base initialization
        initialize(e);
    }

    /*
     * Field getters and setters
     */
    
    public TableImpl getTable()
    {
    	return m_table;
    }
    
    void setTable(TableImpl t)
    {
    	m_table = t;
    }
    
    /**
     * Perform general initializations
     * @param e
     */
    protected void initialize(TableCellsElement e)
    {
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        setIndex(-1);
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Index:
                return getIndex();
                
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
        else if (getTable() != null && getTable() != (TableElementImpl)this)
            source = getTable();
        else if (getTableContext() != null)
            source = getTableContext();
        else
            source = ContextImpl.getDefaultContext();

        return source;
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
    public ContextImpl getTableContext()
    {
        return getTable() != null ? getTable().getTableContext() : null;
    }
    
    /**
     * Makes sure the specified object has the same parent table as this object
     * @param e
     * @throws InvalidParentException if the specified element belongs to a different Table
     */
    void vetParent(TableCellsElementImpl... elems)
    {
        if (elems != null) {
            for (TableCellsElementImpl e : elems) {
                if (e == this)
                    continue;               
                else if (e.getTable() == null)
                    e.setTable(this.getTable());              
                else if (e.getTable() != getTable())
                    throw new InvalidParentException(e.getElementType(), this.getElementType());
            }
        }       
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
