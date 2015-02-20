package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.event.TableElementEventType;
import org.tms.api.event.TableElementListener;
import org.tms.api.event.TableElementListeners;
import org.tms.api.exceptions.InvalidParentException;

/**
 * This is the abstract superclass for all table elements that contain cells, 
 * including TableImpl, RowImpl, ColumnImpl, and SubsetImpl. 
 */
abstract class TableCellsElementImpl extends TableElementImpl 
{
    abstract public int getNumCells();
    
    protected TableImpl m_table;   
    protected Set<Derivable> m_affects;
    
    private int m_pendings;
    private TableElementListeners m_listeners;

    protected TableCellsElementImpl(TableElementImpl e)
    {
        super(e);
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
    protected void initialize(TableElementImpl e)
    {
        super.initialize(e);
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        
        m_affects = new LinkedHashSet<Derivable>();
        m_listeners = new TableElementListeners(this);
        m_pendings = 0;
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
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
                if (e == this || e == null)
                    continue;               
                else if (e.getTable() == null)
                    e.setTable(this.getTable());              
                else if (e.getTable() != getTable())
                    throw new InvalidParentException(e, this);
            }
        }       
    }

    @Override
    public List<Derivable> getAffects()
    {
        int numAffects = 0;
        List<Derivable> affects = new ArrayList<Derivable>(m_affects != null ? (numAffects = m_affects.size()) : 0);
        if (numAffects > 0)
            affects.addAll(m_affects);
        
        return Collections.unmodifiableList(affects);
    }
    
    @Override
    protected void delete(boolean compress)
    {
        // handle onBeforeDelete processing
        fireEvents(this, TableElementEventType.OnBeforeDelete);
    }
    
    /*
     * Class-specific methods
     */   
    public void registerAffects(Derivable elem)
    {
        m_affects.add(elem);
    }
    
    public void deregisterAffects(Derivable elem)
    {
        m_affects.remove(elem);
    }
    
    protected void incrementPendings()
    {
        m_pendings++;
    }
    
    protected void decrementPendings()
    {
        m_pendings--;
        if (m_pendings < 0)
            m_pendings = 0;
    }
    
    @Override
    public boolean isPendings()
    {
        return m_pendings > 0;
    }

    protected void fireEvents(TableElement te, TableElementEventType evT, Object... args)
    {
        if (isValid())
            m_listeners.fireEvents(this, evT, args);
    }

    protected void fireContainerEvents(Cell cell, TableElementEventType evT, Object... args)
    {
        if (cell != null && cell.isValid() && isValid())
            m_listeners.fireCellContainerEvents(cell, evT, args);
    }

    @Override
    public boolean addListeners(TableElementEventType evT, TableElementListener... tels)
    {
        vetElement();
        TableImpl parent = null;
        if ((parent = getTable()) != null)
            parent.createEventProcessorThreadPool();

        return m_listeners.addListeners(evT, tels);
    }

    @Override
    public boolean removeListeners(TableElementEventType evT, TableElementListener... tels)
    {
        vetElement();
        return m_listeners.removeListeners(evT, tels);
    }

    @Override
    public List<TableElementListener> getListeners(TableElementEventType... evTs)
    {
        vetElement();
        return m_listeners.getListeners(evTs);
    }

    @Override
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs)
    {
        vetElement();
        return m_listeners.removeAllListeners(evTs);
    }

    @Override
    public boolean hasListeners(TableElementEventType... evTs)
    {
        return m_listeners.hasListeners(evTs);
    }
}
