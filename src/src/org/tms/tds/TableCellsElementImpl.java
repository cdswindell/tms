package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.exceptions.InvalidParentException;
import org.tms.tds.events.EventsProcessorThreadPoolCreator;
import org.tms.tds.events.TableElementListeners;

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
    private Map<String, Object> m_elemProperties;
    
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
    @Override
    synchronized protected Map<String, Object> getElemProperties(boolean createIfEmpty)
    {
        if (m_elemProperties == null && createIfEmpty)
            m_elemProperties = new HashMap<String, Object>();
        
        return m_elemProperties;
    }

    protected void resetElemProperties()
    {
        if (m_elemProperties != null) {
            m_elemProperties.clear();
            m_elemProperties = null;
        }
    }
    
    public TableImpl getTable()
    {
        return m_table;
    }
    
    void setTable(TableImpl t)
    {
        m_table = t ;
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
        m_listeners = null;
        m_pendings = 0;
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Tags:
                return getTags();
                
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
     * Retrieve the TableContext associated with this table element; the context is associated with the parent table
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
                
                // make sure element is valid
                vetElement(e);
                
                // now check parent
                if (e.getTable() == null)
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
        fireEvents(this, TableElementEventType.OnBeforeDelete);
        
        // clear label, also resets index, if rows are indexed
        setLabel(null);        
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

    synchronized private TableElementListeners fetchListeners()
    {
        if (m_listeners == null) {
            boolean notifyInSameThread = false;
            
            TableContext tc = null;
            Table t = getTable();
            if (t != null && t.isEventProcessorThreadPool()) 
                notifyInSameThread = ((EventsProcessorThreadPoolCreator)t).isEventsNotifyInSameThread();
            else if (t != null && (tc = t.getTableContext()) != null && t.isEventProcessorThreadPool())
                notifyInSameThread = ((EventsProcessorThreadPoolCreator)tc).isEventsNotifyInSameThread();
            
            m_listeners = new TableElementListeners(this, notifyInSameThread);
        }
        
        return m_listeners;
    }
    
    protected void fireEvents(TableElement te, TableElementEventType evT, Object... args)
    {
        if (TableElementListeners.hasAnyListeners(te.getTable()))
            fetchListeners().fireEvents(te, evT, args);
    }

    protected void fireContainerEvents(Cell cell, TableElementEventType evT, Object... args)
    {
        if (cell != null && cell.isValid() && isValid() && TableElementListeners.hasAnyListeners(cell.getTable()))
            fetchListeners().fireCellContainerEvents(cell, evT, args);
    }

    @Override
    public boolean addListeners(TableElementEventType evT, TableElementListener... tels)
    {
        vetElement();
        TableImpl parent = null;
        ContextImpl tc = null;
        if ((parent = getTable()) != null && parent.isEventProcessorThreadPool())
            ((EventsProcessorThreadPoolCreator)parent).createEventProcessorThreadPool();
        else if ((parent = getTable()) != null && (tc = parent.getTableContext()) != null && tc.isEventProcessorThreadPool())
        	((EventsProcessorThreadPoolCreator)tc).createEventProcessorThreadPool();

        return fetchListeners().addListeners(evT, tels);
    }

    @Override
    public boolean removeListeners(TableElementEventType evT, TableElementListener... tels)
    {
        vetElement();
        return fetchListeners().removeListeners(evT, tels);
    }

    @Override
    public List<TableElementListener> getListeners(TableElementEventType... evTs)
    {
        vetElement();
        return fetchListeners().getListeners(evTs);
    }

    @Override
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs)
    {
        vetElement();
        return fetchListeners().removeAllListeners(evTs);
    }

    @Override
    public boolean hasListeners(TableElementEventType... evTs)
    {
        if (m_listeners == null)
            return false;
        else
            return fetchListeners().hasListeners(evTs);
    }
    
    @Override
    public boolean tag(String... tags) 
    { 
        if (tags != null && tags.length > 0) {
            ContextImpl tc = getTableContext();            
            Set<Tag> newTags = Tag.encodeTags(tags, tc);
            
            @SuppressWarnings("unchecked")
            Set<Tag> curTags = (Set<Tag>)getPropertyInternal(TableProperty.Tags);
            
            if (curTags == null) {
                setProperty(TableProperty.Tags, newTags);
                return true;
            }
            else
                return curTags.addAll(newTags);
        }
        
        return false;
    }
    
    @Override
    public boolean untag(String... tags)
    { 
        if (tags != null && tags.length > 0) {
            ContextImpl tc = getTableContext();            
            Set<Tag> oldTags = Tag.encodeTags(tags, tc, false);
            
            @SuppressWarnings("unchecked")
            Set<Tag> curTags = (Set<Tag>)getPropertyInternal(TableProperty.Tags);
            
            if (curTags == null) 
                return false;
            else
                return curTags.removeAll(oldTags);
        }
        
        return false;
    }     
    
    @Override
    public void setTags(String... tags)
    { 
        if (tags != null && tags.length > 0) {
            ContextImpl tc = getTableContext();            
            Set<Tag> newTags = Tag.encodeTags(tags, tc);
            
            setProperty(TableProperty.Tags, newTags);
        }
        else
            clearProperty(TableProperty.Tags);
    }
    
    @Override
    public String [] getTags()
    { 
        @SuppressWarnings("unchecked")
        Set<Tag> curTags = (Set<Tag>)getPropertyInternal(TableProperty.Tags);
        
        return Tag.decodeTags(curTags);
    }  
    
    @Override
    public boolean isTagged(String... tags)
    {
        @SuppressWarnings("unchecked")
        Set<Tag> curTags = getTable() != null ? (Set<Tag>)getPropertyInternal(TableProperty.Tags) : null;
        if (curTags == null || curTags.isEmpty())
            return false;
        
        // if param arg is null or empty, return true, 
        // as this element is tagged
        
        if (tags == null || tags.length == 0)
            return true;
        
        // otherwise, encode tags and use set math to return answer
        Set<Tag> queryTags = Tag.encodeTags(tags, getTableContext(), false);   
        if (queryTags == null || queryTags.isEmpty())
            return false;
        
        return curTags.containsAll(queryTags);
    }
}
