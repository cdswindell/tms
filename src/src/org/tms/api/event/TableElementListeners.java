package org.tms.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final AtomicLong sf_AssemblyIdCntr = new AtomicLong();
    
    private TableElement m_te;
    protected Map<TableElementEventType, Set<TableElementListener>> m_listeners;
    
    public TableElementListeners(TableElement te)
    {
        m_te = te;
        m_listeners = new HashMap<TableElementEventType, Set<TableElementListener>>();
    }
    
    @Override
    synchronized public boolean addListener(TableElementEventType evT, TableElementListener... tels)
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
            
            return addedAny;
        }
        else 
            return false;
    }

    @Override
    synchronized public boolean removeListener(TableElementEventType evT, TableElementListener... tels)
    {
        if (tels != null && tels.length > 0) {            
            boolean removedAny = false;
            TableElementEventType [] evTsArray = evT != null ? new TableElementEventType [] {evT} : TableElementEventType.values();
            for (TableElementEventType eT : evTsArray) {
                Set<TableElementListener> evTlisteners = m_listeners.get(evT);
                if (evTlisteners != null)     {           
                    for (TableElementListener tel : tels) {
                        if (tel == null)
                            continue;
                        
                        if (evTlisteners.remove(tel))
                            removedAny = true;
                    }
                    
                    if (evTlisteners.isEmpty())
                        m_listeners.remove(eT);
                }
            }
            
            return removedAny;
        }
        else 
            return false;
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
        if (evTs == null)
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

    public void fireCellContainerEvents(Cell cell, TableElementEventType evT, Object[] args)
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
            
            if (events != null && !events.isEmpty()) 
                fireEvents(evT, events);
        }
    }
    
    public void fireEvents(Listenable te, TableElementEventType evT, Object... args)
    {
        if (evT == null || te == null)
            return;
        
        if (!te.hasListeners(evT))
            return;
        
        if (!(te instanceof TableElement))
            return;
        
        Set<TableElementEvent> events = generateEvents(te, evT, args);
        if (events != null && !events.isEmpty()) 
            fireEvents(evT, events);
    }

    private void fireEvents(TableElementEventType evT, Set<TableElementEvent> events)
    {
        // if we are asked to throw exceptions, process the events in the current thread
        if (evT.isThrowExceptions()) {
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
            queueEvents(events);
    }

    private void queueEvents(Set<TableElementEvent> events)
    {
        // TODO Auto-generated method stub
        
    }

    private Set<TableElementEvent> generateEvents(Listenable te, TableElementEventType evT, Object[] args)
    {
        Set<TableElementEvent> events = new LinkedHashSet<TableElementEvent>();
        long assemblyId = sf_AssemblyIdCntr.incrementAndGet();
        
        generateEvents(events, assemblyId, true, (TableElement)te, evT, args);
        
        // add in parent table
        Table parentTable =  te.getTable();
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

    @Override
    public ElementType getElementType()
    {
        return m_te.getElementType();
    }

    @Override
    public Table getTable()
    {
        return m_te.getTable();
    }
}
