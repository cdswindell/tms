package org.tms.tds;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.events.TableElementEvent;
import org.tms.tds.events.EventProcessorExecutor;
import org.tms.tds.events.EventProcessorThreadPool;
import org.tms.tds.events.EventsProcessorThreadPoolCreator;

public class EvTableImpl extends TableImpl implements EventProcessorThreadPool, EventsProcessorThreadPoolCreator
{
	public static final Table createEvTable() 
	{
		return new EvTableImpl();
	}
	
	public static final Table createEvTable(int nRows, int nCols) 
	{
		return new EvTableImpl(nRows, nCols);
	}
	
    public static final Table createEvTable(ContextImpl c) 
    {
        return new EvTableImpl(ContextImpl.getPropertyInt(c, TableProperty.RowCapacityIncr), 
                               ContextImpl.getPropertyInt(c, TableProperty.ColumnCapacityIncr), c);
    }
    
    public static final Table createEvTable(TableImpl t) 
    {
        ContextImpl tc = t.getTableContext();
        return new EvTableImpl(ContextImpl.getPropertyInt(tc, TableProperty.RowCapacityIncr), 
                               ContextImpl.getPropertyInt(tc, TableProperty.ColumnCapacityIncr), t);
    }
    
    public static final Table createEvTable(int nRows, int nCols, ContextImpl c) 
    {
        return new EvTableImpl(nRows, nCols, c);
    }
    
    public static final Table createEvTable(int nRows, int nCols, TableImpl t) 
    {
        return new EvTableImpl(nRows, nCols, t);
    }
    
    private int m_eventsCorePoolThreads;
    private int m_eventsMaxPoolThreads;
    private long m_eventsKeepAliveTimeout;
    private TimeUnit m_eventsKeepAliveTimeUnit;
    
    private EventProcessorExecutor m_eventThreadPool;
    private Object m_eventThreadPoolLock;
    
	protected EvTableImpl() 
	{
		super();
	}

	protected EvTableImpl(int nRows, int nCols) 
	{
		super(nRows, nCols);
	}

	protected EvTableImpl(int nRows, int nCols, ContextImpl c) 
	{
		super(nRows, nCols, c);
	}

	protected EvTableImpl(int nRows, int nCols, TableImpl t) 
	{
		super(nRows, nCols, t);
	}

	@Override
    protected void initializeSpecialized(TableImpl t) 
    {
		super.initializeSpecialized(t);
		
		m_eventThreadPoolLock = new Object();
	}
    
