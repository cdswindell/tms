package org.tms.tds.events;

import org.tms.api.events.Listenable;
import org.tms.api.events.PendingDerivationsEvent;
import org.tms.api.events.TableElementEventType;

public class PendingDerivationsEventImpl extends BaseTableElementEvent implements PendingDerivationsEvent
{
    private static final long serialVersionUID = 4434997403257589967L;

    static final PendingDerivationsEvent createPendingsEvent(Listenable source, Listenable trigger, long assemblyId)
    {
        return new PendingDerivationsEventImpl(source, trigger, TableElementEventType.OnPendings, assemblyId);
    }
    
    static final PendingDerivationsEvent createNoPendingsEvent(Listenable source, Listenable trigger, long assemblyId)
    {
        return new PendingDerivationsEventImpl(source, trigger, TableElementEventType.OnNoPendings, assemblyId);
    }
    
    private PendingDerivationsEventImpl(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId)
    {
        super(evT, source, trigger, assemblyId);
    }
    
    @Override
    public boolean isPendingDerivations() { return getType() == TableElementEventType.OnPendings; }
}
