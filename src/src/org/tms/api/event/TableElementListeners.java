package org.tms.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.exceptions.UnimplementedException;

public class TableElementListeners implements Listenable
{
    static final private AtomicLong sf_AssemblyIdCntr = new AtomicLong();
    
    static private EventProcessorExecutor sf_EventProcessorThreadPool = null;
    
    static final private Map<Table, Set<TableElementEventType>> sf_TableRegisteredListenersMap = 
            new ConcurrentHashMap<Table, Set<TableElementEventType>>();
    
    static final public boolean hasAnyListeners(Table table, TableElementEventType... evTs) 
    {
        if (table != null) {
            synchronized (table) {
                Set<TableElementEventType> registeredListeners = sf_TableRegisteredListenersMap.get(table);
                if (registeredListeners == null || registeredListeners.isEmpty())
                    return false;
                
                // check individual types 
                if (evTs == null || evTs.length == 0)
                    return true;
                
                boolean hasSome = false;
                for (TableElementEventType evT : evTs) {
                    if (evT == null)
                        continue;
                    else if (!registeredListeners.contains(evT))
                        return false;
                    else
                        hasSome = true;
                }
                
                return hasSome;
            }
        }
        else
            return false;
    }
    
    static final public void deregisterTable(Table t)
    {
        if (t != null)
            sf_TableRegisteredListenersMap.remove(t);
    }
    
    static final private void registerTableListener(TableElement te, TableElementEventType... evTs)
    {
        if (te == null || evTs == null || evTs.length == 0) return;
        
        Table parentTable = te.getTable();
        if (parentTable == null) return;
        
        synchronized(parentTable) {
            Set<TableElementEventType> registeredListeners = sf_TableRegisteredListenersMap.get(parentTable);
            if (registeredListeners == null) {
                registeredListeners = new HashSet<TableElementEventType>();
                sf_TableRegisteredListenersMap.put(parentTable, registeredListeners);
            }
                
            for (TableElementEventType evT : evTs) {
                if (evT == null) continue;
                
                registeredListeners.add(evT);
            }
        }
    }
    
    /*
     * Instance fields and methods
     */
    private TableElement m_te;
    private Map<TableElementEventType, Set<TableElementListener>> m_listeners;
    private boolean m_notifyInSameThread;
    
    public TableElementListeners(TableElement te)
    {
        m_te = te;
        m_notifyInSameThread = false;
        m_listeners = new HashMap<TableElementEventType, Set<TableElementListener>>();
    }
    
    @Override
    synchronized public boolean addListeners(TableElementEventType evT, TableElementListener... tels)
    {
        if (tels != null && tels.length > 0) {
            boolean addedAny = false;
            TableElementEventType [] evTsArray = evT != null ? new TableElementEventType [] {evT} : TableElementEventType.values();
            for (TableElementEventType eT : evTsArray) {
                if (eT.isImplementedBy(m_te)) {
                    Set<TableElementListener> evTlisteners = m_listeners.get(evT);
                    if (evTlisteners == null) {
                        evTlisteners = new LinkedHashSet<TableElementListener>();
                        m_listeners.put(eT, evTlisteners);
                    }
                    
                    for (TableElementListener tel : tels) {
                        if (evTlisteners.add(tel))
                            addedAny = true;
                    }
                }
                else
                    throw new UnimplementedException(String.format("%s not supported by %s", evT, m_te.getElementType()));
            }
            
            if (addedAny)
                TableElementListeners.registerTableListener(m_te, evT);
            
            return addedAny;
        }
        else 
            return false;
    }

    @Override
    synchronized public boolean removeListeners(TableElementEventType evT, TableElementListener... tels)
    {
        boolean removedAny = false;
        TableElementEventType [] evTsArray = evT != null ? new TableElementEventType [] {evT} : TableElementEventType.values();
        for (TableElementEventType eT : evTsArray) {
            Set<TableElementListener> evTlisteners = m_listeners.get(evT);
            if (evTlisteners != null)     { 
                if (tels != null && tels.length > 0) {
                    for (TableElementListener tel : tels) {
                        if (tel == null)
                            continue;
                        
                        if (evTlisteners.remove(tel))
                            removedAny = true;
                    }
                    
                    if (evTlisteners.isEmpty())
                        m_listeners.remove(eT);
                }
                else {
                    // remove all listeners
                    removedAny = m_listeners.remove(evT) != null;
                }
            }
        }
        
        return removedAny;
    }

    @Override
    synchronized  public List<TableElementListener> getListeners(TableElementEventType... evTs)
    {
        List<TableElementListener> listeners = new ArrayList<TableElementListener>();
        
        TableElementEventType [] evTsArray = evTs != null && evTs.length > 0 ? evTs : TableElementEventType.values();
        for (TableElementEventType evT : evTsArray) {
            if (evT == null) 
                continue;
            
            Set<TableElementListener> evTlisteners = m_listeners.get(evT);
            if (evTlisteners != null)
                listeners.addAll(evTlisteners);
        }
        
        return listeners;
    }

    @Override
    synchronized public List<TableElementListener> removeAllListeners(TableElementEventType... evTs)
    {
        List<TableElementListener> listeners = getListeners(evTs);
        if (evTs == null || evTs.length == 0)
            m_listeners.clear();
        else {
            for (TableElementEventType evT : evTs) {
                if (evT != null)
                    m_listeners.remove(evT);
            }
        }
            
        return listeners;
    }

    @Override
    public boolean hasListeners(TableElementEventType... evTs)
    {
        if (evTs == null || evTs.length == 0)
            return !m_listeners.isEmpty();
        else {
            List<TableElementListener> listeners = getListeners(evTs);
            return listeners != null && listeners.size() > 0;
        }
    }

