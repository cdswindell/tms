package org.tms.tds;

import org.tms.api.ElementType;

public class Row extends TableElement
{
    public Row(Table parentTable)
    {
        super(ElementType.Table, parentTable);
    }
}
