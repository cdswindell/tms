package org.tms.api.event;

import org.tms.api.TableElement;

public class PendingDerivationsEvent extends TableElementEvent
{
    private static final long serialVersionUID = -5965616786722158812L;

    public static final PendingDerivationsEvent createPendingsEvent(TableElement te, long timeStamp)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnPendings, timeStamp);
    }
    
    public static final PendingDerivationsEvent createNoPendingsEvent(TableElement te, long timeStamp)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnNoPendings, timeStamp);
    }
    
    private PendingDerivationsEvent(TableElement source, TableElementEventType evT, long timeStamp)
    {
        super(evT, source, timeStamp);
    }
    
    public boolean isPendingDerivations() { return getType() == TableElementEventType.OnPendings; }
}
