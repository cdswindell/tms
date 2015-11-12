package org.tms.tds;

public class CellUtils
{
    static final public boolean cellUpdater(CellImpl c, Object update)
    {
        return c.setCellValueNoDataTypeCheck(update);
    }

    static final public void cellErrorMessageUpdater(CellImpl c, String eMsg)
    {
        c.setErrorMessage(eMsg);
    }
}
