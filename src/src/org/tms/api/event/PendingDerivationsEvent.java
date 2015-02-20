package org.tms.api.event;

public class PendingDerivationsEvent extends TableElementEvent
{
    private static final long serialVersionUID = 4434997403257589967L;

    static final PendingDerivationsEvent createPendingsEvent(Listenable source, Listenable trigger, long assemblyId)
    {
        return new PendingDerivationsEvent(source, trigger, TableElementEventType.OnPendings, assemblyId);
    }
    
    static final PendingDerivationsEvent createNoPendingsEvent(Listenable source, Listenable trigger, long assemblyId)
    {
        return new PendingDerivationsEvent(source, trigger, TableElementEventType.OnNoPendings, assemblyId);
    }
    
    private PendingDerivationsEvent(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId)
    {
        super(evT, source, trigger, assemblyId);
    }
    
    public boolean isPendingDerivations() { return getType() == TableElementEventType.OnPendings; }
}
