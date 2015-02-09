package org.tms.teq;

public class BlockedDerivationException extends Exception
{
    private static final long serialVersionUID = 2733979614193783470L;
    
    private PendingState m_pendingState;
    
    public BlockedDerivationException()
    {
        m_pendingState = null;
    }
    
    public BlockedDerivationException(PendingState ps)
    {
        m_pendingState = ps;
    }
    
    public PendingState getPendingState()
    {
        return m_pendingState;
    }
}
