package org.tms.api.derivables;

import java.util.UUID;

import org.tms.teq.DerivationImpl;


public interface Derivation
{
    public static void postResult(UUID transactionId, double rslt)
    {
        DerivationImpl.postResult(transactionId, rslt);
    }


    public static UUID getTransactionID()
    {
        return DerivationImpl.getTransactionID();
    }
    

    String getAsEnteredExpression();

}
