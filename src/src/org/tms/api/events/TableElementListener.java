package org.tms.api.events;

@FunctionalInterface
public interface TableElementListener
{
    public void eventOccured(TableElementEvent e);
}
