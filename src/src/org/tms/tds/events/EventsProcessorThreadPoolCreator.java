package org.tms.tds.events;

public interface EventsProcessorThreadPoolCreator 
{
	public void createEventProcessorThreadPool();
    public boolean isEventsNotifyInSameThread();
    public void setEventsNotifyInSameThread(boolean value);
}
