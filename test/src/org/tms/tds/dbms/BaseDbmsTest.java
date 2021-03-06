package org.tms.tds.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.tms.BaseTest;
import org.tms.api.exceptions.TableIOException;

import com.mysql.jdbc.JDBC4ResultSet;

public class BaseDbmsTest extends BaseTest
{
    private Statement m_statement;
    private Connection m_connection;

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
        releaseJdbcResources();
    }

    public ResultSet fetchResultSet(String connUrl, String query) 
    throws SQLException
    {
        releaseJdbcResources();
        ResultSet rs = null;
        
        m_connection = DriverManager.getConnection(connUrl);
        m_statement = m_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                   ResultSet.CONCUR_READ_ONLY);
        if (m_statement == null)
        	m_statement = m_connection.createStatement();
        
        rs =  m_statement.executeQuery(query);
        
        return rs;
    }

    public long getDbmsRowCount(ResultSet resultSet) 
    throws SQLException
    {
        long totalRows = 0;
        try {
        	totalRows = ((JDBC4ResultSet)resultSet).getUpdateCount();
        	return totalRows;
        }
        catch (ClassCastException e) { }
        
        // try another technique
        try {   
        	resultSet.beforeFirst();;
            if (resultSet.last())
            	totalRows = resultSet.getRow();
            else {
            	while (resultSet.next()) {
            		totalRows++;
            	}
            }
            resultSet.beforeFirst();
        } 
        catch(SQLFeatureNotSupportedException ex)  {
            return 0;
        }
        catch (SQLException e)
        {
            throw new TableIOException(e);
        }
        
        return totalRows ;
    }

    public int getDbmsColumnCount(ResultSet resultSet) 
    throws SQLException
    {
        int totalCols = 0;
        try {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            totalCols = rsmd.getColumnCount();
        } 
        catch(SQLFeatureNotSupportedException ex)  {
            return 0;
        }
        
        return totalCols ;
    }

    public void close(Object obj)
    {
        if (obj == null)
            return;
        
        if (obj instanceof ResultSet) {
            try
            {
                ((ResultSet)obj).close();
            }
            catch (SQLException e) { }
        }
        else if (obj instanceof Statement) {
            try
            {
                ((Statement)obj).close();
            }
            catch (SQLException e) { }
        }
        else if (obj instanceof Connection) {
            try
            {
                ((Connection)obj).close();
            }
            catch (SQLException e) { }
        }
    }
    
    protected void releaseJdbcResources()
    {
        close(m_statement); m_statement = null;
        close(m_connection); m_connection = null;
    }    
}
