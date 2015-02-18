package org.tms.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.exceptions.UnimplementedException;

public class TableElementListeners implements Listenable
{
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
        return !m_listeners.isEmpty();
    }

    public void fireEvents(Listenable te, TableElementEventType evT, Object... args)
    {
        if (evT == null || te == null)
            return;
        
        if (!te.hasListeners(evT))
            return;
        
        if (!(te instanceof TableElement))
            return;
        
        List<TableElementEvent> events = generateEvents(te, evT, args);
        if (events != null && !events.isEmpty()) 
            fireEvents(te, evT, events);
    }

    private void fireEvents(Listenable te, TableElementEventType evT, List<TableElementEvent> events)
    {
        // if we are asked to throw exceptions, process the events in the current thread
        if (evT.isThrowExceptions()) {
            ((TableElement)te).getTable().pushCurrent();
            try {
                for (TableElementEvent e : events) {
                List<TableElementListener> listeners = ((TableElementListeners) e.getSource()).getListeners(evT);
                if (listeners != null) {
                    for (TableElementListener listener : listeners) {
                            listener.eventOccured(e);
                        }
                    }
                } 
            }
            finally {
                ((TableElement)te).getTable().popCurrent();
            }
        }
        else
            queueEvents(events);
    }

    private void queueEvents(List<TableElementEvent> events)
    {
        // TODO Auto-generated method stub
        
    }

    private List<TableElementEvent> generateEvents(Listenable te, TableElementEventType evT, Object[] args)
    {
        List<TableElementEvent> events = new ArrayList<TableElementEvent>();
        
        generateEvents(events, true, (TableElement)te, evT, args);
        
        // add in parent table
        Table parentTable =  te.getTable();
        if (parentTable != null && parentTable != this)
            generateEvents(events, true, parentTable, evT, args);                            
            
        return events;
    }

    private void generateEvents(List<TableElementEvent> events, boolean doRecurse, TableElement te, TableElementEventType evT, Object[] args)
    {
        if (te == null)
            return;
        
        if (evT.isImplementedBy(te) && ((Listenable)te).hasListeners(evT)) {
            TableElementEvent event = TableElementEventFactory.createEvent((TableElement)te, evT, args);
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
                    if (subsets != null) {
                        for (Subset s : subsets) {
                            generateEvents(events, false, s, evT, args);                            
                        }
                    }
                    
                    generateEvents(events, true, ((Cell)te).getRow(), evT, args);
                    generateEvents(events, true, ((Cell)te).getColumn(), evT, args);
                    break;
                    
                case Row:
                case Column:
                case Table:
                    if (subsets != null) {
                        for (Subset s : subsets) {
                            generateEvents(events, false, s, evT, args);                            
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
