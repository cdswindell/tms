package org.tms.api.factories;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;

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

    /**
     * Construct a TableFactory instance.
     * <p>
     * Protected constructor prevents anyone creating a TableFactory from outside of the package.
     * The TableFactory class is intended only to provide a place for the static factory methods
     * to reside.
     */
    protected TableFactory()
    {
        // noop
    }
}
