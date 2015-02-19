package org.tms.api.event;

import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnimplementedException;

class TableElementEventFactory
{
    static final TableElementEvent createEvent(TableElement te, TableElementEventType evT, long timeStamp, Object... args)
    {
        if (te == null) 
            throw new IllegalTableStateException("TableElement required");
        
        if (evT == null) 
            throw new IllegalTableStateException("TableElementEventType required");
        
        int numArgs = args != null ? args.length : 0;
        switch (evT) {
            case OnBeforeNewValue:
                if (numArgs != 2)
                    throw new InvalidException(String.format("%s requires a before and after value", evT));
                else
                    return CellValueChangedEvent.createOnBefore(te, timeStamp, args[0], args[1]);
                
            case OnNewValue:
                if (numArgs < 1)
                    throw new InvalidException(String.format("%s requires at least one (1) value", evT));
                else if (numArgs == 1)
                    return CellValueChangedEvent.createOn(te, timeStamp, args[0]);
                else
                    return CellValueChangedEvent.createOn(te, timeStamp, args[0], args[1]);
                
            case OnPendings:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return PendingDerivationsEvent.createPendingsEvent(te, timeStamp);
                
            case OnNoPendings:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return PendingDerivationsEvent.createNoPendingsEvent(te, timeStamp);
                
            case OnBeforeCreate:
                if (numArgs < 1 || !(args[0] instanceof ElementType))
                    throw new InvalidException(String.format("%s requires the ElementType that is being created", evT));
                else
                    return CreateEvent.createOnBefore(te, timeStamp, (ElementType)args[0]);
                
            case OnCreate:
                if (numArgs >0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return CreateEvent.createOn(te, timeStamp);
                
            case OnBeforeDelete:
                if (numArgs >0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return DeleteEvent.createOnBefore(te, timeStamp);
                
            case OnDelete:
                if (numArgs > 0)
                    throw new InvalidException(String.format("%s does not require any values", evT));
                else
                    return DeleteEvent.createOn(te, timeStamp);
                
            default:
                throw new UnimplementedException("Unimplemented Event Type:" + evT);
        }
    }
}
