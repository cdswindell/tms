package org.tms.tds;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.util.WeakHashSet;

public class Table extends TableElement
{
    private boolean m_dirty;
    
    private Cell [] m_cells;
    private Row [] m_rows;
    private Column [] m_cols;
    
    private Set<Range> m_ranges;
    
    private Context m_context;
    
    private int m_numAllocRows;
    private int m_numAllocCols;
    
    private int m_nextRowIdx;
    private int m_nextColIdx;
    
    //Initialized from context or source table
    private int m_rowAllocIncr;
    private int m_columnAllocIncr;
    
    public Table(int nRows, int nCols)
    {
        this(nRows, nCols, Context.getDefaultContext());
    }

    protected Table(int nRows, int nCols, Context c)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(c);        
        
        initialize(nRows, nCols, null);
    }

    protected Table(int nRows, int nCols, Table t)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(t != null ? t.getContext() : null);        
        
        initialize(nRows, nCols, t);
    }
    
    private void initialize(int nRows, int nCols, Table t)
    {
        initializeProperties(t);
        
        m_numAllocRows = getNumAllocRows(nRows);
        m_numAllocCols = getNumAllocColumns(nCols);
        m_nextRowIdx = m_nextColIdx = 0;
        
        // allocate base memory for rows and columns
        m_rows = new Row[m_numAllocRows];
        m_cols = new Column[m_numAllocCols];
                
        // set all other arrays/sets/maps to null
        m_cells = null;
        m_ranges = null;
        
        // set dirty flag, as table structure has changed
        setDirty(true);
    }

    @Override
    protected void initialize(TableElement e) 
    {
        // noop for tables, initialization happens slightly differently
    }
    
    protected void initializeProperties(Table e)
    {
        BaseElement source = getInitializationSource(e);
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {
                case RowAllocIncr:
                    if (!isValidPropertyValueInt(value))
                        value = Context.sf_ROW_ALLOC_INCR_DEFAULT;
                    setRowAllocIncr((int)value);
                    break;
                    
                case ColumnAllocIncr:
                    if (!isValidPropertyValueInt(value))
                        value = Context.sf_COLUMN_ALLOC_INCR_DEFAULT;
                    setColumnAllocIncr((int)value);
                    break;

                default:
                    throw new IllegalStateException("No initialization available for Table Property: " + tp);                       
            }
        }
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case RowAllocIncr:
                return getRowAllocIncr();
                
            case ColumnAllocIncr:
                return getColumnAllocIncr();
                
            case numRowsAlloc:
                return getNumAllocRows();
                
            case numColumnsAlloc:
                return getNumAllocColumns();
                
            case numRanges:
                return getRangesField(FieldAccess.ReturnEmptyIfNull).size();
                
            default:
                return super.getProperty(key);
        }
    }
    
    private Context setContext(Context c)
    {
        if (c == null)
            c = Context.getDefaultContext();
        
        m_context = c.register(this);
        
        return m_context;
    }
    
    void clearContext() 
    {
        if (getContext() != null)
            getContext().unregister(this);
        m_context = null;
    }
    
    @Override
    protected Context getContext()
    {
        return m_context;
    }

    @Override
    protected Table getTable()
    {
        return this;
    }

    protected boolean isDirty()
    {
        return m_dirty;
    }

    
    private Set<Range> getRangesField(FieldAccess... fas)
    {
        FieldAccess fa = FieldAccess.checkAccess(fas);
        if (m_ranges == null) {
            if (fa == FieldAccess.CreateIfNull)
                m_ranges = new WeakHashSet<Range>();
            else
                return Collections.emptySet();
        }
        
        return m_ranges;
    }
    
    protected boolean add(Range r)
    {
        vetParent(r);
        return getRangesField().add(r);
    }
    
    protected boolean remove(Range r)
    {
        vetParent(r);
        return getRangesField().remove(r);
    }
    
    void setDirty(boolean dirty)
    {
        m_dirty = dirty;
    }

    synchronized protected int getRowAllocIncr()
    {
        if (m_rowAllocIncr <= 0) 
            m_rowAllocIncr = Context.getPropertyInt(getContext(), TableProperty.RowAllocIncr);
        
        return m_rowAllocIncr;
    }

    protected void setRowAllocIncr(int rowAllocIncr)
    {
        if (rowAllocIncr <= 0) {
            synchronized(this) 
            {
                // force a reset of the row alloc value
                m_rowAllocIncr = 0;
                getRowAllocIncr();
            }           
        }
        else
            m_rowAllocIncr = rowAllocIncr;
    }

    synchronized protected int getColumnAllocIncr()
    {
        if (m_columnAllocIncr <= 0) 
            m_columnAllocIncr = Context.getPropertyInt(getContext(), TableProperty.ColumnAllocIncr);
        
        return m_columnAllocIncr;
    }

    protected void setColumnAllocIncr(int colAllocIncr)
    {
        if (colAllocIncr <= 0) {
            synchronized(this) 
            {
                // force a reset of the row alloc value
                colAllocIncr = 0;
                getColumnAllocIncr();
            }           
        }
        else
            m_columnAllocIncr = colAllocIncr;
    }
    
    protected int getNumAllocRows()
    {
        return m_numAllocRows;
    }
    
    private int getNumAllocRows(int nRows)
    {
        int rowAlloc = getRowAllocIncr();
        if (nRows > 0) {
            int remainder = nRows % rowAlloc;
            rowAlloc = nRows + (remainder > 0 ? rowAlloc - remainder : 0);
        }
        
        return rowAlloc;
    }
    
    private int getNumAllocColumns(int nCols)
    {
        int colAlloc = getColumnAllocIncr();
        if (nCols > 0) {
            int remainder = nCols % colAlloc;
            colAlloc = nCols + (colAlloc - remainder);
        }
        
        return colAlloc;
    }

    protected int getNumAllocColumns()
    {
        return m_numAllocCols;
    }

    protected boolean add(Row r, Access access)
    {
        // allocate new row, if specified row is null
        if (r == null)
            r = new Row(this);
        
        return true;
    }
    
    /**
     * Empty tables contain no defined (set) cells
     */
    @Override
    protected boolean isEmpty()
    {
        if (m_cols == null || m_rows == null || m_cells == null)
            return true;
        else
            return false;
    }  
}
