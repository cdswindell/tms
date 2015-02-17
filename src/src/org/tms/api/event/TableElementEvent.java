package org.tms.api.event;

import java.util.EventObject;

import org.tms.api.TableElement;

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
