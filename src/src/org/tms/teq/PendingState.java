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
    private Row m_curRow;
    private Column m_curCol;
    private Token m_pendingToken;
    private Object m_pendingIntermediate;
    private Semaphore m_lock;
    private boolean m_valid;
    
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
    throws PendingDerivationException
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

    public void resetPendingState()
    {
        markInvalid();
        
        Cell cell = getPendingCell();
        if (cell != null)
            m_curCol.getTable().setCellValue(m_curRow, m_curCol, Token.createNullToken());
            
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
}
