package org.tms.teq;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.tms.api.Cell;
import org.tms.api.Operator;
import org.tms.api.TableElement;
import org.tms.teq.Derivation.DerivationContext;
import org.tms.teq.PendingState.BlockedStatisticState;

public class PendingStatistic
{
    private TableElement m_refElement;
    private Set<PendingState> m_blockedCells;
    private Set<PendingState> m_blockingCells;
    private Semaphore m_lock;
    private boolean m_valid;
    private Derivation m_derivation;
    private Operator m_oper;
    
    protected PendingStatistic(Derivation deriv, Operator oper, TableElement ref, Set<PendingState> blockedOnSet)
    {
        m_blockedCells = new LinkedHashSet<PendingState>();
        m_blockingCells = new LinkedHashSet<PendingState>();
        m_refElement = ref;
        m_derivation = deriv;
        m_lock = new Semaphore(1);
        m_oper = oper;
        m_valid = true;
        
        deriv.registerPendingStatistic(this);
        
        lock();
        try {
            // register this PendingStatistic with each dependent calculation
            boolean blockedOnAny = false;
            for (PendingState ps : blockedOnSet) {
                if (ps != null) {
                    ps.lock();
                    try {                      
                        if (ps.registerBlockedDerivation(new BlockedStatisticState(this, deriv))) {
                            m_blockingCells.add(ps);
                            blockedOnAny = true;
                        }
                    }
                    finally {
                        ps.unlock();
                    }
                }
            }
            
            // register with parent derivation, so we don't have to 
            if (!blockedOnAny)
                delete();
        }
        finally {
            unlock();
        }
    }
    
    protected boolean isBlockedOnAny()
    {
        return !m_blockingCells.isEmpty();
    }
    
    public boolean isValid()
    {
        return m_valid;
    }
    
    private void markInvalid()
    {
        m_valid = false;
    }
    
    protected TableElement getReferencedElement()
    {
        return m_refElement;
    }
    
    protected void lock()
    {
        try
        {
            if (m_lock != null)
                m_lock.acquire();
        }
        catch (InterruptedException e) { } // noop;
    }
    
    protected void unlock()
    {
        if (m_lock != null)
            m_lock.release();
    }
    
    protected Derivation getDerivation()
    {
        return m_derivation;
    }

    protected boolean registerBlockedDerivation(PendingState ps)
    {
        if (isValid() && ps != null) {
            ps.lock();
            try {
                if (ps.isValid()) {
                    return m_blockedCells.add(ps);
                }
            }
            finally {
                ps.unlock();
            }                   
        }
        
        return false;
    }

    protected void delete()
    {
        markInvalid();       
        for (PendingState ps : m_blockedCells)
        {
            if (ps == null)
                continue;
            
            ps.lock();
            try {
                if (ps.isValid()) {
                    ps.delete();
                }
            }
            finally {
                ps.unlock();
            }
        }  
        
        m_blockedCells.clear();
        m_derivation.deregisterPendingStatistic(this);
    }

    protected void updateRootPending(PendingState ps, PendingState newPs)
    {
        m_blockingCells.remove(ps);
        m_blockingCells.add(newPs);
        
    }
    
    protected void unblockStatistic(PendingState tps)
    {
        Set<PendingState> toRemove = new HashSet<PendingState>();
        
        lock();
        try {           
            // check that parent reference is still valid, we need to do this
            // on every iteration           
            if (m_refElement.isInvalid()) {
                delete();
                return;
            }
                            
            // see if any of the cells in the reference are still pending, 
            // if so, continue waiting
            for (Cell c: m_refElement.cells()) {
                if (isBlockingCell(c))
                        return; 
                
                if (m_refElement.isInvalid()) {
                    delete();
                    return;
                }
            } 
            
            DerivationContext dc = new DerivationContext();
            for (PendingState ps : m_blockedCells) {
                Derivation psDeriv = null;
                if (ps != null && ps.isValid() && (psDeriv = ps.getDerivation()) != null) {
                    try {
                        // blocks on access to ps
                        ps.reevaluate(dc);                        
                    }
                    catch (PendingDerivationException pc)
                    {
                        // if derivation is being cleared, it could go away,
                        // get exclusive access while we perform cache
                        ps.lock();
                        try {
                            if (ps.isValid()) {
                                PendingState newPs = pc.getPendingState();
                                newPs.registerBlockedDerivations(ps);
                                psDeriv.cacheDeferredCalculation(newPs, dc);
                            }
                            else {
                                ps.delete();
                                dc.clearPendings();
                            }
                        }
                        finally {
                            ps.unlock();
                        }
                    }
                    catch (BlockedDerivationException e) {} // noop
                }
            }
            
            // process any spawned calculations
            dc.processPendings();
            
            // remove the cells we processed so they are not cleared by delete
            m_blockingCells.removeAll(toRemove);   
            toRemove.clear();
            
            // we're all done. delete ourselves
            if (m_blockingCells.isEmpty())
                delete();
        }
        finally { 
            m_blockingCells.removeAll(toRemove);           
            unlock();
        }
    }

    private boolean isBlockingCell(Cell c)
    {
        if (c == null)
            return false;
        
        if (c.isDerived()) {
            List<TableElement> affectedBy = c.getAffectedBy();
            if (affectedBy !=null && affectedBy.contains(m_refElement)) {
                return false;
            }
        }
        
        return c.isPendings();
    }
    
    public String toString()
    {
        return String.format("Pending Statistic: %s (%s)", m_oper.getLabel(), m_refElement);
    }
}
