package org.tms.api.events;

import org.tms.api.ElementType;
import org.tms.api.TableElement;

public class CreateEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = 3756424038671548782L;

    static final CreateEvent createOnBefore(Listenable source, Listenable trigger, long assemblyId, ElementType et)
    {
        return new CreateEvent(source, trigger, TableElementEventType.OnBeforeCreate, assemblyId, et);
    }
    
    static final CreateEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new CreateEvent(source, trigger, TableElementEventType.OnCreate, assemblyId, null);
    }
    
    private ElementType m_createdElementType;
    
    private CreateEvent(Listenable source, Listenable trigger, TableElementEventType evT, long timeStamp, ElementType et)
    {
        super(evT, source, trigger, timeStamp);
        
        m_createdElementType = isBefore() ? et : ((TableElement)source).getElementType();
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeCreate; }
    
    public ElementType getCreatedElementType() { return m_createdElementType; }
}
