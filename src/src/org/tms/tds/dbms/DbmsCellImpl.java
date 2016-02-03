package org.tms.tds.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ExternalDependenceTableElement;

public class DbmsCellImpl extends CellImpl implements ExternalDependenceTableElement
{
    private DbmsRowImpl m_row;
    
    protected DbmsCellImpl(DbmsRowImpl row, DbmsColumnImpl col)
    {
        super(col, row.getCellOffset());
        
        m_row = row;
        setReadOnly(true);
    }

    @Override
    public DbmsRowImpl getRow()
    {
        return m_row;
    }
    
    @Override
    public DbmsColumnImpl getColumn()
    {
        return (DbmsColumnImpl)m_col;
    }
    
    @Override
    public DbmsTableImpl getTable() 
    {
        if (m_row != null)
            return m_row.getTable();
        else
            return null;
    }

    @Override
    public Object getCellValue()
    {
        synchronized (m_row) {
            if (!m_row.isResultSetRowProcessed()) {
                try {
                    processResultSetRow(m_row);
                }
                finally {
                    m_row.setResultSetRowProcessed(true);
                }
            }
        }
            
        return m_cellValue;
    }
    
    @SuppressWarnings("resource")
	private void processResultSetRow(DbmsRowImpl row)
    {
        DbmsTableImpl table = row.getTable();
        ResultSet rs = table.getResultSet();
        try
        {
            rs.absolute(row.getResultSetIndex());
            for (Cell cell : row.cells()) {
                if (cell == null || !(cell instanceof DbmsCellImpl)) continue;
                
                Column c = cell.getColumn();
                if (c != null && c instanceof DbmsColumnImpl) 
                    ((DbmsCellImpl)cell).m_cellValue = rs.getObject(((DbmsColumnImpl)c).getResultSetIndex());
            }
        }
        catch (SQLException e)
        {
            throw new TableIOException(e);
        }
    }

    @Override
    public boolean isWriteProtected()
    {
        return true;
    }
    
    @Override 
    public boolean setCellValue(Object value)
    {
        throw new ReadOnlyException(this, TableProperty.CellValue);
    }
    
    @Override 
    public boolean fill(Object value)
    {
        throw new ReadOnlyException(this, TableProperty.CellValue);
    }
    
    @Override 
    public void delete()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot delete a database cell");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database cell");
    }
}
