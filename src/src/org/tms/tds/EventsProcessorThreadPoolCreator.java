package org.tms.tds;

interface EventsProcessorThreadPoolCreator 
{
	public void createEventProcessorThreadPool();
    public boolean isEventsNotifyInSameThread();
    public void setEventsNotifyInSameThread(boolean value);
}
