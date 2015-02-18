package org.tms.api.event;

import org.tms.api.TableElement;

public class DeleteEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = 2815967836341221707L;

    public static final TableElementEvent createOnBefore(TableElement parent, long timeStamp)
    {
        return new DeleteEvent(parent, TableElementEventType.OnBeforeDelete, timeStamp);
    }
    
    public static final TableElementEvent createOn(TableElement te, long timeStamp)
    {
        return new DeleteEvent(te, TableElementEventType.OnDelete, timeStamp);
    }
    
    private DeleteEvent(TableElement source, TableElementEventType evT, long timeStamp)
    {
        super(evT, source, timeStamp);        
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeDelete; }
}
