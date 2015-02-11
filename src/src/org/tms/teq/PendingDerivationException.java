package org.tms.teq;

import org.tms.teq.PendingState.AwaitingState;

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
    
    public AwaitingState getAwaitingState()
    {
        if (m_pendingState != null && m_pendingState instanceof AwaitingState)
            return (AwaitingState)m_pendingState;
        else
            return null;
    }
}
