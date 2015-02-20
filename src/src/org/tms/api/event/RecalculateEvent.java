package org.tms.api.event;

import org.tms.api.ElementType;

public class RecalculateEvent extends TableElementEvent
{
    private static final long serialVersionUID = 1686418740323871539L;

    static final RecalculateEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new RecalculateEvent(source, trigger, TableElementEventType.OnRecalculate, assemblyId, null);
    }
    
    private RecalculateEvent(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId, ElementType et)
    {
        super(evT, source, trigger, assemblyId);
    }
}
