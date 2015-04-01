package org.tms.tds.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import org.tms.api.Access;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;

public class DbmsTableImpl extends TableImpl
{
    private String m_connectionUrl;
    private String m_query;
    private ResultSet m_resultSet;
    private Statement m_statement;
    private Connection m_connection;
    private int m_numDbmsCols;
    private int m_numDbmsRows;
    
    protected DbmsTableImpl(String connectionUrl, String query)
    throws ClassNotFoundException, SQLException
    {
        this(connectionUrl, query, null, ContextImpl.createDefaultContext());
    }
    
    protected DbmsTableImpl(String connectionUrl, String query, String driverClassName, ContextImpl tc) 
    throws ClassNotFoundException, SQLException
    {
        // initialize the default table object
        super(tc.getRowCapacityIncr(), tc.getColumnCapacityIncr(), tc);
        m_numDbmsCols = m_numDbmsRows = 0;
        
        // load the database driver, this could throw an exception
        tc.loadDatabaseDriver(driverClassName);
        
        // save the connection info
        m_connectionUrl = connectionUrl;
        m_query = query;
        
        fetchResultSet();
    }
    
    public int getNumDbmsColumns()
    {
        return m_numDbmsCols;
    }
    
    public int getNumDbmsRows()
    {
        return m_numDbmsRows;
    }
    
    protected ResultSet getResultSet()
    {
        return m_resultSet;
    }
    
    private void fetchResultSet()
    throws SQLException
    {
        releaseJdbcResources();
        
        m_connection = DriverManager.getConnection(m_connectionUrl);
        m_statement = m_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                   ResultSet.CONCUR_READ_ONLY);
        
        m_resultSet =  m_statement.executeQuery(m_query);
        
        processResultSet(m_resultSet);
    }

    private void processResultSet(ResultSet resultSet) 
    throws SQLException
    {
        // count number of rows
        m_numDbmsRows = getDbmsRowCount(resultSet);
        
        // create row data structures
        setRowsCapacity(calcRowsCapacity(m_numDbmsRows));
        for (int i = 1; i <= m_numDbmsRows; i++) {
            DbmsRowImpl row = new DbmsRowImpl(this, i);
            add(row, false, false, Access.ByIndex, i);
        }
       
        // process column information
        ResultSetMetaData rsmd = resultSet.getMetaData();
        if (rsmd != null) {
            m_numDbmsCols = rsmd.getColumnCount();
            setColumnsCapacity(calcColumnsCapacity(m_numDbmsCols));
            
            for (int i = 1; i <= m_numDbmsCols; i++) {
                DbmsColumnImpl col = new DbmsColumnImpl(this, i, rsmd.getColumnClassName(i));
                add(col, false, false, Access.ByIndex, i);
                
                col.setLabel(rsmd.getColumnLabel(i));
            }            
        }
        else
            throw new SQLException("No metadata available");
    }

    private int getDbmsRowCount(ResultSet resultSet)
    {
        int totalRows = 0;
        try {
            resultSet.last();
            totalRows = resultSet.getRow();
            resultSet.beforeFirst();
        } 
        catch(SQLFeatureNotSupportedException ex)  {
            return 0;
        }
        catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return totalRows ;
    }

    private void releaseJdbcResources()
    {
        m_numDbmsCols = m_numDbmsRows = 0;
        if (m_resultSet != null) {
            try {
                m_resultSet.close();
                m_resultSet = null;
            }
            catch (SQLException e) {
                // noop
            }
        }
        
        if (m_statement != null) {
            try {
                m_statement.close();
                m_statement = null;
            }
            catch (SQLException e) {
                // noop
            }
        }
        
        if (m_connection != null) {
            try {
                m_connection.close();
                m_connection = null;
            }
            catch (SQLException e) {
                // noop
            }
        }        
    }
    
    @Override
    protected void delete(boolean compress)
    {
        try {
            super.delete(compress);
        }
        finally {
            releaseJdbcResources();
        }
    }
    
    @Override
    public void finalize() 
    {
        try {
            super.finalize();   
        }
        finally {
            releaseJdbcResources();
        }
    }
}
