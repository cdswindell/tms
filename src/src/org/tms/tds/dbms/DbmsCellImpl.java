package org.tms.tds.dbms;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.tms.api.TableProperty;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.tds.CellImpl;

public class DbmsCellImpl extends CellImpl
{

    private DbmsRowImpl m_row;
    private Token m_cellValue;
    
    protected DbmsCellImpl(DbmsRowImpl row, DbmsColumnImpl col)
    {
        super(col, -1);
        
        m_row = row;
        setReadOnly(true);
    }

    @Override
    protected boolean isWriteProtected()
    {
        return true;
    }
    
    @Override 
    public boolean setCellValue(Object value)
    {
        throw new ReadOnlyException(this, TableProperty.CellValue);
    }
    
    @Override
    public Object getCellValue()
    {
        if (m_cellValue == null) {
            ResultSet rs = getTable().getResultSet();
            try
            {
                rs.absolute(m_row.getResultSetIndex());
                Object value = rs.getObject(getColumn().getResultSetIndex());
                m_cellValue = new Token(value);
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
            
        return m_cellValue.getValue();
    }
    
    @Override
    public DbmsRowImpl getRow()
    {
        return m_row;
    }
    
    @Override
    public DbmsColumnImpl getColumn()
    {
        return (DbmsColumnImpl)super.getColumn();
    }
    
    @Override
    public DbmsTableImpl getTable() 
    {
        if (m_row != null)
            return m_row.getTable();
        else
            return null;
    }

}
