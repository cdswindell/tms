package org.tms.api.factories;

import java.io.IOException;
import java.sql.SQLException;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.io.CSVReader;
import org.tms.io.IOFormat;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.dbms.DbmsTableImpl;

public final class TableFactory 
{
    static public Table createTable()
    {
        Table t = TableImpl.createTable();
        return t;
    }
    
    static public Table createTable(int nRows, int nCols)
    {
		Table t = TableImpl.createTable(nRows, nCols);
		return t;
    }

    static public Table createTable(Table ot)
    {
        Table t = null;       
        if (ot != null && ot instanceof TableImpl) 
            t = TableImpl.createTable((TableImpl)ot);
        
        return t;
    }

    static public Table createTable(TableContext c)
    {
        Table t = null;       
        if (c instanceof ContextImpl)
        	t = TableImpl.createTable((ContextImpl)c);
        
        return t;
    }

    static public Table createTable(int nRows, int nCols, Table rt)
    {
		Table t = null;		
        if (t instanceof TableImpl)
        	t = TableImpl.createTable(nRows, nCols, (TableImpl)rt);
        
		return t;
    }
    
    static public Table createTable(int nRows, int nCols, TableContext c)
    {
        Table t = null;       
        if (c instanceof ContextImpl)
            t = TableImpl.createTable(nRows, nCols, (ContextImpl)c);
        
        return t;
    }

    /*
     * DBMS Tables
     */
    static public Table createDbmsTable(String connectionUrl, String query) 
    throws SQLException
    {
        Table t = DbmsTableImpl.createTable(connectionUrl, query);       
        return t;
    }

    static public Table createDbmsTable(String connectionUrl, String query, String driverClassName, ContextImpl tc) 
    throws SQLException, ClassNotFoundException
    {
        Table t = DbmsTableImpl.createTable(connectionUrl, query, driverClassName, tc);       
        return t;
    }

    static public Table createDbmsTable(String connectionUrl, String query, String driverClassName) 
    throws SQLException, ClassNotFoundException
    {
        Table t = DbmsTableImpl.createTable(connectionUrl, query, 
                  driverClassName, ContextImpl.fetchDefaultContext());       
        return t;
    }

    /*
     * Import Operations
     */    
    static public Table importCSV(String csvFileName, boolean hasRowNames, boolean hasColumnHeaders)
    {
        return importCSV(csvFileName, ContextImpl.fetchDefaultContext(), IOFormat.CSV.withRowNames(hasRowNames).withColumnNames(hasColumnHeaders));
    }
    
    static public Table importCSV(String csvFileName, TableContext tc, IOFormat format)
    {
        CSVReader r = new CSVReader(csvFileName, tc, format);
        try
        {
            return r.parse();
        }
        catch (IOException e)
        {
            throw new TableIOException(e);
        }
    }
    
    /**
     * Construct a TableFactory instance.
     * <p>
     * Protected constructor prevents anyone creating a TableFactory from outside of the package.
     * The TableFactory class is intended only to provide a home for the static factory methods
     * to reside.
     */
    protected TableFactory()
    {
        // noop
    }
}
