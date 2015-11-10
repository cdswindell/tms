package org.tms.api.factories;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.io.CSVOptions;
import org.tms.api.io.IOOption;
import org.tms.api.io.TMSOptions;
import org.tms.api.io.XLSOptions;
import org.tms.api.io.XMLOptions;
import org.tms.io.CSVReader;
import org.tms.io.TMSReader;
import org.tms.io.TableExportAdapter;
import org.tms.io.XMLReader;
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

    static public Table fromCollection(Collection<?> col)
    {
        return fromCollection(col, ContextImpl.fetchDefaultContext());
    }
    
    static public Table fromCollection(Collection<?> col, TableContext c)
    {
        Table t = null; 
        if (c instanceof ContextImpl) {
	    	int nRows = col.size();
	        Class<?> keyClazz = null;
	        
            t = TableImpl.createTable(nRows, 1, (ContextImpl)c);
            
            Column keyCol = null;
            for (Object e : col) {
            	Row r = t.addRow();
            	
            	if (keyCol == null)
            		keyCol = t.addColumn(1);
            	
            	
            	if (e != null) {
                	t.setCellValue(r, keyCol, e);
                	
                	if (keyClazz == null)
                		keyClazz = e.getClass();
                	else {
                		Class<? extends Object> thisClazz = e.getClass();
                		if (!(thisClazz == keyClazz || keyClazz.isAssignableFrom(thisClazz))) {
                			if (thisClazz.isAssignableFrom(keyClazz))
                				keyClazz = thisClazz;
                			else
                				keyClazz = Object.class;
                		}
                	}
            	}
            }
            
            // assign column types
            if (keyClazz != null && keyClazz != Object.class)
            	keyCol.setDataType(keyClazz);
        }
        
        return t;
    }
    
    static public Table fromMap(Map<?, ?> map)
    {
        return fromMap(map, ContextImpl.fetchDefaultContext());
    }
    
    static public Table fromMap(Map<?, ?> map, TableContext c)
    {
        Table t = null; 
        if (c instanceof ContextImpl) {
        	int nRows = map.size();
            Class<?> keyClazz = null;
            Class<?> valClazz = null;
            
            t = TableImpl.createTable(nRows, 2, (ContextImpl)c);
            
            Column keyCol = null;
            Column valCol = null;
            for (Map.Entry<?, ?> e : map.entrySet()) {
            	Row r = t.addRow();
            	
            	if (keyCol == null)
            		keyCol = t.addColumn(1);
            	
            	if (valCol == null)
            		valCol = t.addColumn(2);
            	
            	t.setCellValue(r, keyCol, e.getKey());
            	if (keyClazz == null)
            		keyClazz = e.getKey().getClass();
            	else {
            		Class<? extends Object> thisClazz = e.getKey().getClass();
            		if (!(thisClazz == keyClazz || keyClazz.isAssignableFrom(thisClazz))) {
            			if (thisClazz.isAssignableFrom(keyClazz))
            				keyClazz = thisClazz;
            			else
            				keyClazz = Object.class;
            		}
            	}
            	
            	Object val = e.getValue();
            	if (val != null) {
                	t.setCellValue(r, valCol, val);
                	
                	if (valClazz == null)
                		valClazz = val.getClass();
                	else {
                		Class<? extends Object> thisClazz = e.getKey().getClass();
                		if (!(thisClazz == valClazz || valClazz.isAssignableFrom(thisClazz))) {
                			if (thisClazz.isAssignableFrom(valClazz))
                				valClazz = thisClazz;
                			else
                				valClazz = Object.class;
                		}
                	}
            	}
            }
            
            // assign column types
            if (keyClazz != null && keyClazz != Object.class)
            	keyCol.setDataType(keyClazz);
            
            if (valClazz != null && valClazz != Object.class)
            	valCol.setDataType(valClazz);
        }
        
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
    static public Table importFile(String fileName)
    {
        TableContext tc = ContextImpl.fetchDefaultContext();
        IOOption<?> format = TableExportAdapter.generateOptionsFromFileExtension(fileName);
        if (format == null)
            format = TMSOptions.Default;
        
        return importFile(fileName, tc, format);
    }
    
    static public Table importFile(String fileName, IOOption<?> format)
    {
        TableContext tc = ContextImpl.fetchDefaultContext();
        return importFile(fileName, tc, format);
    }
    
    static public Table importFile(String fileName, TableContext tc, IOOption<?> format)
    {
        if (format == null)
            throw new IllegalArgumentException("Format required");
        
        if (!format.canImport())
            throw new IllegalArgumentException("Format does not support Table import");
        
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
                    XlsReader r = new XlsReader(fileName, tc, (XLSOptions)format);
                    return r.parseActiveSheet();
                }
                
                case XML:
                {
                    XMLReader r = new XMLReader(fileName, tc, (XMLOptions)format);
                    return r.parse();
                }
                    
                case TMS:
                {
                    TMSReader r = new TMSReader(fileName, tc, (TMSOptions)format);
                    return r.parse();
                }
                    
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
    
    static public Table importFile(InputStream in, TableContext tc, IOOption<?> format)
    {
        if (format == null)
            throw new IllegalArgumentException("Format required");
        
        if (!format.canImport())
            throw new IllegalArgumentException("Format does not support Table import");
        
        try
        {
            switch (format.getFileFormat()) {
                case CSV:
                {
                    CSVReader r = new CSVReader(in, tc, (CSVOptions)format);
                    return r.parse();
                }
                    
                case EXCEL:
                {
                    XlsReader r = new XlsReader(in, tc, (XLSOptions)format);
                    return r.parseActiveSheet();
                }
                
                case XML:
                {
                    XMLReader r = new XMLReader(in, tc, (XMLOptions)format);
                    return r.parse();
                }
                    
                case TMS:
                {
                    TMSReader r = new TMSReader(in, tc, (TMSOptions)format);
                    return r.parse();
                }
                    
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
