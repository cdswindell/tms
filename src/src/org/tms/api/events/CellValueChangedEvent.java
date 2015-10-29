package org.tms.api.events;


public interface CellValueChangedEvent extends TableElementEvent, OnBeforeEvent
{
    public boolean isBefore();
    
    public boolean isOldValueAvailable();
    
    public Object getOldValue();
    
    public Object getNewValue();
}
