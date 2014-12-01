package org.tms.tds;

import org.tms.api.ElementType;

public class Row extends TableElement
{
    public Row(Table parentTable)
    {
        super(ElementType.Table, parentTable);
    }

    @Override
    protected void reset()
    {
    }

    @Override
    protected Table getTable()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
