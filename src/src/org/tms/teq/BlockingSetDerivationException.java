package org.tms.teq;

import java.util.Set;

import org.tms.api.TableElement;

public class BlockingSetDerivationException extends BlockedDerivationException
{
    private static final long serialVersionUID = -3577341853539801587L;
    
    private PendingStatistic m_pendingStatistic;
    private Set<BaseAsyncState> m_pendingStates;
    private TableElement m_refElement;
    
    public BlockingSetDerivationException(Set<BaseAsyncState> blockedOnSet, TableElement ref)
    {
        m_pendingStates = blockedOnSet;
        m_refElement = ref;
    }
    
    public BlockingSetDerivationException(PendingStatistic pendingStat)
    {
        m_pendingStatistic = pendingStat;
        m_refElement = pendingStat.getReferencedElement();
    }

    public Set<BaseAsyncState> getBlockingSet()
    {
        return m_pendingStates;
    }
    
    public PendingStatistic getPendingStatistic()
    {
        return m_pendingStatistic;
    }
    
    public TableElement getReferenceElement()
    {
        return m_refElement;
    }
    
    @Override
    public BaseAsyncState getPendingState()
    {
        return null;
    }
}
