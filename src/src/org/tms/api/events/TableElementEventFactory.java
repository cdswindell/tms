package org.tms.api.events;

import org.tms.api.ElementType;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnimplementedException;

class TableElementEventFactory
{
    static final TableElementEvent createEvent(Listenable source, Listenable trigger, TableElementEventType evT, 
                                               long assemblyId, Object... args)
    {
        if (source == null) 
            throw new IllegalTableStateException("TableElement required");
        
        if (evT == null) 
            throw new IllegalTableStateException("TableElementEventType required");
        
        int numArgs = args != null ? args.length : 0;
        switch (evT) {
            case OnBeforeNewValue:
                if (numArgs != 2)
                    throw new InvalidException(String.format("%s requires a before and after value", evT));
                else
                    return CellValueChangedEvent.createOnBefore(source, trigger, assemblyId, args[0], args[1]);
                
            case OnNewValue:
                if (numArgs == 0)
                    return CellValueChangedEvent.createOn(source, trigger, assemblyId, null);
                else if (numArgs == 1)
                    return CellValueChangedEvent.createOn(source, trigger, assemblyId, args[0]);
                else
                    return CellValueChangedEvent.createOn(source, trigger, assemblyId, args[0], args[1]);
                
            case OnPendings:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return PendingDerivationsEvent.createPendingsEvent(source, trigger, assemblyId);
                
            case OnNoPendings:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return PendingDerivationsEvent.createNoPendingsEvent(source, trigger, assemblyId);
                
            case OnBeforeCreate:
                if (numArgs < 1 || !(args[0] instanceof ElementType))
                    throw new InvalidException(String.format("%s requires the ElementType that is being created", evT));
                else
                    return CreateEvent.createOnBefore(source, trigger, assemblyId, (ElementType)args[0]);
                
            case OnCreate:
                if (numArgs >0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return CreateEvent.createOn(source, trigger, assemblyId);
                
            case OnBeforeDelete:
                if (numArgs >0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return DeleteEvent.createOnBefore(source, trigger, assemblyId);
                
            case OnDelete:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return DeleteEvent.createOn(source, trigger, assemblyId);
                
            case OnRecalculate:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return RecalculateEvent.createOn(source, trigger, assemblyId);
                
            default:
                throw new UnimplementedException("Unimplemented Event Type:" + evT);
        }
    }
}
