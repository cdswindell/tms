package org.tms.tds.dbms;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.api.io.IOOption;
import org.tms.io.DbmsTableExportAdapter;
import org.tms.io.TableExportAdapter;
import org.tms.tds.ContextImpl;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;
import org.tms.util.WeakHashSet;

import com.mysql.jdbc.JDBC4ResultSet;

public class DbmsTableImpl extends TableImpl implements ExternalDependenceTableElement
{
    public static final DbmsTableImpl createTable(String connectionUrl, String query) 
    throws SQLException
    {
        try
        {
            return new DbmsTableImpl(connectionUrl, query);
        }
        catch (ClassNotFoundException e)
        {
            // noop, cannot happen in this instance;
            return null;
        }
    }
    
    public static final DbmsTableImpl createTable(String connectionUrl, String query, String driverClassName, ContextImpl tc) 
    throws SQLException, ClassNotFoundException
    {
        return new DbmsTableImpl(connectionUrl, query, driverClassName, tc);
    }

    private String m_connectionUrl;
    private String m_query;
    private String m_driverClassName;
    
    private ResultSet m_resultSet;
    private PreparedStatement m_statement;
    private Connection m_connection;
    
    private int m_numDbmsCols;
    private int m_numDbmsRows;
    
    private Set<DbmsRowImpl> m_unprocessedRows;
    private boolean m_processingResultSet = false;
    
    public DbmsTableImpl(String connectionUrl, String query)
    throws SQLException, ClassNotFoundException
    {
        this(connectionUrl, query, null, ContextImpl.fetchDefaultContext());
    }
    
    public DbmsTableImpl(String connectionUrl, String query, String driverClassName, ContextImpl tc) 
    throws ClassNotFoundException, SQLException
    {
        // initialize the default table object
        super(tc.getRowCapacityIncr(), tc.getColumnCapacityIncr(), tc);
        m_numDbmsCols = m_numDbmsRows = 0;
        
        // load the database driver, this could throw an exception
        if (driverClassName != null)
            tc.loadDatabaseDriver(driverClassName);
        
        // save the connection info
        m_driverClassName = driverClassName;
        m_connectionUrl = connectionUrl;
        m_query = query;
        
        fetchResultSet(false);
    }
    
    @Override
    public void export(String fileName, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new DbmsTableExportAdapter(this, fileName, options);
        writer.export();
    }

    @Override
    public void export(OutputStream out, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new DbmsTableExportAdapter(this, out, options);
        writer.export();
    }

    public String getConnectionUrl()
    {
        return m_connectionUrl;
    }
    
    public String getQuery()
    {
        return m_query;
    }
    
    public String getDriverClassName()
    {
        return m_driverClassName;
    }
    
    public int getNumDbmsColumns()
    {
        return m_numDbmsCols;
    }
    
    public int getNumDbmsRows()
    {
        return m_numDbmsRows;
    }
    
    synchronized public void refresh() 
    throws SQLException
    {
        fetchResultSet(true);
    }
    
    protected ResultSet getResultSet()
    {
        return m_resultSet;
    }
    
    /**
     * Remove the specified row from the set of unprocessed rows and release JDBC resources
     * if all rows have been processed
     * @param row the row to removed from the unprocessed set
     */
    synchronized protected void removeDbmsRowFromUnprocessed(DbmsRowImpl row)
    {
        // exit if we're processing a result set, rows are deleted  in this case
        if (m_processingResultSet)
            return;
        
        if (row != null && m_unprocessedRows != null) {
            m_unprocessedRows.remove(row);
            if (m_unprocessedRows.isEmpty())
                releaseJdbcResources();
        }       
    }
    
    synchronized protected int decrementNumDbmsRows()
    {
        // skip operation if we're processing a result set
        if (m_processingResultSet)
            return m_numDbmsRows;
        
        if (--m_numDbmsRows < 0)
            m_numDbmsRows = 0;
        
        return m_numDbmsRows;
    }
    
    private void fetchResultSet(boolean isRefresh)
    throws SQLException
    {
        releaseJdbcResources();
        
        m_connection = DriverManager.getConnection(m_connectionUrl);
        m_statement = m_connection.prepareStatement(m_query,
                                                    ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                    ResultSet.CONCUR_READ_ONLY);
        if (m_statement == null)
            m_statement = m_connection.prepareStatement(m_query);

        m_resultSet =  m_statement.executeQuery();
        processResultSet(m_resultSet, isRefresh);
    }

    private void processResultSet(ResultSet resultSet, boolean isRefresh) 
    throws SQLException
    {
        m_processingResultSet = true;        
        try {
            // delete existing database rows
            if (isRefresh) {
                if (m_unprocessedRows != null) {
                    this.delete(m_unprocessedRows.toArray(new Row [] {}));
                    m_unprocessedRows = null;
                }
                    
                Set<Row> toDeleteRows = new HashSet<Row>(getNumRows());            
                for (RowImpl row : getRowsInternal()) {
                    if (row instanceof DbmsRowImpl)
                        toDeleteRows.add(row);
                }
                
                this.delete(toDeleteRows.toArray(new Row [] {}));
                m_numDbmsRows = 0;
            }
            else
                m_numDbmsCols = m_numDbmsRows = 0;
            
            // create row data structures
            m_numDbmsRows = (int)getDbmsRowCount(resultSet);
            
            setRowsCapacity(calcRowsCapacity(Math.max(m_numDbmsRows, getNumRows())));
            m_unprocessedRows = new WeakHashSet<DbmsRowImpl>(m_numDbmsRows);
            for (int i = 1; i <= m_numDbmsRows; i++) {
                DbmsRowImpl row = new DbmsRowImpl(this, i);
                m_unprocessedRows.add(row);
                add(row, false, false, Access.ByIndex, i);
            }
           
            // process column information
            if (!isRefresh) {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                if (rsmd != null) {
                    m_numDbmsCols = rsmd.getColumnCount();
                    setColumnsCapacity(calcColumnsCapacity(m_numDbmsCols));
                    
                    for (int i = 1; i <= m_numDbmsCols; i++) {
                        DbmsColumnImpl col = new DbmsColumnImpl(this, i, rsmd.getColumnClassName(i));
                        add(col, false, false, Access.ByIndex, i);
                        
                        col.setLabelInternal(rsmd.getColumnLabel(i));
                    }            
                }
                else
                    throw new SQLException("No metadata available");
            }
            
            // if we're refreshing, recalculate all derivations
            if (isRefresh)
                recalculate();
        }
        finally {
            m_processingResultSet = false;
        }
    }

    private long getDbmsRowCount(ResultSet resultSet)
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

    private void releaseJdbcResources()
    {
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
            m_unprocessedRows = null;
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
}
