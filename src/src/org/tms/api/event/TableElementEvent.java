package org.tms.api.event;

import java.util.EventObject;

import org.tms.api.TableElement;

abstract public class TableElementEvent extends EventObject
{
    private static final long serialVersionUID = -1891192023512811055L;
    
    private TableElementEventType m_evT;
    
    public TableElementEvent(TableElementEventType evT, TableElement source)
    {
        super(source);
        m_evT = evT;
    }

    @Override
    public TableElement getSource()
    {
        return (TableElement)super.getSource();
    }

    public TableElementEventType getType()
    {
        return m_evT;
    }
}
