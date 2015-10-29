package org.tms.api.events;

import org.tms.api.TableElement;

public interface TableElementEvent
{
    public Listenable getSource();
    public TableElementEventType getType();
    public Listenable getTrigger();
    public boolean isTriggered();
    public long getAssemblyId();
    public long getTimeStamp();
    public TableElement getTable();
}
