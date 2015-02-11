package org.tms.teq;

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
    private Token m_pendingToken;
    private Object m_pendingIntermediate;
    private Semaphore m_lock;
    
    protected Row m_curRow;
    protected Column m_curCol;
    protected boolean m_valid;
    
    protected PendingState(PostfixStackEvaluator pse, Row row, Column col, Token tk)
    {
        m_uuid = Derivation.getTransactionID();
        m_pse = pse;
        m_curRow = row;
        m_curCol = col;
        m_pendingToken = tk;  
        m_pendingIntermediate = tk.getValue();
        m_valid = true;
        
        if (pse.getDerivation() != null && isRegisterWithParentDerivation())
            getDerivation().registerPendingState(this);
        
        m_lock = new Semaphore(1, true);
    }

    protected PendingState()
    {
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
    
    protected Cell getPendingCell()
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
        Cell cell = getPendingCell();
        if (cell == null || !cell.isPendings())
            return Token.createNullToken();
        
        // calculate the new value, this could throw an exception if a pending
        // calculation or a blocked cell is encountered
        Token t = m_pse.reevaluate(m_curRow, m_curCol, dc);
        
        // we want exclusive access
        lock();
        try {            
            // in the event this state has been invalidated (by clearing the derivation
            // just return a null token and don't set cell
            if (!isValid())
                return Token.createNullToken();
            
            // apply numeric precision/rounding
            if (t.isNumeric() )
                t.setValue(getDerivation().applyPrecision(t.getNumericValue()));
            
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
    
    protected void delete()
    {
        markInvalid();
        
        Cell cell = getPendingCell();
        if (cell != null ) {
            if (cell.isPendings() )
                m_curCol.getTable().setCellValue(m_curRow, m_curCol, Token.createNullToken());
            
            Derivation d = getDerivation();
            if (d != null)
                d.resetPendingCellDependents(cell);
        }
        
        m_uuid = null;
        m_pse = null;
        m_pendingToken = null;  
        m_pendingIntermediate = null;     
    }

    protected boolean isStillPending()
    {
        if (!isValid())
            return false;
        
        Cell c = getPendingCell();
        if (c != null)
            return c.isPendings();
        else
            return false;
    }
    
    public boolean isValid()
    {
        return m_valid;
    }
    
    protected void markInvalid()
    {
        m_valid = false;
    }
    
    /**
     * Unblock derivations blocked on the completion of a pending calculation
     */
    protected void unblockDerivations()
    {
        // called while access to this pending state is locked
        if (!isValid())
            return;

        // also unblock derivations blocked on this cell
        Cell c = getPendingCell();
        Derivation d = getDerivation();
        if (d != null && !c.isPendings())
            d.unblockDerivations(c);

    }
    
    protected boolean unblockDerivations(Cell nonPendingCell)
    {
        return false;
    }
    
    protected boolean isBlockedDerivations()
    {
        Cell c = getPendingCell();
        Derivation d = getDerivation();
        if (d != null)
            return d.isBlockedDerivations(c);
        else
            return false;
    }   
    
    protected boolean isBlocked()
    {
        return false;
    }
    
    protected boolean isBlockedStatistic()
    {
        return false;
    }
    
    protected PendingState getRootPendingState()
    {
        return this;
    }
    
    protected boolean isRegisterWithParentDerivation()
    {
        return true;
    }
    
    public String toString()
    {
        return String.format("Pending: Row %d Col %d (%s : %s)", m_curRow.getIndex(), m_curCol.getIndex(),
                isValid() ? "OK" : "Expired",
                isStillPending() ? "Pending" : "Not Pending");
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
        protected boolean isBlockedStatistic()
        {
            return false;
        }
        
        @Override
        protected boolean unblockDerivations(Cell unblockedCell)
        {
            Derivation psDeriv = null;
            if (isValid() && (psDeriv = getDerivation()) != null) {
                DerivationContext dc = new DerivationContext(); 
                try
                {
                    // blocks on access to ps
                    reevaluate(dc);
                    
                    this.unblockDerivations();
                    
                    return true;
                }
                catch (PendingDerivationException pc)
                {
                    // if derivation is being cleared, it could go away,
                    // get exclusive access while we perform cache
                    lock();
                    try {
                        if (isValid()) {
                            PendingState newPs = pc.getPendingState();
                            psDeriv.cacheDeferredCalculation(newPs, dc);
                        }
                        else {
                            delete();
                            dc.clearPendings();
                        }
                    }
                    finally {
                        unlock();
                    }
                    
                    dc.processPendings();
                }
                catch (BlockedDerivationException e)
                {
                }
            }
            
            return false;
        }
        
        @Override
        protected PendingState getRootPendingState()
        {
            Token  t = getPendingToken();
            if (t != null && t.isPending()) {
                PendingState ps = t.getPendingState();
                if (ps == this)
                    return this;
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
        
        @Override
        protected boolean isRegisterWithParentDerivation()
        {
            return false;
        }
        
        public String toString()
        {
            return String.format("Blocked: Row %d Col %d (%s)", m_curRow.getIndex(), m_curCol.getIndex(),
                    isValid() ? "OK" : "Expired");
        }        
    }
    
    protected static class BlockedStatisticState extends PendingState
    {
        private PendingStatistic m_pendingStat;
        private Derivation m_derivation;
        
        protected BlockedStatisticState(PendingStatistic pendingStat, Derivation deriv)
        {
            m_pendingStat = pendingStat;
            m_derivation = deriv;
            m_valid = true;
        }
        
        @Override
        protected void delete()
        {
            m_pendingStat.delete();
            super.delete();
        }       
       
        @Override
        protected boolean isBlocked()
        {
            return false;
        }
        
        @Override
        protected boolean isBlockedStatistic()
        {
            return true;
        }        
        
        @Override
        protected boolean unblockDerivations(Cell unblockedCell)
        {
            if (isValid())
                return m_pendingStat.unblockStatistic(unblockedCell);
            else
                return false;
        }
        
        @Override
        protected Derivation getDerivation()
        {
            return m_derivation;
        }
        
        @Override
        protected PendingState getRootPendingState()
        {
            return this;
        }
        
        @Override 
        protected Cell getPendingCell()
        {
            return null;
        }
        
        protected boolean isStillPending()
        {
            if (!isValid() || m_pendingStat == null)
                return false;
            
            return m_pendingStat.isBlockedOnAny();
        }
        
        @Override
        protected boolean isRegisterWithParentDerivation()
        {
            return false;
        }
        
        public String toString()
        {
            return String.format("Blocked  %s (%s)", m_pendingStat, isValid() ? "OK" : "Expired");
        }                
    }
}
