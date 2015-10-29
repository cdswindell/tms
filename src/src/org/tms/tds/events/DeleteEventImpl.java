package org.tms.tds.events;

import org.tms.api.events.DeleteEvent;
import org.tms.api.events.Listenable;
import org.tms.api.events.TableElementEvent;
import org.tms.api.events.TableElementEventType;


public class DeleteEventImpl extends BaseTableElementEvent implements DeleteEvent
{
    private static final long serialVersionUID = -2123789762649394968L;

    static final TableElementEvent createOnBefore(Listenable source, Listenable trigger, long assemblyId)
    {
        return new DeleteEventImpl(source, trigger, TableElementEventType.OnBeforeDelete, assemblyId);
    }
    
    static final TableElementEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new DeleteEventImpl(source, trigger, TableElementEventType.OnDelete, assemblyId);
    }
    
    private DeleteEventImpl(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId)
    {
        super(evT, source, trigger, assemblyId);        
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeDelete; }
}
