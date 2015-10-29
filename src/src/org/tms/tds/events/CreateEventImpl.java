package org.tms.tds.events;

import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.events.CreateEvent;
import org.tms.api.events.Listenable;
import org.tms.api.events.TableElementEventType;

public class CreateEventImpl extends BaseTableElementEvent implements CreateEvent
{
    private static final long serialVersionUID = 3756424038671548782L;

    static final CreateEvent createOnBefore(Listenable source, Listenable trigger, long assemblyId, ElementType et)
    {
        return new CreateEventImpl(source, trigger, TableElementEventType.OnBeforeCreate, assemblyId, et);
    }
    
    static final CreateEvent createOn(Listenable source, Listenable trigger, long assemblyId)
    {
        return new CreateEventImpl(source, trigger, TableElementEventType.OnCreate, assemblyId, null);
    }
    
    private ElementType m_createdElementType;
    
    private CreateEventImpl(Listenable source, Listenable trigger, TableElementEventType evT, long timeStamp, ElementType et)
    {
        super(evT, source, trigger, timeStamp);
        
        m_createdElementType = isBefore() ? et : ((TableElement)source).getElementType();
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeCreate; }
    
    @Override
    public ElementType getCreatedElementType() { return m_createdElementType; }

}
