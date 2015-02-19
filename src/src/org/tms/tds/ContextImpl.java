package org.tms.tds;

import java.util.HashMap;
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
import org.tms.api.exceptions.InvalidAccessException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.teq.Derivation;
import org.tms.util.WeakHashSet;

public class ContextImpl extends BaseElementImpl implements TableContext, DerivableThreadPool
{
    private static ContextImpl sf_DEFAULT_CONTEXT;
    
    static final int sf_ROW_CAPACITY_INCR_DEFAULT = 1024;
    static final int sf_COLUMN_CAPACITY_INCR_DEFAULT = 32;
    static final double sf_FREE_SPACE_THRESHOLD_DEFAULT = 2.0;

    static final int sf_CORE_POOL_SIZE_DEFAULT = 8;
    static final int sf_MAX_POOL_SIZE_DEFAULT = 128;
    static final int sf_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT = 15;
    static final boolean sf_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT = true;
    
    static final boolean sf_READ_ONLY_DEFAULT = false;
    static final boolean sf_SUPPORTS_NULL_DEFAULT = true;
    static final boolean sf_ENFORCE_DATA_TYPE_DEFAULT = false;
    static final boolean sf_AUTO_RECALCULATE_DEFAULT = true;
    
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

    private Set<Table> m_registeredTables;
    
    private int m_rowCapacityIncr;
    private int m_columnCapacityIncr;
    private TokenMapper m_tokenMapper;
    private int m_precision;
    private double m_freeSpaceThreshold;

    private int m_corePoolThreads;
    private int m_maxPoolThreads;
    private long m_keepAliveTimeout;
    private boolean m_allowCoreThreadTimeout;
    private PendingDerivationExecutor m_pendingThreadPool;
    
