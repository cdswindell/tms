package org.tms.tds.excel;

import org.apache.poi.ss.usermodel.CellType;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;

public class ExcelColumnImpl extends ColumnImpl implements ExternalDependenceTableElement
{
	private int m_fieldIndex;
	
    public ExcelColumnImpl(TableImpl parentTable, int fieldIndex)
    {
        super(parentTable);
        
        m_fieldIndex = fieldIndex;
        setReadOnly(true);
    }

    @Override 
    public String getLabel()
    {
    	if (getTable().isColumnNames()) {
    		org.apache.poi.ss.usermodel.Row eR = getTable().getSheet().getRow(0);
    		if (eR != null) {
	    		org.apache.poi.ss.usermodel.Cell eC = eR.getCell(m_fieldIndex);
	    		if (eC != null) {
	    	        CellType cellType = eC.getCellTypeEnum();
	    	        if (cellType == CellType.STRING)
	    	        	return eC.getRichStringCellValue().getString();
	    		}
    		}
    		
    		return null;
    	}
    	else return super.getLabel();   		
    }
    
    @Override
    protected CellImpl getCellInternal(RowImpl row, boolean createIfSparse, boolean setCurrent)
    {
        // in that this column is an excel cell, we need to override createIfSparse
        if (!createIfSparse && (row instanceof ExcelRowImpl))
            createIfSparse = true;
        
        return super.getCellInternal(row, createIfSparse, setCurrent);
    }
    
    /**
     * Return the 0-based index of the row in the dbms table result set
     * @return the 0-based index of the row in the dbms table result set
     */
    int getFieldIndex()
    {
        return m_fieldIndex;
    }
    
    @Override
    public ExcelTableImpl getTable()
    {
        return (ExcelTableImpl)super.getTable();
    }
    
    @Override
    protected CellImpl createNewCell(RowImpl row)
    {
        if (row instanceof ExcelRowImpl)
            return new ExcelCellImpl((ExcelRowImpl) row, this);
        else
            return super.createNewCell(row);
    }

    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on an Excel column");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear an Excel element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill an Excel row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill an Excel row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill an Excel row/column");
    }

	synchronized void unsetProcessedFormulaCells() 
	{
		cellsInternal().forEach(c -> {if (c != null && c instanceof ExcelCellImpl && c.isDerived()) ((ExcelCellImpl)c).unsetProcessed();});	
	}
}
