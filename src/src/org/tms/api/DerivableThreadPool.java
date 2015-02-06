package org.tms.api;

import java.util.UUID;

public interface DerivableThreadPool
{
    public int getThreadKeepAliveTimeout();
    public void setThreadKeepAliveTimeout(int keepAliveTimeout);
    
    boolean isAllowCoreThreadTimeout();
    public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout);
    
    public int getNumMaxPoolThreads();
    public void setNumMaxPoolThreads(int maxPoolSize);
    
    public int getNumCorePoolThreads();
    public void setNumCorePoolThreads(int corePoolSize);
    
    void submitCalculation(UUID transactionId, Runnable r);
    void shutdown();
}
