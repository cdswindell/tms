package org.tms.api.event;

import org.tms.api.ElementType;

public class RecalculateEvent extends TableElementEvent
{
    private static final long serialVersionUID = -6130867796445114499L;

    public static final RecalculateEvent createOn(Listenable te, long timeStamp)
    {
        return new RecalculateEvent(te, TableElementEventType.OnRecalculate, timeStamp, null);
    }
    
    private RecalculateEvent(Listenable source, TableElementEventType evT, long assemblyId, ElementType et)
    {
        super(evT, source, assemblyId);
    }
}
