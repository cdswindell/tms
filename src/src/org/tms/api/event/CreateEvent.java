package org.tms.api.event;

import org.tms.api.ElementType;
import org.tms.api.TableElement;

public class CreateEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = -8266816554096958366L;

    public static final CreateEvent createOnBefore(TableElement parent, ElementType et)
    {
        return new CreateEvent(parent, TableElementEventType.OnBeforeCreate, et);
    }
    
    public static final CreateEvent createOn(TableElement te)
    {
        return new CreateEvent(te, TableElementEventType.OnCreate, null);
    }
    
    private ElementType m_createdElementType;
    
    private CreateEvent(TableElement source, TableElementEventType evT, ElementType et)
    {
        super(evT, source);
        
        m_createdElementType = isBefore() ? et : source.getElementType();
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeCreate; }
    
    public ElementType getCreatedElementType() { return m_createdElementType; }
}
