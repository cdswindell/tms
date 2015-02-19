package org.tms.api.event;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface EventProcessorThreadPool
{
    public long getKeepAliveTime(TimeUnit unit);
    public void setKeepAliveTime(long time, TimeUnit unit);
    
    boolean allowsCoreThreadTimeOut();
    public void allowCoreThreadTimeOut(boolean allowCoreThreadTimeout);
    
    public int getMaximumPoolSize();
    public void setMaximumPoolSize(int maxPoolSize);
    
    public int getCorePoolSize();
    public void setCorePoolSize(int corePoolSize);
    
    public void submitEvents(Collection<TableElementEvent> events);
    public boolean remove(TableElementEvent e);
    public void shutdown();
}
