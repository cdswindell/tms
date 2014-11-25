package org.tms.tds;

import org.tms.api.TableElementType;

public class Table extends TableElement
{
    public Table()
    {
        super(TableElementType.Table);
    }

    public Table(String name)
    {
        this();       
        this.setLabel(name);
    }

}
