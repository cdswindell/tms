package org.tms.api.factories;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.tds.TableImpl;

public class TableFactory 
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

    static public Table createTable(int nRows, int nCols, TableContext c)
    {
		Table t = TableImpl.createTable(nRows, nCols, c);
		return t;
    }

    static public Table createTable(int nRows, int nCols, Table rt)
    {
		Table t = TableImpl.createTable(nRows, nCols, rt);
		return t;
    }
}
