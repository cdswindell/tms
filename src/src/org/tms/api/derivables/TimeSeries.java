package org.tms.api.derivables;

import java.util.UUID;

import org.tms.teq.DerivationImpl;

public interface TimeSeries extends BasicFormula
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
}
