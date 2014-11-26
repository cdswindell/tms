package org.tms.tds;

import org.tms.api.ElementType;

public class Column extends TableElement
{
    public Column(Table parentTable)
    {
        super(ElementType.Column, parentTable);
    }
}
