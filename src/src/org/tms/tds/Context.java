package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.UnimplementedException;

public class Context extends BaseElement
{
    private static Context sf_DEFAULT_CONTEXT;
    
    private static final int sf_ROW_ALLOC_INCR_DEFAULT = 10;
    private static final int sf_COLUMN_ALLOC_INCR_DEFAULT = 10;

    protected static int getPropertyInt(Context c, TableProperty key)
    {
        if (c != null)
            return c.getPropertyInt(key);
        else
            return getDefaultContext().getPropertyInt(key);
    }

    synchronized protected static Context getDefaultContext()
    {
        if (sf_DEFAULT_CONTEXT == null) {
            sf_DEFAULT_CONTEXT = new Context(true);
        }
            
        return sf_DEFAULT_CONTEXT;
    }

    private boolean m_default;
    private int m_rowAllocIncr;
    private int m_columnAllocIncr;
    
    private Context(boolean isDefault)
    {
        super(ElementType.Context);      
        m_default = isDefault;
        
        // initialize from default context, unless this the default
        initialize();
     }

    protected Context()
    {
        this(false);
    }
    
    protected void initialize()
    {
        Context defaultContext = isDefault() ? this : Context.getDefaultContext();
        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = null;
            if (isDefault())
                value = getPropertyDefault(tp);
            else
                value = defaultContext.getProperty(tp);
            
            // set the corresponding value
            switch (tp)
            {
                case RowAllocIncr:
                    if (value != null && value instanceof Integer && ((int)value) > 0)
                        setRowAllocIncr((int)value);
                    else 
                        setRowAllocIncr(sf_ROW_ALLOC_INCR_DEFAULT);
                    break;
                case ColumnAllocIncr:
                    if (value != null && value instanceof Integer && ((int)value) > 0)
                        setColumnAllocIncr((int)value);
                    else 
                        setColumnAllocIncr(sf_COLUMN_ALLOC_INCR_DEFAULT);
                    break;
                default:
                    throw new IllegalStateException("No initialization available for Context Property: " + tp);                       
            }
        }
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

    
    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case RowAllocIncr:
                return getRowAllocIncr();
                
            case ColumnAllocIncr:
                return getColumnAllocIncr();
                
            default:
                return super.getProperty(key);
        }        
    }

    public boolean isDefault()
    {
        return m_default;
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
}
