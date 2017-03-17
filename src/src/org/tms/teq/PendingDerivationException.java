package org.tms.teq;

import org.tms.teq.BaseAsyncState.PendingState;

public class PendingDerivationException extends Exception
{
    private static final long serialVersionUID = 6277341563377116215L;
    
    private BaseAsyncState m_pendingState;
    
    public PendingDerivationException(BaseAsyncState ps)
    {
        m_pendingState = ps;
    }
    
    public BaseAsyncState getPendingState()
    {
        return m_pendingState;
    }
    
    public PendingState getAwaitingState()
    {
        if (m_pendingState != null && m_pendingState instanceof PendingState)
            return (PendingState)m_pendingState;
        else
            return null;
    }
}
