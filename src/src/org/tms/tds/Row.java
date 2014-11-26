package org.tms.tds;

import org.tms.api.ElementType;

public class Row extends BaseElement
{
    public Row()
    {
        super(ElementType.Table);
    }

    public Row(String name)
    {
        this();       
        this.setLabel(name);
    }

}
