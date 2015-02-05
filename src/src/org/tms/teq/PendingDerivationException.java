package org.tms.teq;

import org.tms.teq.PostfixStackEvaluator.PendingState;

public class PendingDerivationException extends Exception
{
    private static final long serialVersionUID = 6277341563377116215L;
    
    private PendingState m_pendingState;
    
    public PendingDerivationException(PendingState ps)
    {
        m_pendingState = ps;
    }
    
    public PendingState getPendingState()
    {
        return m_pendingState;
    }
}
