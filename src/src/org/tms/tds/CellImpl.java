package org.tms.tds;

import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.DataTypeEnforcementException;
import org.tms.teq.Token;

public class CellImpl extends TableElementImpl implements Cell
{
    private Object m_cellValue;
    private ColumnImpl m_col;
    private int m_cellOffset;
    
    public CellImpl(ColumnImpl col, int cellOffset)
    {
        super(ElementType.Cell, col);
        m_col = col;
        m_cellOffset = cellOffset;
    }

    /*
     * Field getters/setters
     */
    
    public Object getCellValue()
    {
        return m_cellValue;
    }
    
    protected void setCellValue(Object value)
    {
        setCellValue(value, true);
    }
    
    protected void setDerivedCellValue(Token t)
    {
        if (t.isError())
            ;
        else if (t.isNull())
            ;
        else
            setCellValue(t.getValue(), true);
    }
    
    void setCellValueNoDataTypeCheck(Object value)
    {
        setCellValue(value, false);
    }
    
    protected void setCellValue(Object value, boolean typeSafeCheck)
    {
        if (typeSafeCheck && value != null && this.isDataTypeEnforced()) {
            if (isDatatypeMismatch(value))
                throw new DataTypeEnforcementException(getEnforcedDataType(), value);
        }
        
        m_cellValue = value;
    }
    
    private boolean isDatatypeMismatch(Object value)
    {
        if (value == null)
            return false;
        
        Class<? extends Object> valueClazz = value.getClass();
        
        // column data type takes president over cell data type
        Class<? extends Object> clazz = getColumn() != null && getColumn().getDataType() != null ? getColumn().getDataType() : null;
        if (clazz != null)
            return !clazz.isAssignableFrom(valueClazz);
        
        // finally, if the cell currently has a value, check prospect against cell
        if (getCellValue() != null)
            return !getDataType().isAssignableFrom(valueClazz);
        
        // if we get here, no mismatch
        return false;
    }
    
    Class<? extends Object> getEnforcedDataType()
    {
        if (getColumn() != null && getColumn().getDataType() != null)
            return getColumn().getDataType();
        else
            return getDataType();
    }
    
    protected int getCellOffset()
    {
    	return m_cellOffset;
    }
    
    void setCellOffset(int offset)
    {
    	m_cellOffset = offset;
    }
    
    protected ColumnImpl getColumn()
    {
    	return m_col;
    }
    
    protected RowImpl getRow()
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
     * A cell's data type will be enforced if:
     * <ul>
     * <li>The cell's parent table EnforceDataType property is set or</li>
     * <li>The cell's parent row EnforceDataType property is set or</li>
     * <li>The cell's parent column EnforceDataType property is set <i>and</i> the parent column has been assigned a data type or</li>
     * <li>The cell's EnforceDataType property is set and the cell has a data type, by virtue of there being a non-null cell value</li>
     * </ul>
     * @return
     */
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else if (getRow() != null && getRow().isDataTypeEnforced())
            return true;
        else if (getColumn() != null && getColumn().isDataTypeEnforced() )
            return true;
        else
            return this.isEnforceDataType() && this.getDataType() != null;
    }

    /**
     * A cell is empty if it is null
     */
    @Override
    public boolean isNull()
    {
        return m_cellValue == null;
    }

    @Override
    public Object getProperty(TableProperty key)
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
    protected void initialize(TableElementImpl e)
    {
        super.initialize(e);
        
        BaseElementImpl source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;            
            switch (tp) {
                default:
                    throw new IllegalStateException("No initialization available for CellImpl Property: " + tp);                       
            }
        }
        
        // initialize other variables
        m_cellValue = null;
        m_cellOffset = -1;
        m_col = null;
    }
    
    @Override
    public void delete()
    {
    	setCellValue(null);
    }

	@Override
	public void fill(Object o) 
	{
		setCellValue(o);
	}

	@Override
	public TableImpl getTable() 
	{
		if (m_col != null)
			return m_col.getTable();
		else
			return null;
	}

	@Override
	public ContextImpl getTableContext() 
	{
		if (getTable() != null)
			return getTable().getTableContext();
		else
			return null;
	}
}
