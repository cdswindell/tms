package org.tms.api.derivables;

import java.util.UUID;

public interface DerivableThreadPool
{
    public void submitCalculation(UUID transactionId, Runnable r);
    public boolean remove(UUID transactionId);
    public void shutdownDerivableThreadPool();
}
