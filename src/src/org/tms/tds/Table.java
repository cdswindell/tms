package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Table extends TableElement
{
    private boolean m_dirty;
    
    private Cell [] m_cells;
    private Row [] m_rows;
    private Column [] m_cols;
    
    private Context m_context;
    
    private int m_rowAllocIncr;
    private int m_columnAllocIncr;
    
    private int m_numAllocRows;
    private int m_numAllocCols;
    
    private int m_nextRowIdx;
    private int m_nextColIdx;
    
    public Table(int nRows, int nCols, Context c)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(c);        
        
        initialize(nRows, nCols);
    }

    @Override
    protected void reset()
    {
        m_cells = null;
        m_rows = null;
        m_cols = null;
        
        m_rowAllocIncr = m_columnAllocIncr = 0;      
        m_numAllocRows = m_numAllocCols = 0;
        m_nextRowIdx = m_nextColIdx = 0;
        
        setIndex(-1);
        setTable(this);
        setContext(null);
        setDirty(false);
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
    }
    
    /**
     * @param nRows
     * @param nCols
     */
    private void initialize(int nRows, int nCols)
    {
        m_numAllocRows = getNumAllocRows(nRows);
        m_numAllocCols = getNumAllocColumns(nCols);
        m_nextRowIdx = m_nextColIdx = 0;
        
        m_cells = new Cell[m_numAllocRows * m_numAllocCols];
        m_rows = new Row[m_numAllocRows];
        m_cols = new Column[m_numAllocCols];
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
                
            case NumAllocRows:
                return getNumAllocRows();
                
            default:
                return super.getProperty(key);
        }
    }
    
    private void setContext(Context c)
    {
        m_context = c;
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

    public boolean isDirty()
    {
        return m_dirty;
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

}