	@Override
    protected boolean initializeSpecializedProperty(TableProperty tp, Object value)
    {
       switch (tp) {
            case isEventsNotifyInSameThread:
                if (!isValidPropertyValueBoolean(value))
                    value = ContextImpl.sf_EVENTS_NOTIFY_IN_SAME_THREAD_DEFAULT;
                setEventsNotifyInSameThread((boolean)value);
                return true;      
                
            case isEventsAllowCoreThreadTimeout:
                if (!isValidPropertyValueBoolean(value))
                    value = ContextImpl.sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT;
                eventsAllowCoreThreadTimeOut((boolean)value);
                return true;      
                
            case numEventsCorePoolThreads:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_EVENTS_CORE_POOL_SIZE_DEFAULT;
                setEventsCorePoolSize((int)value);
                return true;      
                
            case numEventsMaxPoolThreads:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_EVENTS_MAX_POOL_SIZE_DEFAULT;
                setEventsMaximumPoolSize((int)value);
                return true;      
                
            case EventsThreadKeepAliveTimeout:
                if (!isValidPropertyValueInt(value))
                    value = ContextImpl.sf_EVENTS_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                setEventsKeepAliveTime((int)value, TimeUnit.SECONDS);
                return true;      
                
            default:
                return super.initializeSpecializedProperty(tp, value);      
        }
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case isEventsNotifyInSameThread:
                return isEventsNotifyInSameThread();
                
            case isEventsAllowCoreThreadTimeout:
                return eventsAllowsCoreThreadTimeOut();
                
            case numEventsCorePoolThreads:
                return getEventsCorePoolSize();
                
            case numEventsMaxPoolThreads:
                return getEventsMaximumPoolSize();
                
            case EventsThreadKeepAliveTimeout:
                return getEventsKeepAliveTime(TimeUnit.SECONDS);
                
            default:
                return super.getProperty(key);
        }        
    }
        
    public boolean isEventsNotifyInSameThread()
    {
        
        return isSet(sf_EVENTS_NOTIFY_IN_SAME_THREAD_FLAG);
    }

    public void setEventsNotifyInSameThread(boolean notifyInSameThread)
    {
        set(sf_EVENTS_NOTIFY_IN_SAME_THREAD_FLAG, notifyInSameThread);
    }

    public int getEventsCorePoolSize()
    {
        return m_eventsCorePoolThreads;
    }

    public void setEventsCorePoolSize(int corePoolSize)
    {
        if (corePoolSize < 0) {
            if (getTableContext() != null) 
                m_eventsCorePoolThreads = getTableContext().getEventsCorePoolSize();
            else
                m_eventsCorePoolThreads = ContextImpl.getDefaultContext().getEventsCorePoolSize();
        }
        else
            m_eventsCorePoolThreads = corePoolSize; 
        
        if (m_eventThreadPool != null && m_eventsCorePoolThreads <= m_eventThreadPool.getMaximumPoolSize())
            m_eventThreadPool.setCorePoolSize(m_eventsCorePoolThreads);
    }

    public int getEventsMaximumPoolSize()
    {
        return m_eventsMaxPoolThreads;
    }

    public void setEventsMaximumPoolSize(int maxPoolSize)
    {
        if (maxPoolSize < 1) {
            if (getTableContext() != null) 
            	m_eventsMaxPoolThreads = getTableContext().getEventsMaximumPoolSize();
            else
            	m_eventsMaxPoolThreads = ContextImpl.getDefaultContext().getEventsMaximumPoolSize();
        }
        else
            m_eventsMaxPoolThreads = maxPoolSize;
        
        if (m_eventThreadPool != null && m_eventsMaxPoolThreads >= m_eventThreadPool.getCorePoolSize())
            m_eventThreadPool.setMaximumPoolSize(m_eventsMaxPoolThreads);
    }
  
    public long getEventsKeepAliveTime(TimeUnit unit)
    {
        if (unit == null)
            unit = TimeUnit.SECONDS;
        return m_eventsKeepAliveTimeUnit.convert(m_eventsKeepAliveTimeout, unit);
    }

    public void setEventsKeepAliveTime(long time, TimeUnit unit)
    {
        if (unit == null)
            unit = TimeUnit.SECONDS;
        
        if (time <= 0) {
            m_eventsKeepAliveTimeUnit = unit;
            if (getTableContext() != null) 
            	m_eventsKeepAliveTimeout = getTableContext().getEventsKeepAliveTime(unit);
            else 
            	m_eventsKeepAliveTimeout = ContextImpl.getDefaultContext().getEventsKeepAliveTime(unit);
        }
        else {
            m_eventsKeepAliveTimeout = time;
            m_eventsKeepAliveTimeUnit = unit;
        }
                
        if (m_eventThreadPool != null)
            m_eventThreadPool.setKeepAliveTime(m_eventsKeepAliveTimeout, m_eventsKeepAliveTimeUnit);
    }

    public boolean eventsAllowsCoreThreadTimeOut()
    {
        return isSet(sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_FLAG);
    }

    public void eventsAllowCoreThreadTimeOut(boolean allowCoreThreadTimeout)
    {
        set(sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_FLAG, allowCoreThreadTimeout);
    }
    
    /**
     * Create and initialize the thread pool to support event processing.
     */
    public void createEventProcessorThreadPool()
    {
        synchronized (m_eventThreadPoolLock) {
            if (m_eventThreadPool == null) {
                int maxPoolSize = Math.max(getEventsMaximumPoolSize(), getEventsCorePoolSize());
                TimeUnit unit = m_eventsKeepAliveTimeUnit != null ? m_eventsKeepAliveTimeUnit : TimeUnit.SECONDS;
                m_eventThreadPool = new EventProcessorExecutor(getEventsCorePoolSize(),
                                                               maxPoolSize,
                                                               getEventsKeepAliveTime(unit),
                                                               unit,
                                                               eventsAllowsCoreThreadTimeOut());  
            }
        }
    }

    @Override
    public void submitEvents(Collection<TableElementEvent> events)
    {
        createEventProcessorThreadPool();       
        m_eventThreadPool.submitEvents(events);
    }

    @Override
    public boolean remove(TableElementEvent e)
    {
        if (m_eventThreadPool != null)
            return m_eventThreadPool.remove(e);
        else
            return false;
    }

    @Override
    public void shutdownEventProcessorThreadPool()
    {
        if (m_eventThreadPool != null)
            m_eventThreadPool.shutdownEventProcessorThreadPool();
    }   
}
