package org.tms.tds.dbms;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
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
    public int getNumCells()
    {
        return getTable().getNumDatabaseColumns() + super.getNumCells();
    }
    
    @Override
    protected CellImpl getCell(RowImpl row, boolean setCurrent)
    {
        if (row != null && row instanceof DbmsRowImpl) {
            DbmsCellImpl cell = new DbmsCellImpl((DbmsRowImpl)row, this);
            if (setCurrent) {
                if (row != null)
                    row.setCurrent();
                this.setCurrent();
            }
            
            return cell;
        }
        else
            return super.getCell(row, setCurrent);
    }
    
    @Override
    public Iterable<Cell> cells()
    {
        vetElement();
        return new DbmsColumnCellIterable();
    }
    
    @Override
    public Derivable setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database column");
    }
    
    /**
     * Iterator to produce a column's table cells in row order. Rows and
     * cells are created, as needed, if they do not already exist.
     */
    protected class DbmsColumnCellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private int m_index;
        private int m_numRows;
        private DbmsColumnImpl m_col;
        private DbmsTableImpl m_table;
        
        public DbmsColumnCellIterable()
        {
            m_col = DbmsColumnImpl.this;
            m_table = m_col.getTable();
            m_index = 1;
            m_numRows = m_table != null ? m_table.getNumRows() : 0;
        }

        @Override
        public Iterator<Cell> iterator()
        {
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return m_index <= m_numRows;
        }

        @Override
        public CellImpl next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            CellImpl c = null;
            RowImpl row = m_table.getRowInternal(true, false, Access.ByIndex, m_index++);
            if (row instanceof DbmsRowImpl)
                c = new DbmsCellImpl((DbmsRowImpl)row, m_col);
            else
                c = m_col.getCellInternal(row, true, false);

            return c;
        }       
    }
}
