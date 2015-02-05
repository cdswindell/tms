package org.tms.api;

import java.util.UUID;

public interface PendingThreadPool
{

    void submitCalculation(UUID transactionId, Runnable r);
    void shutdown();

}
