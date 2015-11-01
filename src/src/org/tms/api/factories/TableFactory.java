package org.tms.api.factories;

import java.io.IOException;
import java.sql.SQLException;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.io.options.CSVOptions;
import org.tms.api.io.options.IOOptions;
import org.tms.api.io.options.XlsOptions;
import org.tms.io.CSVReader;
import org.tms.io.XlsReader;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.dbms.DbmsTableImpl;

/**
 * The class {@code TableFactory} contains methods to construct {@link Table} objects as well as to import
 * data in other formats (e.g., CSV, Microsoft Excel) into {@link Table}s
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
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
        return importFile(csvFileName, ContextImpl.fetchDefaultContext(), CSVOptions.Default.withRowLabels(hasRowNames).withColumnNames(hasColumnHeaders));
    }
    
    static public Table importFile(String fileName, TableContext tc, IOOptions<?> format)
    {
        if (format == null)
            throw new IllegalArgumentException("Format argument cannot be null");
        
        if (!format.canImport())
            throw new IllegalArgumentException("Format does not support import");
        
        try
        {
            switch (format.getFileFormat()) {
                case CSV:
                {
                    CSVReader r = new CSVReader(fileName, tc, (CSVOptions)format);
                    return r.parse();
                }
                    
                case EXCEL:
                {
                    XlsReader r = new XlsReader(fileName, tc, (XlsOptions)format);
                    return r.parseActiveSheet();
                }
                
                case TMS:
                case XML:
                case JSON:
                {
                    return null;
                }
                    
                default:    
                    throw new UnimplementedException("No support for file format:" + format.getFileFormat());                    
            }
        }
        catch (IOException e)
        {
            throw new TableIOException(e);
        }
    }
    
    /**
     * Construct a TableFactory instance.
     * <p>
     * Protected constructor prevents anyone creating a TableFactory from outside of the class.
     * The TableFactory class is intended only to provide a home for the static factory methods
     * to reside.
     */
    private TableFactory()
    {
        // noop
    }
}
