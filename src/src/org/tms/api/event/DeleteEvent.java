package org.tms.api.event;

public class DeleteEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = 2815967836341221707L;

    public static final TableElementEvent createOnBefore(Listenable te, long timeStamp)
    {
        return new DeleteEvent(te, TableElementEventType.OnBeforeDelete, timeStamp);
    }
    
    public static final TableElementEvent createOn(Listenable te, long timeStamp)
    {
        return new DeleteEvent(te, TableElementEventType.OnDelete, timeStamp);
    }
    
    private DeleteEvent(Listenable source, TableElementEventType evT, long timeStamp)
    {
        super(evT, source, timeStamp);        
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeDelete; }
}
