package org.tms.tds.excel;

import java.util.Date;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;

public class ExcelRowImpl extends RowImpl implements ExternalDependenceTableElement
{
	private org.apache.poi.ss.usermodel.Row m_eRow;
	
    public ExcelRowImpl(ExcelTableImpl parentTable, org.apache.poi.ss.usermodel.Row eR) 
    {
		super(parentTable);
		m_eRow = eR;
	}

    @Override 
    public String getLabel()
    {
    	if (getTable().isRowNames()) {
    		org.apache.poi.ss.usermodel.Cell eC = m_eRow.getCell(0);
    		if (eC != null) {
    	        CellType cellType = eC.getCellTypeEnum();
    	        if (cellType == CellType.STRING)
    	        	return eC.getRichStringCellValue().getString();
    		}
    		
    		return null;
    	}
    	else return super.getLabel();   		
    }
    
    @Override
    protected int getCellOffset()
    {
        return super.getCellOffset();
    }

    @Override
    protected void delete(boolean compress)
    {
        super.delete(compress);
    }
    
    @Override
    public ExcelTableImpl getTable() 
    {
        return (ExcelTableImpl)super.getTable();
    }
   
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a log file on a database row");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a log file element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }

	public Object getCellValue(ExcelColumnImpl column) 
	{
		org.apache.poi.ss.usermodel.Cell eC = m_eRow.getCell(column.getFieldIndex(), MissingCellPolicy.RETURN_BLANK_AS_NULL);
		return fetchCellValue(eC);
	}
	
    private Object fetchCellValue(org.apache.poi.ss.usermodel.Cell eC) 
    {
        if (eC == null)
            return null;

        // decode based on the cell type
        CellType cellType = eC.getCellTypeEnum();
        switch(cellType) {
            case BOOLEAN:
                return eC.getBooleanCellValue();

            case NUMERIC:
                return eC.getNumericCellValue();

            case STRING:
                return eC.getRichStringCellValue().getString();

            case FORMULA:
                return fetchFormulaCellValue(eC);

            default:
                return null;
        }
    }

    private Object fetchFormulaCellValue(org.apache.poi.ss.usermodel.Cell eC) 
    {
        String cellFormula = eC.getCellFormula();
        switch (cellFormula) {
            case "TRUE":
            case "TRUE()":
                return true;

            case "FALSE":
            case "FALSE()":
                return false;

            case "NOW":
            case "NOW()":
            case "TODAY":
            case "TODAY()":
                return new Date();
        }

        // parse formula value
        Object cv = null;
        try {
            cv = eC.getNumericCellValue();
        }
        catch (IllegalStateException ise) {
            try {
                cv = eC.getStringCellValue();
            }
            catch (IllegalStateException iseStr) {
                try {
                    cv = eC.getBooleanCellValue();
                }
                catch (IllegalStateException iseBool) {
                    cv = ErrorCode.NaN;
                }
            }
        }

        return cv;
    }
}
