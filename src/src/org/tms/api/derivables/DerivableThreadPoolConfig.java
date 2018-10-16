package org.tms.api.derivables;

import java.util.concurrent.TimeUnit;

public interface DerivableThreadPoolConfig
{	
	public boolean isPendingThreadPoolEnabled();
    public void setPendingThreadPoolEnabled(boolean threadPoolEnabled);

    public boolean isPendingAllowsCoreThreadTimeOut();
    public void setPendingAllowCoreThreadTimeOut(boolean allowCoreThreadTimeout);

	public TimeUnit getPendingKeepAliveTimeUnit();
    public long getPendingKeepAliveTime(TimeUnit unit);
    public void setPendingKeepAliveTime(long time, TimeUnit unit);

	public int getPendingCorePoolSize();
	public void setPendingCorePoolSize(int corePoolSize);

	public int getPendingMaximumPoolSize();
	public void setPendingMaximumPoolSize(int maxPoolSize);
}
