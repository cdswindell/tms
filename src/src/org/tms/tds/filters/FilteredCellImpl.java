package org.tms.tds.filters;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;

public class FilteredCellImpl extends CellImpl 
{
    private FilteredRowImpl m_row;
	private CellImpl m_parent;

	protected FilteredCellImpl(FilteredRowImpl row, FilteredColumnImpl col, CellImpl cell)
    {
        super(col, -1);
        
        m_row = row;
        m_parent = cell;
        setReadOnly(true);
    }

    @Override
    public RowImpl getRow()
    {
        return m_row;
    }
    
    @Override
    public ColumnImpl getColumn()
    {
        return m_col;
    }
    
    @Override
    public FilteredTableImpl getTable() 
    {
        return m_col != null ? (FilteredTableImpl)m_col.getTable() : null;
    }

    @Override
    public Object getCellValue()
    {           
        return m_parent.getCellValue();
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
