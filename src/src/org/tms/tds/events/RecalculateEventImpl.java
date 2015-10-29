package org.tms.tds.events;

import org.tms.api.ElementType;
import org.tms.api.events.Listenable;
import org.tms.api.events.RecalculateEvent;
import org.tms.api.events.TableElementEventType;

public class RecalculateEventImpl extends BaseTableElementEvent implements RecalculateEvent
{
    private static final long serialVersionUID = 1686418740323871539L;

    static final RecalculateEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new RecalculateEventImpl(source, trigger, TableElementEventType.OnRecalculate, assemblyId, null);
    }
    
    private RecalculateEventImpl(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId, ElementType et)
    {
        super(evT, source, trigger, assemblyId);
    }
}
