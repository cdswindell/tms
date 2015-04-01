package org.tms.tds.dbms;

import org.tms.api.derivables.Derivable;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
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
    public int getNumCells()
    {
        return getTable().getNumDatabaseRows() + super.getNumCells();
    }
    
    @Override
    protected CellImpl getCell(ColumnImpl col, boolean setCurrent)
    {
        if (col != null && col instanceof DbmsColumnImpl) {
            DbmsCellImpl cell = new DbmsCellImpl(this, (DbmsColumnImpl)col);
            if (setCurrent) {
                if (col != null)
                    col.setCurrent();
                this.setCurrent();
            }
            
            return cell;
        }
        else
            return super.getCell(col, setCurrent);
    }
    
    @Override
    public Derivable setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database row");
    }
}
