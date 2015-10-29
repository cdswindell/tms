package org.tms.api.events;

import org.tms.api.ElementType;

public interface CreateEvent extends TableElementEvent, OnBeforeEvent
{
    public ElementType getCreatedElementType();
}