    public void fireCellContainerEvents(Cell cell, TableElementEventType evT, Object... args)
    {        
        if (evT.isAlertContainer()) {
            Set<TableElementEvent> events = new LinkedHashSet<TableElementEvent>();
            long assemblyId = 0;
            
            Row r = cell.getRow();
            if (r != null && (r.hasListeners(evT) || r.getNumSubsets() > 0)) {
                if (assemblyId == 0)
                    assemblyId = sf_AssemblyIdCntr.incrementAndGet();
                generateEvents(events, assemblyId, true, r, evT, args);
            }
            
            Column c = cell.getColumn();
            if (c != null && (c.hasListeners(evT) || c.getNumSubsets() > 0)) {
                if (assemblyId == 0)
                    assemblyId = sf_AssemblyIdCntr.incrementAndGet();
                generateEvents(events, assemblyId, true, c, evT, args);
            }
            
            // harvest table events, if we didn't process row or column
            if (assemblyId == 0) {
                Table parent = cell.getTable();
                if (parent != null && (parent.hasListeners(evT) || parent.getNumSubsets() > 0)) {
                    assemblyId = sf_AssemblyIdCntr.incrementAndGet();
                    generateEvents(events, assemblyId, true, parent, evT, args);                   
                }
            }
            
            if (!events.isEmpty()) 
                fireEvents(cell, evT, events);
        }
    }
    
    public void fireEvents(Listenable te, TableElementEventType evT, Object... args)
    {
        if (evT == null || te == null)
            return;
        
        if (!(te.hasListeners(evT) || evT.isAlertContainer())) // if we don't have listeners and we don't alert containers, return
            return;
        
        if (!(te instanceof TableElement))
            return;
        
        Set<TableElementEvent> events = generateEvents(te, evT, args);
        if (events != null && !events.isEmpty()) 
            fireEvents((TableElement)te, evT, events);
    }

    private void fireEvents(TableElement te, TableElementEventType evT, Set<TableElementEvent> events)
    {
        // if we are asked to throw exceptions, process the events in the current thread
        if (evT.isNotifyInSameThread() || m_notifyInSameThread) {
            getTable().pushCurrent();
            try {
                for (TableElementEvent e : events) {
                    List<TableElementListener> listeners = ((Listenable) e.getSource()).getListeners(evT);
                    if (listeners != null) {
                        for (TableElementListener listener : listeners) {
                            listener.eventOccured(e);
                        }
                    }
                } 
            }
            finally {
                getTable().popCurrent();
            }
        }
        else
            queueEvents(te, events);
    }

    private void queueEvents(TableElement te, Set<TableElementEvent> events)
    {
        if (te.getTable() instanceof EventProcessorThreadPool)
            ((EventProcessorThreadPool)te.getTable()).submitEvents(events);
        else if (te.getTableContext() instanceof EventProcessorThreadPool)
            ((EventProcessorThreadPool)te.getTableContext()).submitEvents(events);
        else {
            synchronized (TableElementListeners.class) {
                if (sf_EventProcessorThreadPool == null) 
                    sf_EventProcessorThreadPool = new EventProcessorExecutor();
            }
            
            sf_EventProcessorThreadPool.submitEvents(events);
        }
    }

    private Set<TableElementEvent> generateEvents(Listenable te, TableElementEventType evT, Object[] args)
    {
        Set<TableElementEvent> events = new LinkedHashSet<TableElementEvent>();
        long assemblyId = sf_AssemblyIdCntr.incrementAndGet();
        
        generateEvents(events, assemblyId, true, (TableElement)te, evT, args);
        
        // add in parent table
        Table parentTable =  ((TableElement)te).getTable();
        if (parentTable != null && parentTable != this)
            generateEvents(events, assemblyId, true, parentTable, evT, args);                            
            
        return events;
    }

    private void generateEvents(Set<TableElementEvent> events, long assemblyId, boolean doRecurse, 
                                TableElement te, TableElementEventType evT, Object[] args)
    {
        if (te == null)
            return;
        
        if (evT.isImplementedBy(te) && ((Listenable)te).hasListeners(evT)) {
            TableElementEvent event = TableElementEventFactory.createEvent((TableElement)te, evT, assemblyId, args);
            if (event != null)
                events.add(event);
        }
        
        // recursion exit expression
        if (!doRecurse)
            return;
        
        if (evT.isAlertContainer()) {
            List<Subset> subsets = evT.isImplementedBy(ElementType.Subset) ? te.getSubsets() : null;
            switch (te.getElementType()) {
                case Cell:
                    generateEvents(events, assemblyId, true, ((Cell)te).getRow(), evT, args);
                    generateEvents(events, assemblyId, true, ((Cell)te).getColumn(), evT, args);
                    
                    if (subsets != null) {
                        for (Subset s : subsets) {
                            generateEvents(events, assemblyId, false, s, evT, args);                            
                        }
                    }
                    break;
                    
                case Row:
                case Column:
                    generateEvents(events, assemblyId, true, te.getTable(), evT, args);                            
                    // do no break, continue on and grab subsets
                    
                case Table:
                    if (subsets != null) {
                        for (Subset s : subsets) {
                            generateEvents(events, assemblyId, false, s, evT, args);                            
                        }
                    }
                    break;
                    
                default:
                    break;
            }
        }                
    }

    public ElementType getElementType()
    {
        return m_te.getElementType();
    }

    public Table getTable()
    {
        return m_te.getTable();
    }
}
