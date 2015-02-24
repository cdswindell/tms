package org.tms.tds;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.PendingDerivationExecutor;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.event.EventProcessorExecutor;
import org.tms.api.event.EventProcessorThreadPool;
import org.tms.api.event.TableElementEvent;
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.teq.Derivation;
import org.tms.util.WeakHashSet;

public class ContextImpl extends BaseElementImpl implements TableContext, DerivableThreadPool, 
															EventProcessorThreadPool, EventsProcessorThreadPoolCreator
{
    private static ContextImpl sf_DEFAULT_CONTEXT;
    
    static final int sf_ROW_CAPACITY_INCR_DEFAULT = 1024;
    static final int sf_COLUMN_CAPACITY_INCR_DEFAULT = 32;
    static final double sf_FREE_SPACE_THRESHOLD_DEFAULT = 2.0;
    static final boolean sf_TABLE_PERSISTANCE_DEFAULT = false;

    static final int sf_PENDING_CORE_POOL_SIZE_DEFAULT = 8;
    static final int sf_PENDING_MAX_POOL_SIZE_DEFAULT = 128;
    static final int sf_PENDING_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT = 5;
    static final boolean sf_PENDING_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT = true;
    
    static final boolean sf_EVENTS_NOTIFY_IN_SAME_THREAD_DEFAULT = false;
    static final int sf_EVENTS_CORE_POOL_SIZE_DEFAULT = 2;
    static final int sf_EVENTS_MAX_POOL_SIZE_DEFAULT = 5;
    static final int sf_EVENTS_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT = 30;
    static final boolean sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT = false;
    
    static final boolean sf_READ_ONLY_DEFAULT = false;
    static final boolean sf_SUPPORTS_NULL_DEFAULT = true;
    static final boolean sf_ENFORCE_DATA_TYPE_DEFAULT = false;
    static final boolean sf_AUTO_RECALCULATE_DEFAULT = true;
    
    static final boolean sf_ROW_LABELS_INDEXED_DEFAULT = false;
    static final boolean sf_COLUMN_LABELS_INDEXED_DEFAULT = false;
    static final boolean sf_CELL_LABELS_INDEXED_DEFAULT = false;
    static final boolean sf_SUBSET_LABELS_INDEXED_DEFAULT = false;
    
    static final Map<TableProperty, Object> sf_PROPERTY_DEFAULTS = new HashMap<TableProperty, Object>();    

    public static TableContext createContext()
    {
        return new ContextImpl(false, null);
    }
    
    public static TableContext createContext(TableContext c)
    {
        return new ContextImpl(false, c);
    }
    
    public static TableContext createDefaultContext()
    {
        return getDefaultContext();
    }
    
    protected static double getPropertyDouble(ContextImpl c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyDouble(key);
        else
            return getDefaultContext().getPropertyDouble(key);
    }

    protected static int getPropertyInt(ContextImpl c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyInt(key);
        else
            return getDefaultContext().getPropertyInt(key);
    }

    protected static boolean getPropertyBoolean(ContextImpl c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyBoolean(key);
        else
            return getDefaultContext().getPropertyBoolean(key);
    }

    synchronized protected static ContextImpl getDefaultContext()
    {
        if (sf_DEFAULT_CONTEXT == null) {
            sf_DEFAULT_CONTEXT = new ContextImpl(true, null);
            sf_DEFAULT_CONTEXT.setLabel("Default Table Context");
        }
            
        return sf_DEFAULT_CONTEXT;
    }

    private Set<TableImpl> m_registeredNonpersistantTables;
    private Set<TableImpl> m_registeredPersistantTables;
    
    private int m_rowCapacityIncr;
    private int m_columnCapacityIncr;
    private TokenMapper m_tokenMapper;
    private int m_precision;
    private double m_freeSpaceThreshold;
    
    private int m_eventsCorePoolThreads;
    private int m_eventsMaxPoolThreads;
    private long m_eventsKeepAliveTimeout;
    private TimeUnit m_eventsKeepAliveTimeUnit;
    
    private int m_pendingCorePoolThreads;
    private int m_pendingMaxPoolThreads;
    private long m_pendingKeepAliveTimeout;
    private TimeUnit m_pendingKeepAliveTimeUnit;
    private PendingDerivationExecutor m_pendingThreadPool;
    
    private EventProcessorExecutor m_eventThreadPool;
    private Object m_eventThreadPoolLock;

    private Map<String, Object> m_elemProperties;
    
    private ContextImpl(boolean isDefault, TableContext otherContext)
    {
        super();      
        set(sf_IS_DEFAULT_FLAG, isDefault);
        m_registeredNonpersistantTables = new WeakHashSet<TableImpl>();
        m_registeredPersistantTables = new HashSet<TableImpl>();
        
        // initialize from default context, unless this the default
        if (otherContext != null && !(otherContext instanceof ContextImpl))
        	throw new UnsupportedImplementationException(otherContext);
        
        initialize((ContextImpl)otherContext);
     }

    protected ContextImpl()
    {
        this(false, null);
    }
    
    protected ContextImpl(TableContext otherContext)
    {
        this(false, otherContext);
    }
    
    protected void initialize()
    {
        initialize(ContextImpl.getDefaultContext());
    }
    
    protected void initialize(ContextImpl otherContext)
    {
        ContextImpl sourceContext = isDefault() ? otherContext : (otherContext != null ? otherContext : ContextImpl.getDefaultContext());
        if (this == sourceContext)
            return; // nothing to do
        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = null;
            if (isDefault() && otherContext == null)
                value = getPropertyDefault(tp);
            else
                value = sourceContext.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            // set the corresponding value
            switch (tp)
            {
                case RowCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = sf_ROW_CAPACITY_INCR_DEFAULT;
                    setRowCapacityIncr((int)value);
                    break;
                    
                case ColumnCapacityIncr:
                    if (!isValidPropertyValueInt(value))
                        value = sf_COLUMN_CAPACITY_INCR_DEFAULT;
                    setColumnCapacityIncr((int)value);
                    break;
                    
                case FreeSpaceThreshold:
                    if (!isValidPropertyValueDouble(value))
                        value = sf_FREE_SPACE_THRESHOLD_DEFAULT;
                    setFreeSpaceThreshold((double)value);
                    break;
                    
                case Precision:
                    if (!isValidPropertyValueInt(value))
                        value = Derivation.sf_DEFAULT_PRECISION;
                    setPrecision((int)value);
                    break;
                    
                case isAutoRecalculate:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_AUTO_RECALCULATE_DEFAULT;
                    setAutoRecalculate((boolean)value);
                    break;
                    
                case isRowLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_ROW_LABELS_INDEXED_DEFAULT;
                    setRowLabelsIndexed((boolean)value);
                    break;
                    
                case isColumnLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_COLUMN_LABELS_INDEXED_DEFAULT;
                    setColumnLabelsIndexed((boolean)value);
                    break;
                    
                case isCellLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_CELL_LABELS_INDEXED_DEFAULT;
                    setCellLabelsIndexed((boolean)value);
                    break;
                    
                case isSubsetLabelsIndexed:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_SUBSET_LABELS_INDEXED_DEFAULT;
                    setSubsetLabelsIndexed((boolean)value);
                    break;
                    
                case isPersistant:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_TABLE_PERSISTANCE_DEFAULT;
                    setPersistant((boolean)value);
                    break;
                    
                case TokenMapper:
                    if (value == null)
                        value = TokenMapper.fetchTokenMapper(this);
                    else 
                        value = TokenMapper.cloneTokenMapper((TokenMapper)value, this);
                    setTokenMapper((TokenMapper)value);
                    break;
                    
                case isPendingAllowCoreThreadTimeout:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_PENDING_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT;
                    pendingAllowCoreThreadTimeOut((boolean)value);
                    break;                   
                    
                case numPendingCorePoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_PENDING_CORE_POOL_SIZE_DEFAULT;
                    setPendingCorePoolSize((int)value);
                    break;
                    
                case numPendingMaxPoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_PENDING_MAX_POOL_SIZE_DEFAULT;
                    setPendingMaximumPoolSize((int)value);
                    break;
                    
                case PendingThreadKeepAliveTimeout:
                    if (!isValidPropertyValueInt(value))
                        value = sf_PENDING_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                    setPendingKeepAliveTime((int)value, TimeUnit.SECONDS);
                    break;
                
                case isEventsNotifyInSameThread:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_EVENTS_NOTIFY_IN_SAME_THREAD_DEFAULT;
                    setEventsNotifyInSameThread((boolean)value);
                    break;      
                    
                case isEventsAllowCoreThreadTimeout:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_EVENTS_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT;
                    eventsAllowCoreThreadTimeOut((boolean)value);
                    break;                   
                    
                case numEventsCorePoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_EVENTS_CORE_POOL_SIZE_DEFAULT;
                    setEventsCorePoolSize((int)value);
                    break;
                    
                case numEventsMaxPoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_EVENTS_MAX_POOL_SIZE_DEFAULT;
                    setEventsMaximumPoolSize((int)value);
                    break;
                    
                case EventsThreadKeepAliveTimeout:
                    if (!isValidPropertyValueInt(value))
                        value = sf_EVENTS_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                    setEventsKeepAliveTime((int)value, TimeUnit.SECONDS);
                    break;
                    
                default:
                    if (!tp.isOptional())
                        throw new IllegalStateException("No initialization available for Context Property: " + tp);                       
            }
        }
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        m_pendingThreadPool = null;        
        m_eventThreadPool = null;
        m_eventThreadPoolLock = new Object();  
        m_eventsKeepAliveTimeUnit = m_pendingKeepAliveTimeUnit = TimeUnit.SECONDS;
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numTables:
                return getNumTables();
                
            case RowCapacityIncr:
                return getRowCapacityIncr();
                
            case ColumnCapacityIncr:
                return getColumnCapacityIncr();
                
            case FreeSpaceThreshold:
                return getFreeSpaceThreshold();
                
            case Precision:
                return getPrecision();
                
            case isAutoRecalculate:
                return isAutoRecalculate();
                
            case isRowLabelsIndexed:
                return isRowLabelsIndexed();
                
            case isColumnLabelsIndexed:
                return isColumnLabelsIndexed();
                
            case isCellLabelsIndexed:
                return isCellLabelsIndexed();
                
            case isSubsetLabelsIndexed:
                return isSubsetLabelsIndexed();
                
            case isPersistant:
                return isPersistant();
                
            case TokenMapper:
                return getTokenMapper();
                
            case isPendingAllowCoreThreadTimeout:
                return pendingAllowsCoreThreadTimeOut();
                
            case numPendingCorePoolThreads:
                return getPendingCorePoolSize();
                
            case numPendingMaxPoolThreads:
                return getPendingMaximumPoolSize();
                
            case PendingThreadKeepAliveTimeout:
                return getPendingKeepAliveTime(TimeUnit.SECONDS);                
             
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
        
    @Override
    synchronized public int getNumTables()
    {
        return m_registeredNonpersistantTables.size() + m_registeredPersistantTables.size();
    }

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
    
    public double getFreeSpaceThreshold()
    {
        return m_freeSpaceThreshold;
    }
    
    public void setFreeSpaceThreshold(double value)
    {
        if (value < 0.0) {
            if (this.isDefault()) 
                m_freeSpaceThreshold = sf_FREE_SPACE_THRESHOLD_DEFAULT;
            else
                m_freeSpaceThreshold = ContextImpl.getDefaultContext().getFreeSpaceThreshold();
        }
        else 
            m_freeSpaceThreshold = value;
    }

    private Object getPropertyDefault(TableProperty tp)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ElementType getElementType()
    {
        return ElementType.Context;
    }
    @Override
    protected boolean isNull()
    {
         return m_registeredNonpersistantTables.isEmpty() && m_registeredPersistantTables.isEmpty();
    }
    
    protected boolean isDefault()
    {
        return isSet(sf_IS_DEFAULT_FLAG);
    }

    public boolean isAutoRecalculate()
    {
        return isSet(sf_AUTO_RECALCULATE_FLAG);
    }
    
    public void setAutoRecalculate(boolean value)
    {
        set(sf_AUTO_RECALCULATE_FLAG, value);
    }

    public boolean isRowLabelsIndexed()
    {
        return isSet(sf_ROW_LABELS_INDEXED_FLAG);
    }

    public void setRowLabelsIndexed(boolean rowLabelsIndexed)
    {
        set(sf_ROW_LABELS_INDEXED_FLAG, rowLabelsIndexed);
    }

    public boolean isColumnLabelsIndexed()
    {
        return isSet(sf_COLUMN_LABELS_INDEXED_FLAG);
    }

    public void setColumnLabelsIndexed(boolean colLabelsIndexed)
    {
        set(sf_COLUMN_LABELS_INDEXED_FLAG, colLabelsIndexed);
    }

    public boolean isCellLabelsIndexed()
    {
        return isSet(sf_CELL_LABELS_INDEXED_FLAG);
    }

    public void setCellLabelsIndexed(boolean rowLabelsIndexed)
    {
        set(sf_CELL_LABELS_INDEXED_FLAG, rowLabelsIndexed);
    }

    public boolean isSubsetLabelsIndexed()
    {
        return isSet(sf_SUBSET_LABELS_INDEXED_FLAG);
    }

    public void setSubsetLabelsIndexed(boolean rowLabelsIndexed)
    {
        set(sf_SUBSET_LABELS_INDEXED_FLAG, rowLabelsIndexed);
    }

    public boolean isPersistant()
    {
        return isSet(sf_IS_TABLE_PERSISTANT_FLAG);
    }
    
    public void setPersistant(boolean persistant)
    {
        set(sf_IS_TABLE_PERSISTANT_FLAG, persistant);
    }
    
    public TokenMapper getTokenMapper()
    {
        return m_tokenMapper;
    }

    protected void setTokenMapper(TokenMapper tm)
    {
        if (tm == null)
            tm = TokenMapper.fetchTokenMapper(this);
        
        m_tokenMapper = tm;
    }
    
    public int getRowCapacityIncr()
    {
        return m_rowCapacityIncr;
    }

    public void setRowCapacityIncr(int rowCapacityIncr)
    {
        if (rowCapacityIncr <= 0) {
            if (this.isDefault()) 
                m_rowCapacityIncr = sf_ROW_CAPACITY_INCR_DEFAULT;
            else
                m_rowCapacityIncr = ContextImpl.getDefaultContext().getRowCapacityIncr();
        }
        else
            m_rowCapacityIncr = rowCapacityIncr;
    }

    public int getColumnCapacityIncr()
    {
        return m_columnCapacityIncr;
    }

    public void setColumnCapacityIncr(int columnCapacityIncr)
    {
        if (columnCapacityIncr <= 0) {
            if (this.isDefault()) 
                m_columnCapacityIncr = sf_COLUMN_CAPACITY_INCR_DEFAULT;
            else
                m_columnCapacityIncr = ContextImpl.getDefaultContext().getColumnCapacityIncr();
        }
        else
            m_columnCapacityIncr = columnCapacityIncr;
    }

    public int getPrecision()
    {
        return m_precision;
    }

    public void setPrecision(int precision)
    {
        if (precision <= 0) {
            if (this.isDefault()) 
                m_precision = Derivation.sf_DEFAULT_PRECISION;
            else
                m_precision = ContextImpl.getDefaultContext().getPrecision();
        }
        else
            m_precision = precision;
    }

    public int getPendingCorePoolSize()
    {
        return m_pendingCorePoolThreads;
    }

    public void setPendingCorePoolSize(int corePoolSize)
    {
        if (corePoolSize < 0) {
            if (this.isDefault()) 
                m_pendingCorePoolThreads = sf_PENDING_CORE_POOL_SIZE_DEFAULT;
            else
                m_pendingCorePoolThreads = ContextImpl.getDefaultContext().getPendingCorePoolSize();
        }
        else
            m_pendingCorePoolThreads = corePoolSize;        
        
        if (m_pendingThreadPool != null && m_pendingCorePoolThreads <= m_pendingMaxPoolThreads)
            m_pendingThreadPool.setCorePoolSize(m_pendingCorePoolThreads);
    }

    public int getPendingMaximumPoolSize()
    {
        return m_pendingMaxPoolThreads;
    }

    public void setPendingMaximumPoolSize(int maxPoolSize)
    {
        if (maxPoolSize < 1) {
            if (this.isDefault()) 
                m_pendingMaxPoolThreads = sf_PENDING_MAX_POOL_SIZE_DEFAULT;
            else
                m_pendingMaxPoolThreads = ContextImpl.getDefaultContext().getPendingMaximumPoolSize();
        }
        else
            m_pendingMaxPoolThreads = maxPoolSize;
        
        if (m_pendingThreadPool != null && m_pendingMaxPoolThreads >= m_pendingCorePoolThreads)
            m_pendingThreadPool.setMaximumPoolSize(m_pendingMaxPoolThreads);
    }
    
    public long getPendingKeepAliveTime(TimeUnit unit)
    {
        if (unit == null)
            unit = TimeUnit.SECONDS;
        return m_pendingKeepAliveTimeUnit.convert(m_pendingKeepAliveTimeout, unit);
    }

    public void setPendingKeepAliveTime(long time, TimeUnit unit)
    {
        if (unit == null)
            unit = TimeUnit.SECONDS;
        
        if (time <= 0) {
            if (this.isDefault()) {
                m_pendingKeepAliveTimeout = sf_PENDING_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                m_pendingKeepAliveTimeUnit = TimeUnit.SECONDS;
            }
            else {
                m_pendingKeepAliveTimeout = ContextImpl.getDefaultContext().getPendingKeepAliveTime(unit);
                m_pendingKeepAliveTimeUnit = unit;
            }
        }
        else {
            m_pendingKeepAliveTimeout = time;
            m_pendingKeepAliveTimeUnit = unit;
        }
        
        if (m_pendingThreadPool != null)
            m_pendingThreadPool.setKeepAliveTime(m_pendingKeepAliveTimeout, m_pendingKeepAliveTimeUnit);
    }

    public boolean pendingAllowsCoreThreadTimeOut()
    {
        return isSet(sf_PENDINGS_ALLOW_CORE_THREAD_TIMEOUT_FLAG);
    }

    public void pendingAllowCoreThreadTimeOut(boolean allowCoreThreadTimeout)
    {
        set(sf_PENDINGS_ALLOW_CORE_THREAD_TIMEOUT_FLAG, allowCoreThreadTimeout);
    }

    @Override
    public void submitCalculation(UUID transactionId, Runnable r)
    {
        synchronized(this) {
            if (m_pendingThreadPool == null) {
                int maxPoolSize = Math.max(getPendingMaximumPoolSize(), getPendingCorePoolSize());
                TimeUnit unit = m_pendingKeepAliveTimeUnit != null ? m_pendingKeepAliveTimeUnit : TimeUnit.SECONDS;
                m_pendingThreadPool = new PendingDerivationExecutor(getPendingCorePoolSize(),
                                                                    maxPoolSize,
                                                                    getPendingKeepAliveTime(unit),
                                                                    unit,
                                                                    this.pendingAllowsCoreThreadTimeOut());  
            }
        }
        
        m_pendingThreadPool.submitCalculation(transactionId, r);       
    }

    @Override
    public void shutdownDerivableThreadPool()
    {
        if (m_pendingThreadPool != null)
            m_pendingThreadPool.shutdownDerivableThreadPool();
    }
    
    @Override
    public boolean remove(UUID r)
    {
        if (r != null && m_pendingThreadPool != null)
            return m_pendingThreadPool.remove(r);  
        else
            return false;
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
            if (this.isDefault()) 
                m_eventsCorePoolThreads = sf_EVENTS_CORE_POOL_SIZE_DEFAULT;
            else
                m_eventsCorePoolThreads = getDefaultContext().getEventsCorePoolSize();
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
            if (this.isDefault()) 
                m_eventsMaxPoolThreads = sf_EVENTS_MAX_POOL_SIZE_DEFAULT;
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
            if (this.isDefault()) {
                m_eventsKeepAliveTimeout = sf_PENDING_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                m_eventsKeepAliveTimeUnit = TimeUnit.SECONDS;
            }
            else {
                m_eventsKeepAliveTimeout = ContextImpl.getDefaultContext().getEventsKeepAliveTime(unit);
                m_eventsKeepAliveTimeUnit = unit;
            }
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
    
    synchronized protected ContextImpl register(TableImpl table)
    {
        // register the table with this context
        if (table != null) {
            if (table.isPersistant())
                registerPersistant(table);
            else
                registerNonpersistant(table);
        }
        
        return this;
    }
    
    synchronized protected void deregister(TableImpl table)
    {
        if (table != null) {
            m_registeredNonpersistantTables.remove(table);
            m_registeredPersistantTables.remove(table);
        }
    }
    
    synchronized protected boolean isRegistered(Table t)
    {
        return m_registeredNonpersistantTables.contains(t) || m_registeredPersistantTables.contains(t);
    }

    synchronized protected void registerPersistant(TableImpl t)
    {
        if (t != null) {
            m_registeredNonpersistantTables.remove(t);
            m_registeredPersistantTables.add(t);            
        }        
    }

    synchronized protected void registerNonpersistant(TableImpl t)
    {
        if (t != null) {
            m_registeredPersistantTables.remove(t);            
            m_registeredNonpersistantTables.add(t);
        }        
    }
    
    @Override
    public TableImpl getTable(Access mode, Object... mda)
    {
        Object md = null;
        switch (mode) {
            case ByLabel:
            case ByDescription:
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof String))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Table, mode, (md == null ? "<null>" : md.toString())));
                return (TableImpl)find(allTables(), mode == Access.ByLabel ? TableProperty.Label : TableProperty.Description, md);

            case ByProperty:
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (key == null || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Table, mode, (key == null ? "<null>" : key.toString()))); 
                
                // key must either be a table property or a string
                if (key instanceof TableProperty) 
                    return (TableImpl)find(allTables(), (TableProperty)key, value);
                else if (key instanceof String) 
                    return (TableImpl)find(allTables(), (String)key, value);
                else
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Table, mode, (key == null ? "<null>" : key.toString())));                 

            case ByReference:
            {
                md = mda != null && mda.length > 0 ? mda[0] : null;
                if (md == null || !(md instanceof TableImpl) || (((TableImpl)md).getElementType() != ElementType.Table))
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Table, mode, (md == null ? "<null>" : md.toString())));               

                vetElement((TableImpl)md);
                return (TableImpl)md;
            }
            
            default:
                throw new InvalidAccessException(ElementType.Context, ElementType.Table, mode, false, mda);                
        }
    }

    private Collection<TableImpl> allTables()
    {
        Set<TableImpl> allTables = new HashSet<TableImpl>(m_registeredPersistantTables);
        allTables.addAll(m_registeredNonpersistantTables);
        
        return allTables;
    }
}
