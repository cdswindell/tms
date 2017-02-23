package org.tms.tds.filters;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.RowImpl;

public class FilteredRowImpl extends RowImpl 
{
	private RowImpl m_parent;

	protected FilteredRowImpl(FilteredTableImpl parentTable, RowImpl parentRow) 
	{
		super(parentTable, parentRow);
		m_parent = parentRow;
	}

	protected RowImpl getParent() 
	{
		return m_parent;
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
	public void setLabel(String label)
	{
        throw new UnsupportedImplementationException(this, "Cannot set label on a filtered row");
	}

    public void setDataType(Class<? extends Object> dataType)
    {
        throw new UnsupportedImplementationException(this, "Cannot set data type on a filtered row");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a filtered row");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Row, "Cannot clear a filtered row");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Row, "Cannot fill a filtered row");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Row, "Cannot fill a filtered row");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Row, "Cannot fill a filtered row");
    }
}
