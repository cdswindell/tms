package org.tms.api.event;

import java.util.EventObject;

abstract public class TableElementEvent extends EventObject
{
    private static final long serialVersionUID = -2530794656893005953L;
    
    private TableElementEventType m_evT;
    private long m_timeStamp;
    private long m_assemblyId;
    
    public TableElementEvent(TableElementEventType evT, Listenable source)
    {
        super(source);
        m_evT = evT;
        m_timeStamp = System.currentTimeMillis();
    }

    TableElementEvent(TableElementEventType evT, Listenable source, long assemblyId)
    {
        this(evT, source);
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
        
        return true;
    }

    public String toString()
    {
        return String.format("Source: %s Event: %s (%d:%d)", getSource(), getType(), getAssemblyId(), getTimeStamp());
    }
}
