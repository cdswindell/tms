package org.tms.api.event;

import org.tms.api.ElementType;
import org.tms.api.TableElement;

public class CreateEvent extends TableElementEvent implements OnBeforeEvent
{
    private static final long serialVersionUID = -8266816554096958366L;

    public static final CreateEvent createOnBefore(Listenable te, long timeStamp, ElementType et)
    {
        return new CreateEvent(te, TableElementEventType.OnBeforeCreate, timeStamp, et);
    }
    
    public static final CreateEvent createOn(Listenable te, long timeStamp)
    {
        return new CreateEvent(te, TableElementEventType.OnCreate, timeStamp, null);
    }
    
    private ElementType m_createdElementType;
    
    private CreateEvent(Listenable source, TableElementEventType evT, long timeStamp, ElementType et)
    {
        super(evT, source, timeStamp);
        
        m_createdElementType = isBefore() ? et : ((TableElement)source).getElementType();
    }
    
    @Override
    public boolean isBefore() { return getType() == TableElementEventType.OnBeforeCreate; }
    
    public ElementType getCreatedElementType() { return m_createdElementType; }
}
