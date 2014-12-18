package org.tms.tds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.util.WeakHashSet;

public class ContextImpl extends BaseElementImpl implements TableContext
{
    private static ContextImpl sf_DEFAULT_CONTEXT;
    
    static final int sf_ROW_CAPACITY_INCR_DEFAULT = 10;
    static final int sf_COLUMN_CAPACITY_INCR_DEFAULT = 10;
    
    static final boolean sf_READ_ONLY_DEFAULT = false;
    static final boolean sf_SUPPORTS_NULL_DEFAULT = true;
    static final boolean sf_ENFORCE_DATA_TYPE_DEFAULT = false;
    
    static final Map<TableProperty, Object> sf_PROPERTY_DEFAULTS = new HashMap<TableProperty, Object>();
    
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
            sf_DEFAULT_CONTEXT.setLabel("Default");
        }
            
        return sf_DEFAULT_CONTEXT;
    }

    private Set<Table> m_registeredTables;
    private boolean m_default;
    private boolean m_enforceDataType;
    
    private int m_rowCapacityIncr;
    private int m_columnCapacityIncr;
    
    private ContextImpl(boolean isDefault, ContextImpl otherContext)
    {
        super(ElementType.Context);      
        m_default = isDefault;
        m_registeredTables = new WeakHashSet<Table>();
        
        // initialize from default context, unless this the default
        initialize(otherContext);
     }

    protected ContextImpl()
    {
        this(false, null);
    }
    
    protected ContextImpl(ContextImpl otherContext)
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
                    
                case isEnforceDataType:
                    if (!isValidPropertyValueInt(value))
                        value = sf_ENFORCE_DATA_TYPE_DEFAULT;
                    setEnforceDataType((boolean)value);
                    break;
                    
                default:
                    throw new IllegalStateException("No initialization available for Context Property: " + tp);                       
            }
        }
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
    }

    private Object getPropertyDefault(TableProperty tp)
    {
        // TODO Auto-generated method stub
        return null;
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
                
            case isEnforceDataType:
                return isEnforceDataType();
                
            default:
                return super.getProperty(key);
        }        
    }

    protected boolean isDefault()
    {
        return m_default;
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

    protected boolean isEnforceDataType()
    {
        return m_enforceDataType;
    }

    protected void setEnforceDataType(boolean enforceDataType)
    {
        m_enforceDataType = enforceDataType;
    }

    protected ContextImpl register(Table table)
    {
        // register the table with this context
        m_registeredTables.add(table);
        return this;
    }
    
    protected void unregister(TableImpl table)
    {
        if (table != null) 
            m_registeredTables.remove(table);
    }
    
    protected boolean isRegistered(Table t)
    {
        return m_registeredTables.contains(t);
    }

    @Override
    protected boolean isEmpty()
    {
         return m_registeredTables.isEmpty();
    }
}
