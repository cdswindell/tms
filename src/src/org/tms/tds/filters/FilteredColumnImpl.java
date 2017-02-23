package org.tms.tds.filters;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;

public class FilteredColumnImpl extends ColumnImpl 
{
	private ColumnImpl m_parent;

	protected FilteredColumnImpl(FilteredTableImpl parentTable, ColumnImpl parentCol) 
	{
		super(parentTable, parentCol);
		
		m_parent = parentCol;
        setReadOnly(true);
	}
	
	protected ColumnImpl getParent()
	{
		return m_parent;
	}
	
	@Override
    protected CellImpl getCellInternal(RowImpl row, boolean createIfSparse, boolean setCurrent)
    {
		if (row instanceof FilteredRowImpl && this instanceof FilteredColumnImpl) {
			FilteredRowImpl fRow = (FilteredRowImpl) row;
			FilteredTableImpl fTable = (FilteredTableImpl)getTable();
			
			CellImpl c = fTable.getCellInternal(fTable.getParent(), fRow.getParent(), this.getParent(), createIfSparse, false);
			return new FilteredCellImpl(fRow, this, c);
		}
		else
			return super.getCellInternal(row, createIfSparse, setCurrent);
    }

	@Override
    public Object getProperty(TableProperty key)
    {
		switch (key) {
			case Tags:
			case Label:
			case TimeSeries:
			case Derivation:
			case Description:
			case isEnforceDataType:
				return getParent().getProperty(key);
				
			default:
				return super.getProperty(key);
		}
    }

	@Override
	public String getLabel()
	{
		return m_parent.getLabel();
	}
	
	@Override
	public String getDescription()
	{
		return m_parent.getDescription();
	}
	
    @Override
    public Class<? extends Object> getDataType()
    {
        return m_parent.getDataType();
    }

	@Override
	public void setLabel(String label)
	{
        throw new UnsupportedImplementationException(this, "Cannot set label on a filtered column");
	}

    public void setDataType(Class<? extends Object> dataType)
    {
        throw new UnsupportedImplementationException(this, "Cannot set data type on a filtered column");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a filtered column");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Column, "Cannot clear a filtered column");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Column, "Cannot fill a filtered column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Column, "Cannot fill a filtered column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Column, "Cannot fill a filtered column");
    }
}
