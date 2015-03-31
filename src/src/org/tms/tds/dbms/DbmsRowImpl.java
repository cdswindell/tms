package org.tms.tds.dbms;

import org.tms.api.derivables.Derivable;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.RowImpl;

public class DbmsRowImpl extends RowImpl
{
    private int m_resultSetIndex;
    
    public DbmsRowImpl(DbmsTableImpl parentTable, int rsIndex)
    {
        super(parentTable);
        m_resultSetIndex = rsIndex;
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
    public DbmsTableImpl getTable() 
    {
        return (DbmsTableImpl)super.getTable();
    }

    @Override
    public Derivable setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database row");
    }
}
