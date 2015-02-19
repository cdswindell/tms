package org.tms.api.event;

public class PendingDerivationsEvent extends TableElementEvent
{
    private static final long serialVersionUID = -4497772315539485507L;

    public static final PendingDerivationsEvent createPendingsEvent(Listenable te, long timeStamp)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnPendings, timeStamp);
    }
    
    public static final PendingDerivationsEvent createNoPendingsEvent(Listenable te, long timeStamp)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnNoPendings, timeStamp);
    }
    
    private PendingDerivationsEvent(Listenable source, TableElementEventType evT, long timeStamp)
    {
        super(evT, source, timeStamp);
    }
    
    public boolean isPendingDerivations() { return getType() == TableElementEventType.OnPendings; }
}
