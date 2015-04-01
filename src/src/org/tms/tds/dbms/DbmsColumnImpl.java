package org.tms.tds.dbms;

import org.tms.api.derivables.Derivable;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;

public class DbmsColumnImpl extends ColumnImpl implements TDSOverrides
{
    private int m_resultSetIndex;

    public DbmsColumnImpl(TableImpl parentTable, int resultSetIndex, String clazzName)
    {
        super(parentTable);
        
        m_resultSetIndex = resultSetIndex;    
        setReadOnly(true);
        try
        {
            if (clazzName != null && (clazzName = clazzName.trim()).length() > 0) {
                setDataType(Class.forName(clazzName));
                setStronglyTyped(true);
            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO throw exception
        }
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
    protected CellImpl createNewCell(RowImpl row)
    {
        if (row instanceof DbmsRowImpl)
            return new DbmsCellImpl((DbmsRowImpl) row, this);
        else
            return super.createNewCell(row);
    }

    @Override
    public Derivable setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database column");
    }
}
