package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Table extends TableElement
{
    private Cell [] m_cells;
    private Row [] m_rows;
    private Column [] m_cols;
    
    private Context m_context;
    
    private int m_nAllocRows;
    private int m_nAllocCols;
    
    public Table(int nRows, int nCols, Context c)
    {
        super(ElementType.Table, null);
        setTable(this);
        setContext(c);        
        
        m_nAllocRows = nRows;
        m_nAllocCols = nCols;
        
        m_cells = new Cell[m_nAllocRows * m_nAllocCols];
        m_rows = new Row[m_nAllocRows];
    }
    
    @Override
    protected void reset()
    {
        m_cells = null;
        m_rows = null;
        m_cols = null;
        
        m_nAllocRows = m_nAllocCols = 0;
        
        setIndex(-1);
        setTable(this);
        setContext(null);
        
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
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
}