    private ContextImpl(boolean isDefault, TableContext otherContext)
    {
        super();      
        set(sf_IS_DEFAULT_FLAG, isDefault);
        m_registeredTables = new WeakHashSet<Table>();
        
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
                    
                case TokenMapper:
                    if (value == null)
                        value = TokenMapper.fetchTokenMapper(this);
                    else 
                        value = TokenMapper.cloneTokenMapper((TokenMapper)value, this);
                    setTokenMapper((TokenMapper)value);
                    break;
                    
                case isAllowCoreThreadTimeout:
                    if (!isValidPropertyValueBoolean(value))
                        value = sf_ALLOW_CORE_THREAD_TIMEOUT_DEFAULT;
                    allowCoreThreadTimeOut((boolean)value);
                    break;                   
                    
                case numCorePoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_CORE_POOL_SIZE_DEFAULT;
                    setCorePoolSize((int)value);
                    break;
                    
                case numMaxPoolThreads:
                    if (!isValidPropertyValueInt(value))
                        value = sf_MAX_POOL_SIZE_DEFAULT;
                    setMaximumPoolSize((int)value);
                    break;
                    
                case ThreadKeepAliveTimeout:
                    if (!isValidPropertyValueInt(value))
                        value = sf_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
                    setKeepAliveTime((int)value, TimeUnit.SECONDS);
                    break;
                    
                default:
                    throw new IllegalStateException("No initialization available for Context Property: " + tp);                       
            }
        }
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        m_pendingThreadPool = null;
    }

    protected double getFreeSpaceThreshold()
    {
        return m_freeSpaceThreshold;
    }
    
    protected void setFreeSpaceThreshold(double value)
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
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
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
                
            case TokenMapper:
                return getTokenMapper();
                
            case isAllowCoreThreadTimeout:
                return allowsCoreThreadTimeOut();
                
            case numCorePoolThreads:
                return getCorePoolSize();
                
            case numMaxPoolThreads:
                return getMaximumPoolSize();
                
            case ThreadKeepAliveTimeout:
                return getKeepAliveTime(TimeUnit.SECONDS);
                
            default:
                return super.getProperty(key);
        }        
    }

    @Override
    protected boolean isNull()
    {
         return m_registeredTables.isEmpty();
    }
    
    protected boolean isDefault()
    {
        return isSet(sf_IS_DEFAULT_FLAG);
    }

    public boolean isAutoRecalculate()
    {
        return isSet(sf_AUTO_RECALCULATE_FLAG);
    }
    
    protected void setAutoRecalculate(boolean value)
    {
        set(sf_AUTO_RECALCULATE_FLAG, value);
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
    
    protected int getRowCapacityIncr()
    {
        return m_rowCapacityIncr;
    }

    protected void setRowCapacityIncr(int rowCapacityIncr)
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

    protected int getColumnCapacityIncr()
    {
        return m_columnCapacityIncr;
    }

    protected void setColumnCapacityIncr(int columnCapacityIncr)
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

    protected int getPrecision()
    {
        return m_precision;
    }

    protected void setPrecision(int precision)
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

    @Override
    public int getCorePoolSize()
    {
        return m_corePoolThreads;
    }

    @Override
    public void setCorePoolSize(int corePoolSize)
    {
        if (corePoolSize <= 0) {
            if (this.isDefault()) 
                m_corePoolThreads = sf_CORE_POOL_SIZE_DEFAULT;
            else
                m_corePoolThreads = ContextImpl.getDefaultContext().getCorePoolSize();
        }
        else
            m_corePoolThreads = corePoolSize;        
        
        if (m_pendingThreadPool != null && m_corePoolThreads <= m_maxPoolThreads)
            m_pendingThreadPool.setCorePoolSize(m_corePoolThreads);
    }

    @Override
    public int getMaximumPoolSize()
    {
        return m_maxPoolThreads;
    }

    @Override
    public void setMaximumPoolSize(int maxPoolSize)
    {
        if (maxPoolSize <= 0) {
            if (this.isDefault()) 
                m_maxPoolThreads = sf_MAX_POOL_SIZE_DEFAULT;
            else
                m_maxPoolThreads = ContextImpl.getDefaultContext().getMaximumPoolSize();
        }
        else
            m_maxPoolThreads = maxPoolSize;
        
        if (m_pendingThreadPool != null && m_maxPoolThreads >= m_corePoolThreads)
            m_pendingThreadPool.setMaximumPoolSize(m_maxPoolThreads);
    }

    
    @Override
    public long getKeepAliveTime(TimeUnit unit)
    {
        return m_keepAliveTimeout;
    }

    @Override
    public void setKeepAliveTime(long time, TimeUnit unit)
    {
        if (time <= 0) {
            if (this.isDefault()) 
                m_keepAliveTimeout = sf_KEEP_ALIVE_TIMEOUT_SEC_DEFAULT;
            else
                m_keepAliveTimeout = ContextImpl.getDefaultContext().getKeepAliveTime(TimeUnit.SECONDS);
        }
        else
            m_keepAliveTimeout = time;
        
        if (m_pendingThreadPool != null)
            m_pendingThreadPool.setKeepAliveTime(m_keepAliveTimeout, TimeUnit.SECONDS);
    }

    @Override
    public boolean allowsCoreThreadTimeOut()
    {
        return m_allowCoreThreadTimeout;
    }

    @Override
    public void allowCoreThreadTimeOut(boolean allowCoreThreadTimeout)
    {
        m_allowCoreThreadTimeout = allowCoreThreadTimeout;
    }


    @Override
    public void submitCalculation(UUID transactionId, Runnable r)
    {
        synchronized(this) {
            if (m_pendingThreadPool == null) {
                int maxPoolSize = Math.max(getMaximumPoolSize(), getCorePoolSize());
                m_pendingThreadPool = new PendingDerivationExecutor(getCorePoolSize(),
                                                                    maxPoolSize,
                                                                    getKeepAliveTime(TimeUnit.SECONDS),
                                                                    TimeUnit.SECONDS,
                                                                    this.allowsCoreThreadTimeOut());  
            }
        }
        
        m_pendingThreadPool.submitCalculation(transactionId, r);       
    }

    @Override
    public void shutdown()
    {
        if (m_pendingThreadPool != null)
            m_pendingThreadPool.shutdown();
    }
    
    @Override
    public boolean remove(Runnable r)
    {
        if (r != null && m_pendingThreadPool != null)
            return m_pendingThreadPool.remove(r);  
        else
            return false;
    }
    
    protected ContextImpl register(Table table)
    {
        // register the table with this context
        m_registeredTables.add(table);
        return this;
    }
    
    protected void deregister(TableImpl table)
    {
        if (table != null) 
            m_registeredTables.remove(table);
    }
    
    protected boolean isRegistered(Table t)
    {
        return m_registeredTables.contains(t);
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
                return (TableImpl)find(m_registeredTables, mode == Access.ByLabel ? TableProperty.Label : TableProperty.Description, md);

            case ByProperty:
                Object key = mda != null && mda.length > 0 ? mda[0] : null;
                Object value = mda != null && mda.length > 1 ? mda[1] : null;
                if (key == null || value == null)
                    throw new InvalidException(this.getElementType(), 
                            String.format("Invalid %s %s argument: %s", ElementType.Table, mode, (key == null ? "<null>" : key.toString()))); 
                
                // key must either be a table property or a string
                if (key instanceof TableProperty) 
                    return (TableImpl)find(m_registeredTables, (TableProperty)key, value);
                else if (key instanceof String) 
                    return (TableImpl)find(m_registeredTables, (String)key, value);
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
}
