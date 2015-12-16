package org.tms.tds.logs;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;

public class LogsCellImpl extends CellImpl
{

    private LogsRowImpl m_row;
    
    protected LogsCellImpl(LogsRowImpl row, LogsColumnImpl col)
    {
        super(col, row.getCellOffset());
        
        m_row = row;
        setReadOnly(true);
    }

    @Override
    public LogsRowImpl getRow()
    {
        return m_row;
    }
    
    @Override
    public LogsColumnImpl getColumn()
    {
        return (LogsColumnImpl)m_col;
    }
    
    @Override
    public LogsTableImpl getTable() 
    {
        if (m_row != null)
            return m_row.getTable();
        else
            return null;
    }

    @Override
    public Object getCellValue()
    {
        synchronized (m_row) {
            if (!m_row.isEntryProcessed()) {
                try {
                    processEntry(m_row);
                }
                finally {
                    m_row.setEntryProcessed(true);
                }
            }
        }
            
        return m_cellValue;
    }
    
    private void processEntry(LogsRowImpl row)
    {
        for (Cell cell : row.cells()) {
            if (cell == null || !(cell instanceof LogsCellImpl)) continue;
            
            Column c = cell.getColumn();
            if (c != null && c instanceof LogsColumnImpl) 
                ((LogsCellImpl)cell).m_cellValue = null;
        }
    }

    @Override
    public boolean isWriteProtected()
    {
        return true;
    }
    
    @Override 
    public boolean setCellValue(Object value)
    {
        throw new ReadOnlyException(this, TableProperty.CellValue);
    }
    
    @Override 
    public boolean fill(Object value)
    {
        throw new ReadOnlyException(this, TableProperty.CellValue);
    }
    
    @Override 
    public void delete()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot delete a log file cell");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a log file cell");
    }
}
