package org.tms.api.event;

public class DeleteEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = -2123789762649394968L;

    static final TableElementEvent createOnBefore(Listenable source, Listenable trigger, long assemblyId)
    {
        return new DeleteEvent(source, trigger, TableElementEventType.OnBeforeDelete, assemblyId);
    }
    
    static final TableElementEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new DeleteEvent(source, trigger, TableElementEventType.OnDelete, assemblyId);
    }
    
    private DeleteEvent(Listenable source, Listenable trigger, TableElementEventType evT, long assemblyId)
    {
        super(evT, source, trigger, assemblyId);        
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeDelete; }
}
