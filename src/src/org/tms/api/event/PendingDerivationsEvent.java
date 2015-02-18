package org.tms.api.event;

import org.tms.api.TableElement;

public class PendingDerivationsEvent extends TableElementEvent
{
    private static final long serialVersionUID = -5965616786722158812L;

    public static final PendingDerivationsEvent createPendingsEvent(TableElement te)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnPendings);
    }
    
    public static final PendingDerivationsEvent createNoPendingsEvent(TableElement te)
    {
        return new PendingDerivationsEvent(te, TableElementEventType.OnNoPendings);
    }
    
    private PendingDerivationsEvent(TableElement source, TableElementEventType evT)
    {
        super(evT, source);
    }
    
    public boolean isPendingDerivations() { return getType() == TableElementEventType.OnPendings; }
}
