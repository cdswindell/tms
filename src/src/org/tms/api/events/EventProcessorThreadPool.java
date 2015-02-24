package org.tms.api.events;

import java.util.Collection;

public interface EventProcessorThreadPool
{
    public void submitEvents(Collection<TableElementEvent> events);
    public boolean remove(TableElementEvent e);
    public void shutdownEventProcessorThreadPool();
}
