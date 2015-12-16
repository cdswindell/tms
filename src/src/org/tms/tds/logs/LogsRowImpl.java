package org.tms.tds.logs;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.RowImpl;

public class LogsRowImpl extends RowImpl
{
    private int m_resultSetIndex;
    private boolean m_resultSetRowProcessed;
    
    public LogsRowImpl(LogsTableImpl parentTable, int rsIndex)
    {
        super(parentTable);
        m_resultSetRowProcessed = false;
        m_resultSetIndex = rsIndex;
    }

    protected boolean isEntryProcessed()
    {
        return m_resultSetRowProcessed;
    }
    
    void setEntryProcessed(boolean processed)
    {
        m_resultSetRowProcessed = processed;
        if (processed)
            getTable().removeLogsRowFromUnprocessed(this);
    }
    
    /**
     * Return the 1-based index of the row in the dbms table result set
     * @return the 1-based index of the row in the dbms table result set
     */
    protected int getResultSetIndex()
    {
        return m_resultSetIndex;
    }
    
    @Override
    protected void delete(boolean compress)
    {
        getTable().removeLogsRowFromUnprocessed(this);
        getTable().decrementNumLogsRows();
        
        super.delete(compress);
    }
    
    @Override
    public LogsTableImpl getTable() 
    {
        return (LogsTableImpl)super.getTable();
    }
   
    @Override
    protected int getCellOffset()
    {
        return super.getCellOffset();
    }

    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a log file on a database row");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a log file element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
}
