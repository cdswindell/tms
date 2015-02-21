package org.tms.api.event;

@FunctionalInterface
public interface TableElementListener
{
    public void eventOccured(TableElementEvent e);
}
