package org.tms.api.factories;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Cell;
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
    /**
     * Create a new {@link Table} in the default {@link TableContext}. The new table has no {@link Row}s,
     * {@link Column}s, or {@link Cell}s, however, space is allocated based on the default parameters
     * in the default {@link TableContext}. The new table is returned. 
     * @return the new {@code Table}.
     */
    static public Table createTable()
    {
        Table t = TableImpl.createTable();
        return t;
    }
    
    /**
     * Create a new {@link Table} in the default {@link TableContext}. The new table has no {@link Row}s,
     * {@link Column}s, or {@link Cell}s, however, space is allocated for {@code nRows} {@link Row}s
     * and {@code nCols} {@link Column}s.
     * @param nRows number of {@code Row}s space is allocated for
     * @param nCols number of {@code Column}s space is allocated for
     * @return the new {@code Table}.
     */
    static public Table createTable(int nRows, int nCols)
    {
		Table t = TableImpl.createTable(nRows, nCols);
		return t;
    }

    /**
     * Create a new {@link Table} in the default {@link TableContext}. The new table has no {@link Row}s,
     * {@link Column}s, or {@link Cell}s, however, space is allocated based on the utilization
     * of the specified template {@link Table}.
     * @param template {@code Table} used to initialize configurable options in the new {@code Table}
     * @return the new, initialized, {@code Table}
     */
    static public Table createTable(Table template)
    {
        Table t = null;       
        if (template != null && template instanceof TableImpl) 
            t = TableImpl.createTable((TableImpl)template);
        
        return t;
    }

    static public Table createTable(TableContext tableContext)
    {
        Table t = null;       
        if (tableContext instanceof ContextImpl)
        	t = TableImpl.createTable((ContextImpl)tableContext);
        
        return t;
    }

    static public Table createTable(int nRows, int nCols, Table template)
    {
		Table t = null;		
        if (t instanceof TableImpl)
        	t = TableImpl.createTable(nRows, nCols, (TableImpl)template);
        
		return t;
    }
    
    static public Table createTable(int nRows, int nCols, TableContext tableContext)
    {
        Table t = null;       
        if (tableContext instanceof ContextImpl)
            t = TableImpl.createTable(nRows, nCols, (ContextImpl)tableContext);
        
        return t;
    }

    /**
     * Creates a new {@link Table} in the default {@link TableContext} from the specified
     * {@link java.util.Collection Collection} {@code c}. The data in the {@code Collection}
     * is added to the first {@link Column} in the new {@link Table}, in the natural order of
     * the {@code Collection}.
     * @param c the {@code Collection}
     * @return the new {@code Table}
     */
    static public Table fromCollection(Collection<?> c)
    {
        return fromCollection(c, ContextImpl.fetchDefaultContext());
    }
    
    /**
     * Creates a new {@link Table} in {@link TableContext} {@code tableContext} from the specified
     * {@link java.util.Collection Collection} {@code c}. The data in the {@code Collection}
     * is added to the first {@link Column} in the new {@link Table}, in the natural order of
     * the {@code Collection}.
     * @param c the {@code Collection}
     * @param tableContext {@code TableContext} where the new table is homed
     * @return the new {@code Table}
     */
    static public Table fromCollection(Collection<?> c, TableContext tableContext)
    {
        Table t = null; 
        if (c instanceof ContextImpl) {
	    	int nRows = c.size();
	        Class<?> keyClazz = null;
	        
            t = TableImpl.createTable(nRows, 1, (ContextImpl)tableContext);
            
            Column keyCol = null;
            for (Object e : c) {
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
    
    /**
     * Creates a new {@link Table} in the default {@link TableContext} from the specified
     * {@link java.util.Map Map} {@code map}. The key data in the {@code Map}
     * is added to the first {@link Column} in the new {@link Table}, in the natural order of
     * the {@code Map}, and the value data is added to the second {@link Column}.
     * @param map the {@code Map}
     * @return the new {@code Table}
     */
    static public Table fromMap(Map<?, ?> map)
    {
        return fromMap(map, ContextImpl.fetchDefaultContext());
    }
    
    /**
     * Creates a new {@link Table} in {@link TableContext} {@code tableContext} from the specified
     * {@link java.util.Map Map} {@code map}. The key data in the {@code Map}
     * is added to the first {@link Column} in the new {@link Table}, in the natural order of
     * the {@code Map}, and the value data is added to the second {@link Column}.
     * @param map the {@code Map}
     * @param tableContext {@code TableContext} where the new table is homed
     * @return the new {@code Table}
     */
    static public Table fromMap(Map<?, ?> map, TableContext tableContext)
    {
        Table t = null; 
        if (tableContext instanceof ContextImpl) {
        	int nRows = map.size();
            Class<?> keyClazz = null;
            Class<?> valClazz = null;
            
            t = TableImpl.createTable(nRows, 2, (ContextImpl)tableContext);
            
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

    static public Table createDbmsTable(String connectionUrl, String query, String driverClassName) 
    throws SQLException, ClassNotFoundException
    {
        Table t = DbmsTableImpl.createTable(connectionUrl, query, 
                  							driverClassName, ContextImpl.fetchDefaultContext());       
        return t;
    }

    static public Table createDbmsTable(String connectionUrl, String query, String driverClassName, TableContext tc) 
    throws SQLException, ClassNotFoundException
    {
    	Table t = null;
        if (tc instanceof ContextImpl)
        	t = DbmsTableImpl.createTable(connectionUrl, query, driverClassName, (ContextImpl)tc);       
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
        
        if (tc == null)
        	tc = TableContextFactory.fetchDefaultTableContext();
        
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
        
        if (tc == null)
        	tc = TableContextFactory.fetchDefaultTableContext();
        
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
    protected TableFactory()
    {
        // noop
    }
}
