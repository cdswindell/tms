package org.tms.api.factories;

import java.io.IOException;

import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.io.options.XlsOptions;
import org.tms.io.XlsReader;
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
        TableContext tc = ContextImpl.fetchDefaultContext();
        return tc;
    }   
    
    static public TableContext importWorkbook(String fileName, XlsOptions format)
    {
        TableContext tc = TableContextFactory.createTableContext();
        try {
            importWorkbook(fileName, format, tc);
        }
        finally {
            
        }
        
        return tc;
    }
        
    static public TableContext importWorkbook(String fileName, XlsOptions format, TableContext tc)
    {
        if (format == null)
            format = XlsOptions.Default;
        
        try
        {
            XlsReader r = new XlsReader(fileName, tc, (XlsOptions)format);
            r.parseWorkbook();
        }
        catch (IOException e)
        {
            throw new TableIOException(e);
        }
        
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
