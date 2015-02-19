package org.tms.api.derivables;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface DerivableThreadPool
{
    public long getKeepAliveTime(TimeUnit unit);
    public void setKeepAliveTime(long time, TimeUnit unit);
    
    boolean allowsCoreThreadTimeOut();
    public void allowCoreThreadTimeOut(boolean allowCoreThreadTimeout);
    
    public int getMaximumPoolSize();
    public void setMaximumPoolSize(int maxPoolSize);
    
    public int getCorePoolSize();
    public void setCorePoolSize(int corePoolSize);
    
    public void submitCalculation(UUID transactionId, Runnable r);
    public boolean remove(Runnable r);
    public void shutdown();
}
