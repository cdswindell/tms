package org.tms.api;

import org.tms.tds.ContextImpl;

public class TableContextFactory
{
    static public TableContext createTableContext()
    {
        TableContext tc = ContextImpl.createContext();
        return tc;
    }
    
    static public TableContext fetchDefaultTableContext()
    {
        TableContext tc = ContextImpl.createDefaultContext();
        return tc;
    }   
}
