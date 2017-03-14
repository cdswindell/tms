package org.tms.tds.excel;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ExternalDependenceTableElement;

public class ExcelCellImpl extends CellImpl implements ExternalDependenceTableElement
{
    private ExcelRowImpl m_row;
    
    protected ExcelCellImpl(ExcelRowImpl row, ExcelColumnImpl col)
    {
        super(col, row.getCellOffset());
        
        m_row = row;
        setReadOnly(true);
    }

    @Override
    public ExcelRowImpl getRow()
    {
        return m_row;
    }
    
    @Override
    public ExcelColumnImpl getColumn()
    {
        return (ExcelColumnImpl)m_col;
    }
    
    @Override
    public ExcelTableImpl getTable() 
    {
        if (m_row != null)
            return m_row.getTable();
        else
            return null;
    }

    @Override
    synchronized public Object getCellValue()
    {
    	if (!isSet(sf_IS_PROCESSED_FLAG)) {
    		m_cellValue = m_row.getCellValue((ExcelColumnImpl)getColumn());   		
    		set(sf_IS_PROCESSED_FLAG, true);
    	}
    	
        return m_cellValue;
    }
    
    void unsetProcessed()
    {
		unSet(sf_IS_PROCESSED_FLAG);	
    }
    
    @Override
    public boolean isDerived()
    {
		return m_row.isFormula((ExcelColumnImpl)getColumn()); 	
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
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot delete an Excel cell");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on an Excel cell");
    }
}
