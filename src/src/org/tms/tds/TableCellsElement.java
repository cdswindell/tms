package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidParentException;

abstract class TableCellsElement extends TableElement
{
    abstract protected int getNumCells();
    
    private int m_index = -1;
    private Table m_table;

    protected TableCellsElement(ElementType eType, TableElement e)
    {
        super(eType, e);
        if (e != null)
            setTable(e.getTable());
        
        // perform base initialization
        initialize(e);
    }

    /*
     * Field getters and setters
     */
    
    protected Table getTable()
    {
    	return m_table;
    }
    
    void setTable(Table t)
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
    protected Object getProperty(TableProperty key)
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

    protected BaseElement getInitializationSource(TableElement e)
    {
        BaseElement source = null;
        if (e != null && e != this)
            source = e;
        else if (getTable() != null && getTable() != (TableElement)this)
            source = getTable();
        else if (getContext() != null)
            source = getContext();
        else
            source = Context.getDefaultContext();

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
    protected Context getContext()
    {
        return getTable() != null ? getTable().getContext() : null;
    }
    
    /**
     * Makes sure the specified object has the same parent table as this object
     * @param e
     * @throws InvalidParentException if the specified element belongs to a different Table
     */
    void vetParent(TableCellsElement... elems)
    {
        if (elems != null) {
            for (TableCellsElement e : elems) {
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
