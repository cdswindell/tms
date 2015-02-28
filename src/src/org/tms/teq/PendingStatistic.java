package org.tms.teq;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.tms.api.Cell;
import org.tms.api.TableElement;
import org.tms.api.derivables.Operator;
import org.tms.teq.DerivationImpl.DerivationContext;
import org.tms.teq.PendingState.AwaitingState;
import org.tms.teq.PendingState.BlockedStatisticState;

public class PendingStatistic
{
    private TableElement m_refElement;
    private Set<PendingState> m_blockedCells;
    private Set<Cell> m_blockedOnCells;
    private Semaphore m_lock;
    private boolean m_valid;
    private DerivationImpl m_derivationImpl;
    private Operator m_oper;
    private boolean m_useAccelerator;
    
    protected PendingStatistic(DerivationImpl deriv, Operator oper, TableElement ref, Set<PendingState> blockedOnSet)
    {
        m_blockedCells = new LinkedHashSet<PendingState>();
        m_blockedOnCells = new LinkedHashSet<Cell>();
        m_refElement = ref;
        m_derivationImpl = deriv;
        m_lock = new Semaphore(1);
        m_oper = oper;
        m_useAccelerator = true;
        m_valid = true;
        
        // register with parent derivation, so we don't have to 
        deriv.registerPendingStatistic(this);

        lock();
        try {
            boolean blockedOnAny = false;
            for (PendingState ps : blockedOnSet) {
                if (ps != null) {
                    ps.lock();
                    try {                      
                        if (ps.isStillPending()) {
                            DerivationImpl d = ps.getDerivation();
                            assert d != null : "DerivationImpl Required";
                            
                            Cell blockingCell  = ps.getPendingCell();
                            assert blockingCell != null : "Blocked Cell Required";
                            
                            // register this PendingStatistic with each dependent cell
                            m_blockedOnCells.add(blockingCell);
                            d.registerBlockingCell(blockingCell, new BlockedStatisticState(this, deriv));
                            blockedOnAny = true;
                        }
                    }
                    finally {
                        ps.unlock();
                    }
                }
            }
            
            if (!blockedOnAny) 
                delete();
        }
        finally {
            unlock();
        }
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_derivationImpl == null) ? 0 : m_derivationImpl.hashCode());
        result = prime * result + ((m_refElement == null) ? 0 : m_refElement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PendingStatistic other = (PendingStatistic) obj;
        if (m_derivationImpl == null)
        {
            if (other.m_derivationImpl != null) return false;
        }
        else if (!m_derivationImpl.equals(other.m_derivationImpl)) return false;
        if (m_refElement == null)
        {
            if (other.m_refElement != null) return false;
        }
        else if (!m_refElement.equals(other.m_refElement)) return false;
        return true;
    }

    protected boolean isBlockedOnAny()
    {
        return isValid() || !m_blockedOnCells.isEmpty();
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
    
    protected DerivationImpl getDerivation()
    {
        return m_derivationImpl;
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
        m_blockedOnCells.clear();
        m_derivationImpl.deregisterPendingStatistic(this);
    }

    protected boolean unblockStatistic(Cell unblockedCell)
    {
        Set<PendingState> toRemove = new HashSet<PendingState>();
        
        lock();
        try {           
            // check that parent reference is still valid, we need to do this
            // on every iteration           
            if (m_refElement.isInvalid()) {
                delete();
                return false;
            }
            
            /*
             * If threading support is working properly, all unblocked cells passed into
             * this method *should* also be in the m_blockedOnCells set. If this set is not
             * empty, the PendingStatistic is still blocked.
             * 
             * As a safeguard to catch thread syncing errors, if we encounter an unblocked 
             * cell that is *not* in m_blockedOnCells, disable the use of the accelerator
             * and rely on brute force check
             */
            boolean removedCell = false;
            if (unblockedCell != null) {
                removedCell = m_blockedOnCells.remove(unblockedCell);
                if (m_useAccelerator && removedCell && !m_blockedOnCells.isEmpty())
                    return removedCell;
            }

            if (m_useAccelerator && unblockedCell != null && !removedCell) 
                m_useAccelerator = false;

            // see if any of the cells in the reference are still pending, 
            // if so, continue waiting
            // if threading synchronization is working correctly, this final check 
            // shouldn't be needed...
            for (Cell c: m_refElement.cells()) {
                if (isBlockingCell(c)) 
                    return removedCell ;
                
                if (m_refElement.isInvalid()) {
                    delete();
                    return false;
                }
            } 
            
            DerivationContext dc = new DerivationContext();
            for (PendingState ps : m_blockedCells) {
                DerivationImpl psDeriv = null;
                if (ps != null && ps.isValid() && (psDeriv = ps.getDerivation()) != null) {
                    try {
                        // blocks on access to ps
                        ps.reevaluate(dc);  
                        
                        ps.unblockDerivations();
                    }
                    catch (PendingDerivationException pc)
                    {
                        // if derivation is being cleared, it could go away,
                        // get exclusive access while we perform cache
                        ps.lock();
                        try {
                            if (ps.isValid()) {
                                AwaitingState newPs = pc.getAwaitingState();
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
                else
                    System.out.println("Encountered condition that deletes stat without evaluation");
            }
            
            // remove the cells we processed so they are not cleared by delete
            m_blockedOnCells.removeAll(toRemove);   
            toRemove.clear();
            
            // process any spawned calculations
            dc.processPendings();
            
            // we're all done. delete ourselves
            if (m_blockedOnCells.isEmpty())
                delete();
            
            return true;
        }
        finally { 
            m_blockedOnCells.removeAll(toRemove);           
            unlock();
        }       
    }

    private boolean isBlockingCell(Cell c)
    {
        if (c == null)
            return false;
        
        if (c.isDerived()) {
            List<TableElement> affectedBy = c.getAffectedBy();
            if (affectedBy !=null && affectedBy.contains(m_refElement)) 
                return false;
        }
        
        return c.isPendings();
    }
    
    public String toString()
    {
        return String.format("Pending Statistic: %s (%s)", m_oper.getLabel(), m_refElement);
    }
}
