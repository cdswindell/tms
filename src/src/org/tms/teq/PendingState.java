package org.tms.teq;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.teq.Derivation.DerivationContext;

class PendingState
{
    private UUID m_uuid;
    private PostfixStackEvaluator m_pse;
    private Row m_curRow;
    private Column m_curCol;
    private Token m_pendingToken;
    private Object m_pendingIntermediate;
    private Semaphore m_lock;
    private boolean m_valid;
    private Set<PendingState> m_blockedCells;
    
    protected PendingState(PostfixStackEvaluator pse, Row row, Column col, Token tk)
    {
        m_uuid = Derivation.getTransactionID();
        m_pse = pse;
        m_curRow = row;
        m_curCol = col;
        m_pendingToken = tk;  
        m_pendingIntermediate = tk.getValue();
        m_valid = true;
        
        if (pse.getDerivation() != null)
            getDerivation().registerPendingState(this);
        
        m_blockedCells = Collections.synchronizedSet(new LinkedHashSet<PendingState>());
        
        m_lock = new Semaphore(1, true);
    }

    protected UUID getTransactionID()
    {
        return m_uuid;
    }
    
    protected Token getPendingToken()
    {
        return m_pendingToken;
    }
    
    protected Object getPendingIntermediate()
    {
        return m_pendingIntermediate;
    }
    
    protected Derivation getDerivation()
    {
        if (m_pse != null)
            return m_pse.getDerivation();
        else
            return null;
    }
    
    public Cell getPendingCell()
    {
        if (m_curCol != null && m_curCol.getTable() != null)
            return m_curCol.getTable().getCell(m_curRow, m_curCol);
        else
            return null;
    }
    
    protected Token reevaluate(DerivationContext dc) 
    throws PendingDerivationException, BlockedDerivationException
    {
        // if the target cell is no longer pending, just return, don't override value
        Cell cell = m_curCol.getTable().getCell(m_curRow, m_curCol);
        if (cell == null || !cell.isPendings())
            return Token.createNullToken();
        
        // calculate the new value, this could throw an exception if a pending
        // calculation or a blocked cell is encountered
        Token t = m_pse.reevaluate(m_curRow, m_curCol, dc);
        
        // we want exclusive access
        lock();
        try {
            // in the event this state has been invalidated (by clering the derivation
            // just return a null token and don't set cell
            if (!isValid())
                return Token.createNullToken();
            
            // apply numeric precision/rounding
            if (t.isNumeric() )
                t.setValue(getDerivation().applyPrecision(t.getNumericValue()));
            
            // remove the cell from the Derivation cache, preventing it
            // from connecting back to this PendingState
            Derivation.removePendingCellFromCache(cell);
            
            // check one more time for pending, just in case someone came and
            // cleared the pending state by clearing the parent derivation           
            if (!cell.isPendings())
                return Token.createNullToken();
            
            // set the cell value with the computed result
            m_curCol.getTable().setCellValue(m_curRow, m_curCol, t);
            
            //unblock the cells that were blocked on this pending
            unblockDerivations();
    
            // return the new token
            return t;
        }
        finally {
            unlock();
        }
    }

    protected boolean isRunnable()
    {
        return m_pendingIntermediate != null && m_pendingIntermediate instanceof Runnable;
    }
    
    protected Runnable getPendingRunnable()
    {
        if (isRunnable())
            return (Runnable)m_pendingIntermediate;
        else
            return null;
    }
    
    protected void submitCalculation()
    {
        lock();
        try {
            if (getDerivation() != null)
                getDerivation().submitCalculation(this);
        }
        finally {
            unlock();
        }
    }
    
    protected void lock()
    {
        try
        {
            if (m_lock != null)
                m_lock.acquire();
        }
        catch (InterruptedException e)
        {
            // noop;
        }
    }
    
    protected void unlock()
    {
        if (m_lock != null)
            m_lock.release();
    }

    protected boolean isLocked()
    {
        if (m_lock != null && m_lock.availablePermits() == 0)
            return true;
        else
            return false;
    }
    
    public void resetPendingState()
    {
        markInvalid();
        
        Cell cell = getPendingCell();
        if (cell != null)
            m_curCol.getTable().setCellValue(m_curRow, m_curCol, Token.createNullToken());
        
        // clear all pending cells
        m_blockedCells.forEach(p -> p.resetPendingState());
        m_blockedCells.clear();
        
        m_uuid = null;
        m_pse = null;
        m_pendingToken = null;  
        m_pendingIntermediate = null;     
    }

    public boolean isValid()
    {
        return m_valid;
    }
    
    private void markInvalid()
    {
        m_valid = false;
    }
    
    protected void registerBlockedDerivation(PendingState ps)
    {
        if (isValid() && ps != null)
            m_blockedCells.add(ps);
    }
    

    /**
     * Called while lock is held on specified pending state and
     * before this pending state's threads are started
     * @param ps
     */
    void registerBlockedDerivations(PendingState ps)
    {
        if (ps != null && ps.isValid()) {
            m_blockedCells.addAll(ps.m_blockedCells);
            ps.m_blockedCells.clear();
        }
    }
    
    /**
     * Unblock derivations blocked on the completion of a pending calculation
     */
    private void unblockDerivations()
    {
        // called while access to this pending state is locked
        if (!isValid())
            return;
        
        DerivationContext dc = new DerivationContext(); 
        for (PendingState ps : m_blockedCells) {
            if (!ps.isValid())
                continue;
            
            Derivation psDeriv = ps.getDerivation();
            if (psDeriv == null || psDeriv.isBeingDestroyed())
                continue;
            try
            {
                // reevaluate blocked cell, could run into  
                // another pending or blocked derivation
                ps.reevaluate(dc);
                
                // unblock derivations blocked on this one
                if (ps.isBlockedDerivations())
                    ps.unblockDerivations();
            }
            catch (PendingDerivationException pc)
            {
                // if derivation is being cleared, it could go away,
                // get exclusive access while we perform cache
                ps.lock();
                try {
                    if (ps.isValid()) 
                        psDeriv.cacheDeferredCalculation(pc.getPendingState(), dc);
                    else {
                        ps.resetPendingState();
                        dc.remove(ps);
                    }
                }
                finally {
                    ps.unlock();
                }
            }
            catch (BlockedDerivationException e) { } // noop;
            finally {
                if (ps.isBlockedDerivations()) 
                    ps.unblockDerivations();
                psDeriv.pendingStateProcessed(ps);
            }
        }
        
        // start any threads requested while unblocking
        dc.processPendings();
        
        // clear this list so nothing is reprocessed
        m_blockedCells.clear();
    }
    
    private boolean isBlockedDerivations()
    {
        return m_blockedCells != null ? !m_blockedCells.isEmpty() : false;
    }   
    
    protected boolean isBlocked()
    {
        return false;
    }
    
    public PendingState getRootPendingState()
    {
        return this;
    }
    
    protected static class BlockedState extends PendingState
    {
        protected BlockedState(PostfixStackEvaluator pse, Row row, Column col, Token tk)
        {
            super(pse, row, col, tk);
        }
        
        @Override
        protected boolean isBlocked()
        {
            return true;
        }
        
        @Override
        public PendingState getRootPendingState()
        {
            Token  t = getPendingToken();
            if (t != null && t.isPending()) {
                PendingState ps = t.getPendingState();
                if (ps != null) {
                    ps.lock();
                    try {
                        if (ps.isBlocked())
                            return ps.getRootPendingState();
                        else
                            return ps;
                    }
                    finally {
                        ps.unlock();
                    }
                }                
            }            
            
            return null;
        }        
    }
}
