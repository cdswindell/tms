package org.tms.api.events;

public interface OnBeforeEvent
{
    /**
     * Returns {@code true} if the event is fired before the operation
     * @return true if the event is fired before the operation
     */
    public boolean isBefore();
}