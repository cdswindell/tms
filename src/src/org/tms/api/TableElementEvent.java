package org.tms.api;

import java.util.EventObject;

public class TableElementEvent extends EventObject
{

    public TableElementEvent(TableElement source)
    {
        super(source);
    }

    @Override
    public TableElement getSource()
    {
        return (TableElement)super.getSource();
    }

}
