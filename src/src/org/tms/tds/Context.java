package org.tms.tds;

import java.util.HashMap;
import java.util.Map;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;

public class Context extends BaseElement
{
    private static Context sf_DEFAULT_CONTEXT;
    
    static final int sf_ROW_ALLOC_INCR_DEFAULT = 10;
    static final int sf_COLUMN_ALLOC_INCR_DEFAULT = 10;
    
    static final boolean sf_READ_ONLY_DEFAULT = false;
    static final boolean sf_SUPPORTS_NULL_DEFAULT = true;
    
    static final Map<TableProperty, Object> sf_PROPERTY_DEFAULTS = new HashMap<TableProperty, Object>();
    
    protected static int getPropertyInt(Context c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyInt(key);
        else
            return getDefaultContext().getPropertyInt(key);
    }

    protected static boolean getPropertyBoolean(Context c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyBoolean(key);
        else
            return getDefaultContext().getPropertyBoolean(key);
    }

    synchronized protected static Context getDefaultContext()
    {
        if (sf_DEFAULT_CONTEXT == null) {
            sf_DEFAULT_CONTEXT = new Context(true, null);
        }
            
        return sf_DEFAULT_CONTEXT;
    }

    private boolean m_default;
    
    private boolean m_readOnly;
    private boolean m_supportsNull;
    
    private int m_rowAllocIncr;
    private int m_columnAllocIncr;
    
    private Context(boolean isDefault, Context otherContext)
    {
        super(ElementType.Context);      
        m_default = isDefault;
        
        // initialize from default context, unless this the default
        initialize(otherContext);
     }

    protected Context()
    {
        this(false, null);
    }
    
    protected Context(Context otherContext)
    {
        this(false, otherContext);
    }
    
    protected void initialize()
    {
        initialize(Context.getDefaultContext());
    }
    
    protected void initialize(Context otherContext)
    {
        Context sourceContext = isDefault() ? otherContext : (otherContext != null ? otherContext : Context.getDefaultContext());
        if (this == sourceContext)
            return; // nothing to do
        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = null;
            if (isDefault() && otherContext == null)
                value = getPropertyDefault(tp);
            else
                value = sourceContext.getProperty(tp);
            
            // set the corresponding value
            switch (tp)
            {
                case RowAllocIncr:
                    if (isValidPropertyValueInt(value))
                        setRowAllocIncr((int)value);
                    else 
                        setRowAllocIncr(sf_ROW_ALLOC_INCR_DEFAULT);
                    break;
                    
                case ColumnAllocIncr:
                    if (isValidPropertyValueInt(value))
                        setColumnAllocIncr((int)value);
                    else 
                        setColumnAllocIncr(sf_COLUMN_ALLOC_INCR_DEFAULT);
                    break;
                    
                case ReadOnly:
                    if (isValidPropertyValueBoolean(value))
                        setReadOnly((boolean)value);
                    else 
                        setReadOnly(sf_READ_ONLY_DEFAULT);
                    break;
                    
                case SupportsNull:
                    if (isValidPropertyValueBoolean(value))
                        setSupportsNull((boolean)value);
                    else 
                        setReadOnly(sf_SUPPORTS_NULL_DEFAULT);
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

    protected int getPropertyInt(TableProperty key)
    {
        switch (key)
        {
            case RowAllocIncr:
            case ColumnAllocIncr:
                return (int)getProperty(key);
                
            default:
                throw new UnimplementedException(this, key);
        }
    }

    protected boolean getPropertyBoolean(TableProperty key)
    {
        switch (key)
        {
            case ReadOnly:
            case SupportsNull:
                return (boolean)getProperty(key);
                
            default:
                throw new UnimplementedException(this, key);
        }
    }
  
    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case RowAllocIncr:
                return getRowAllocIncr();
                
            case ColumnAllocIncr:
                return getColumnAllocIncr();
                
            case ReadOnly:
                return isReadOnly();
                
            case SupportsNull:
                return isSupportsNull();
                
            default:
                return super.getProperty(key);
        }        
    }

    protected boolean isDefault()
    {
        return m_default;
    }

    protected boolean isReadOnly()
    {
        return m_readOnly;
    }

    protected void setReadOnly(boolean readOnly)
    {
        m_readOnly = readOnly;
    }

    protected boolean isSupportsNull()
    {
        return m_supportsNull;
    }

    protected void setSupportsNull(boolean supportsNull)
    {
        m_supportsNull = supportsNull;
    }

    protected int getRowAllocIncr()
    {
        return m_rowAllocIncr;
    }

    protected void setRowAllocIncr(int rowAllocIncr)
    {
        if (rowAllocIncr <= 0) {
            if (this.isDefault()) 
                m_rowAllocIncr = sf_ROW_ALLOC_INCR_DEFAULT;
            else
                m_rowAllocIncr = Context.getDefaultContext().getRowAllocIncr();
        }
        else
            m_rowAllocIncr = rowAllocIncr;
    }

    protected int getColumnAllocIncr()
    {
        return m_columnAllocIncr;
    }

    protected void setColumnAllocIncr(int columnAllocIncr)
    {
        if (columnAllocIncr <= 0) {
            if (this.isDefault()) 
                m_columnAllocIncr = sf_COLUMN_ALLOC_INCR_DEFAULT;
            else
                m_columnAllocIncr = Context.getDefaultContext().getColumnAllocIncr();
        }
        else
            m_columnAllocIncr = columnAllocIncr;
    }

    public Context register(Table table)
    {
        // TODO Auto-generated method stub
        return this;
    }
}
