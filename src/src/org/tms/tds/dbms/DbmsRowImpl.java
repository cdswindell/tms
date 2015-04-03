package org.tms.tds.dbms;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivable;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.RowImpl;

public class DbmsRowImpl extends RowImpl
{
    private int m_resultSetIndex;
    private boolean m_resultSetRowProcessed;
    
    public DbmsRowImpl(DbmsTableImpl parentTable, int rsIndex)
    {
        super(parentTable);
        m_resultSetRowProcessed = false;
        m_resultSetIndex = rsIndex;
    }

    protected boolean isResultSetRowProcessed()
    {
        return m_resultSetRowProcessed;
    }
    
    void setResultSetRowProcessed(boolean processed)
    {
        m_resultSetRowProcessed = processed;
        if (processed)
            getTable().removeDbmsRowFromUnprocessed(this);
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
        getTable().removeDbmsRowFromUnprocessed(this);
        getTable().decrementNumDbmsRows();
        
        super.delete(compress);
    }
    
    @Override
    public DbmsTableImpl getTable() 
    {
        return (DbmsTableImpl)super.getTable();
    }
   
    @Override
    protected int getCellOffset()
    {
        return super.getCellOffset();
    }

    @Override
    public Derivable setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database row");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a database element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
}
