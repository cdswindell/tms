package org.tms.tds;

import org.tms.api.ElementType;

public class Column extends BaseElement
{
    public Column(Table parentTable)
    {
        super(ElementType.Column);
    }
}
