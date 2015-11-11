package org.tms.api.factories;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.io.IOOption;
import org.tms.tds.ContextImpl;

/**
 * The class {@code TableContextFactory} contains methods to construct {@link TableContext} 
 * objects as well as to import
 * data in other formats (e.g., TMS, XML, Microsoft Excel) into {@link Table}s in the {@link TableContext}.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class TableContextFactory
{
    /**
     * Create, initialize, and return a new {@link TableContext}. The default
     * {@link TableContext} is used to initialize the new {@link TableContext}.
     * @return a new, initialized {@code TableContext}.
     * @see TableContextFactory#fetchDefaultTableContext fetchDefaultTableContext
     */
    static public TableContext createTableContext()
    {
        TableContext tc = ContextImpl.createContext();
        return tc;
    }
    
    /**
     * Create, initialize, and return a new {@link TableContext}. The specified
     * {@link TableContext} is used to initialize the new {@link TableContext}.
     * @param template {@code TableContext} used to initialize configurable options in the new {@code TableContext}
     * @return a new, initialized {@code TableContext}.
     */
    static public TableContext createTableContext(TableContext template)
    {
    	TableContext tc = ContextImpl.createContext(template);
    	
        return tc;
    }
    
    /**
     * Returns the default {@link TableContext}. The default {@link TableContext}
     * is where all new {@link Table}s are homed, unless an alternative
     * {@link TableContext} is specified when the new {@link Table} is created.
     * @return the default {@code TableContext}
     */
    static public TableContext fetchDefaultTableContext()
    {
        TableContext tc = ContextImpl.fetchDefaultContext();
        return tc;
    }   
    
    /**
     * Creates a new {@link TableContext} and imports all of the {@link Table}s contained 
     * in the specified file into it. Returns the new {@link TableContext}. Currently,
     * only TMS/TC, XML, and XLS files can be imported into {@link TableContext}s.
     * Default {@link org.tms.api.io.IOOption IOOption&lt;T&gt;} parameters are used, with the
     * {@code IOOption<T>} selected based on the file name extension. 
     * @param fileName the name of the file containing the tables to import
     * @return the new {@code TableContext}
     */
    static public TableContext importTables(String fileName)
    {
        TableContext tc = createTableContext();
        tc.importTables(fileName);
        return tc;
    }
        
    /**
     * Creates a new {@link TableContext} and imports all of the {@link Table}s contained 
     * in the specified file into it, using the parameters in the specified 
     * {@link org.tms.api.io.IOOption IOOption&lt;T&gt;}. 
     * Returns the new {@link TableContext}. Currently,
     * only TMS/TC, XML, and XLS files can be imported into {@link TableContext}s.
     * @param fileName the name of the file containing the tables to import
     * @param format {@code IOOption<T>} containing the import options
     * @return the new {@code TableContext}
     */
    static public TableContext importTables(String fileName, IOOption<?> format)
    {
        TableContext tc = createTableContext();
        tc.importTables(fileName, format);
        return tc;
    }
     
    /**
     * Construct a TableContextFactory instance.
     * <p>
     * Protected constructor prevents anyone creating a TableContextFactory from outside of the package.
     * The TableContextFactory class is intended only to provide a place for the static factory methods
     * to reside.
     */
    protected TableContextFactory()
    {
        // noop
    }
}
