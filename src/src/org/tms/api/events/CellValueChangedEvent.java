package org.tms.api.events;

public class CellValueChangedEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = 7240313166857443883L;

    static final CellValueChangedEvent createOn(Listenable source, Listenable trigger, long assemblyId, Object newValue)
    {
        return new CellValueChangedEvent(source, trigger, TableElementEventType.OnNewValue, assemblyId, newValue);
    }
    
    static final CellValueChangedEvent createOn(Listenable source, Listenable trigger, long assemblyId, Object oldValue, Object newValue)
    {
        return new CellValueChangedEvent(source, trigger, TableElementEventType.OnNewValue, assemblyId, oldValue, newValue);
    }
    
    static final CellValueChangedEvent createOnBefore(Listenable source, Listenable trigger, long assemblyId, Object oldValue, Object newValue)
    {
        return new CellValueChangedEvent(source, trigger, TableElementEventType.OnBeforeNewValue, assemblyId, oldValue, newValue);
    }

    private boolean m_oldValueAvailable;
    private Object m_oldValue;
    private Object m_newValue;
    
    private CellValueChangedEvent(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId, Object newValue)
    {
        super(evT, source, trigger, assemblyId);
        
        m_oldValueAvailable = false;
        m_newValue = newValue;
    }
    
    private CellValueChangedEvent(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId, Object oldValue, Object newValue)
    {
        this(source, trigger, evT, assemblyId, newValue);
        
        m_oldValueAvailable = true;       
        m_oldValue = oldValue;
    }
     
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeNewValue; }
    
    public boolean isOldValueAvailable() { return m_oldValueAvailable; }
    
    public Object getOldValue() { return m_oldValue; }
    
    public Object getNewValue() { return m_newValue; }
}
