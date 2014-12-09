package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Cell extends TableElement
{
    private Object m_cellValue;
    private Column m_col;
    private int m_cellOffset;
    
    public Cell(Column col, int cellOffset)
    {
        super(ElementType.Cell, col);
        m_col = col;
        m_cellOffset = cellOffset;
    }

    /*
     * Field getters/setters
     */
    
    protected Object getCellValue()
    {
        return m_cellValue;
    }
    
    protected void setCellValue(Object value)
    {
        m_cellValue = value;
    }
    
    protected int getCellOffset()
    {
    	return m_cellOffset;
    }
    
    void setCellOffset(int offset)
    {
    	m_cellOffset = offset;
    }
    
    protected Column getColumn()
    {
    	return m_col;
    }
    
    protected Row getRow()
    {
    	if (getTable() != null)
    		return getTable().getRowByCellOffset(getCellOffset());
    	else
    		return null;
    }
    
    protected Class<? extends Object> getDataType()
    {
    	if (m_cellValue != null)
    		return m_cellValue.getClass();
    	else
    		return null;
    }
    /*
     * Overridden methods
     */
    
    /**
     * A cell is empty if it is null
     */
    @Override
    protected boolean isEmpty()
    {
        return m_cellValue == null;
    }

    @Override
    protected Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
	        case CellOffset:
	            return getCellOffset();
	            
	        case Column:
	            return getColumn();
	            
	        case Row:
	            return getRow();
	            
	        case DataType:
	            return getColumn();
	            
            default:
                return super.getProperty(key);
        }
    }
    @Override
    protected void initialize(TableElement e)
    {
        super.initialize(e);
        
        BaseElement source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;            
            switch (tp) {
                default:
                    throw new IllegalStateException("No initialization available for Cell Property: " + tp);                       
            }
        }
        
        // initialize other variables
        m_cellValue = null;
        m_cellOffset = -1;
        m_col = null;
    }
    
    @Override
    protected void delete()
    {
    	setCellValue(null);
    }

	@Override
	protected void fill(Object o) 
	{
		setCellValue(o);
	}

	@Override
	protected Table getTable() 
	{
		if (m_col != null)
			return m_col.getTable();
		else
			return null;
	}

	@Override
	protected Context getContext() 
	{
		if (getTable() != null)
			return getTable().getContext();
		else
			return null;
	}
}
