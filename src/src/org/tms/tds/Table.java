package org.tms.tds;

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
