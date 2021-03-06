package org.tms.api.derivables;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.tms.teq.DerivationImpl;

public interface Derivation extends BasicFormula
{
    public static void postResult(UUID transactionId, double rslt)
    {
        DerivationImpl.postResult(transactionId, rslt);
    }

    public static void postResult(UUID transactionId, Token rslt)
    {
        DerivationImpl.postResult(transactionId, rslt);
    }

    public static UUID getTransactionID()
    {
        return DerivationImpl.getTransactionID();
    }
    
    public void recalculateTarget();

    public boolean isPeriodic();
    public long getPeriodInMilliSeconds();
    public void recalculateEvery(long frequency);
    public void recalculateEvery(long frequency, TimeUnit unit);
}
