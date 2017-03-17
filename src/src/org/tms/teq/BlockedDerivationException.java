package org.tms.teq;

public class BlockedDerivationException extends Exception
{
    private static final long serialVersionUID = 2733979614193783470L;
    
    private BaseAsyncState m_pendingState;
    
    public BlockedDerivationException()
    {
        m_pendingState = null;
    }
    
    public BlockedDerivationException(BaseAsyncState ps)
    {
        m_pendingState = ps;
    }
    
    public BaseAsyncState getPendingState()
    {
        return m_pendingState;
    }
}
