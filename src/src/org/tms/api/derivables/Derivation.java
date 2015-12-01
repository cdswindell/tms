package org.tms.api.derivables;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.teq.DerivationImpl;


public interface Derivation
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
    
    Table getTable();
    TableContext getTableContext();

    public String getExpression();
    public String getAsEnteredExpression();
    public String getPostfixExpression();
    public String getInfixExpression();
    public boolean isParsed();
    public boolean isConverted();

    public void recalculateTarget();

    public boolean isPeriodic();
    public void recalculateEvery(int frequency);
    public void recalculateEvery(int frequency, TimeUnit unit);
}
