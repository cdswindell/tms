package org.tms.api;

import java.util.UUID;

public interface DerivableThreadPool
{

    void submitCalculation(UUID transactionId, Runnable r);
    void shutdown();

}
