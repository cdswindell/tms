package org.tms.api.factories;

import org.tms.api.TableContext;
import org.tms.tds.ContextImpl;

public class TableContextFactory
{
    static public TableContext createTableContext()
    {
        TableContext tc = ContextImpl.createContext();
        return tc;
    }
    
    static public TableContext createTableContext(TableContext c)
    {
    	TableContext tc = ContextImpl.createContext(c);
    	
        return tc;
    }
    
    static public TableContext fetchDefaultTableContext()
    {
        TableContext tc = ContextImpl.createDefaultContext();
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
