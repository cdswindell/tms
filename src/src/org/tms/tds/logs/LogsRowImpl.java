package org.tms.tds.logs;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;

public class LogsRowImpl extends RowImpl implements ExternalDependenceTableElement
{
    private int m_lineNumber;
    private boolean m_rowProcessed;
    
    public LogsRowImpl(LogsTableImpl parentTable, int lineNo)
    {
        super(parentTable);
        m_rowProcessed = false;
        m_lineNumber = lineNo;
    }

    protected boolean isEntryProcessed()
    {
        return m_rowProcessed;
    }
    
    void setEntryProcessed(boolean processed)
    {
        m_rowProcessed = processed;
        if (processed)
            getTable().removeLogsRowFromUnprocessed(this);
    }
    
    /**
     * Return the 0-based log file line number associated with this row 
     * @return the 0-based log file line number associated with this row 
     */
    protected int getLineNumber()
    {
        return m_lineNumber;
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
