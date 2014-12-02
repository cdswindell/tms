package org.tms.tds;

import org.tms.api.ElementType;

public class Row extends TableElement
{
    protected Row(Table parentTable)
    {
        super(ElementType.Row, parentTable);
    }

    @Override
    protected void reset()
    {
    }

}
