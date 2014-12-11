package org.tms.api;

import org.tms.tds.TableImpl;

public class Table extends TableImpl
{

    public Table()
    {
        super();
    }

    public Table(int nRows, int nCols)
    {
        super(nRows, nCols);
    }

    public Table(int nRows, int nCols, TableContext c)
    {
        super(nRows, nCols, c);
    }

    public Table(int nRows, int nCols, Table t)
    {
        super(nRows, nCols, t);
    }

}
