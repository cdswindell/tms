package org.tms.api.event;

import java.util.EventObject;

abstract public class TableElementEvent extends EventObject
{
    private static final long serialVersionUID = 2207788387645138100L;
    
    private TableElementEventType m_evT;
    private long m_timeStamp;
    private long m_assemblyId;
    private Listenable m_trigger;
    
    public TableElementEvent(TableElementEventType evT, Listenable source, Listenable trigger)
    {
        super(source);
        m_trigger = trigger;
        m_evT = evT;
        m_timeStamp = System.currentTimeMillis();
    }

    public TableElementEvent(TableElementEventType evT, Listenable source)
    {
        this(evT, source, null);
    }

    TableElementEvent(TableElementEventType evT, Listenable source, Listenable trigger, long assemblyId)
    {
        this(evT, source, trigger);
        m_assemblyId = assemblyId;
    }

    @Override
    public Listenable getSource()
    {
        return (Listenable)super.getSource();
    }

    public TableElementEventType getType()
    {
        return m_evT;
    }
    
    public Listenable getTrigger()
    {
        return m_trigger;
    }
    
    public boolean isTriggered()
    {
        return m_trigger != null && m_trigger != getSource();
    }
    
    public long getAssemblyId()
    {
        return m_assemblyId;
    }
    
    public long getTimeStamp()
    {
        return m_timeStamp;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (m_assemblyId ^ (m_assemblyId >>> 32));
        result = prime * result + ((m_evT == null) ? 0 : m_evT.hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getTrigger() == null) ? 0 : getTrigger().hashCode());
        
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof TableElementEvent)) return false;
        
        TableElementEvent other = (TableElementEvent) obj;
        if (m_assemblyId != other.m_assemblyId) return false;
        if (m_evT != other.getType()) return false;
        if (getSource() != other.getSource()) return false;
        if (getTrigger() != other.getTrigger()) return false;
        
        return true;
    }

    public String toString()
    {
        return String.format("Source: %s Trigger: %s Event: %s (%d:%d)", 
                getSource(), getTrigger(), getType(), getAssemblyId(), getTimeStamp());
    }
}
