package org.tms.api.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void fireEvents(TableElement te, TableElementEventType evT, Object... args)
    {
        // TODO: Implement
    }
}
