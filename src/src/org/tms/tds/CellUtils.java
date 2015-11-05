package org.tms.tds;

public class CellUtils
{
    static final public boolean cellUpdater(CellImpl c, Object update)
    {
        return c.setCellValueNoDataTypeCheck(update);
    }
}
