package org.tms.api.event;

public class CellValueChangedEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = -8132668503086998486L;

    public static final CellValueChangedEvent createOn(Listenable te, long timeStamp, Object newValue)
    {
        return new CellValueChangedEvent(te, TableElementEventType.OnNewValue, timeStamp, newValue);
    }
    
    public static final CellValueChangedEvent createOn(Listenable te, long timeStamp, Object oldValue, Object newValue)
    {
        return new CellValueChangedEvent(te, TableElementEventType.OnNewValue, timeStamp, oldValue, newValue);
    }
    
    public static final CellValueChangedEvent createOnBefore(Listenable te, long timeStamp, Object oldValue, Object newValue)
    {
        return new CellValueChangedEvent(te, TableElementEventType.OnBeforeNewValue, timeStamp, oldValue, newValue);
    }

    private boolean m_oldValueAvailable;
    private Object m_oldValue;
    private Object m_newValue;
    
    private CellValueChangedEvent(Listenable source, TableElementEventType evT, long timeStamp, Object newValue)
    {
        super(evT, source, timeStamp);
        
        m_oldValueAvailable = false;
        m_newValue = newValue;
    }
    
    private CellValueChangedEvent(Listenable source, TableElementEventType evT, long timeStamp, Object oldValue, Object newValue)
    {
        this(source, evT, timeStamp, newValue);
        
        m_oldValueAvailable = true;       
        m_oldValue = oldValue;
    }
     
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeNewValue; }
    
    public boolean isOldValueAvailable() { return m_oldValueAvailable; }
    
    public Object getOldValue() { return m_oldValue; }
    
    public Object getNewValue() { return m_newValue; }
}
