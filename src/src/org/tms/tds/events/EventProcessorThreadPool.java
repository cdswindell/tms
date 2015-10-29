package org.tms.tds.events;

import java.util.Collection;

import org.tms.api.events.TableElementEvent;

public interface EventProcessorThreadPool
{
    public void submitEvents(Collection<TableElementEvent> events);
    public boolean remove(TableElementEvent e);
    public void shutdownEventProcessorThreadPool();
}
