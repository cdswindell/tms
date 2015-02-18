package org.tms.api.event;

import org.tms.api.TableElement;

public class DeleteEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = -1469314176385020764L;

    public static final TableElementEvent createOnBefore(TableElement parent)
    {
        return new DeleteEvent(parent, TableElementEventType.OnBeforeDelete);
    }
    
    public static final TableElementEvent createOn(TableElement te)
    {
        return new DeleteEvent(te, TableElementEventType.OnDelete);
    }
    
    private DeleteEvent(TableElement source, TableElementEventType evT)
    {
        super(evT, source);        
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeDelete; }
}
