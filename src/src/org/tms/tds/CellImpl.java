package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.Range;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.DataTypeEnforcementException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.teq.Derivation;
import org.tms.teq.ErrorCode;
import org.tms.teq.Token;

public class CellImpl extends TableElementImpl implements Cell
{
    private Object m_cellValue;
    private ColumnImpl m_col;
    private int m_cellOffset;
    
    protected CellImpl(ColumnImpl col, int cellOffset)
    {
        super(col);
        
        m_col = col;
        m_cellOffset = cellOffset;
    }

    /*
     * Field getters/setters
     */
    @Override
    public ElementType getElementType()
    {
        return ElementType.Cell;
    }
    
    @Override
    public Object getCellValue()
    {
        return m_cellValue;
    }
    
    @Override
    public boolean setCellValue(Object value)
    {
        vetElement();        
        if (isWriteProtected())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (value == null && !isNullsSupported())
            throw new NullValueException(this, TableProperty.CellValue);
        
        // explicitly set cells can't be derived
        clearDerivation();
        
        // set the cell value, taking datatype enforcement into account, if enabled
        boolean valuesDiffer = setCellValue(value, true);
        
        // recalculate affected table elements
        TableImpl parentTable = getTable();
        if (valuesDiffer && parentTable != null) 
            parentTable.recalculateAffected(this);
        
        return valuesDiffer;
    }
    
    @Override
    public boolean isNumericValue()
    {
        vetElement();
        return m_cellValue != null && (m_cellValue instanceof Number);
    }
    
    @Override
    public boolean isStringValue()
    {
        vetElement();
        return m_cellValue != null && (m_cellValue instanceof String);
    }
    
    @Override
    public boolean isErrorValue()
    {
        vetElement();
        return getErrorCode() != ErrorCode.NoError;
    }
    
    @Override
    public ErrorCode getErrorCode()
    {
        vetElement();
        if (getDataType() == Double.class) {
            if ((double) getCellValue() == Double.NaN)
                return ErrorCode.NaN;
            else if ((double)getCellValue() == Double.POSITIVE_INFINITY)
                return ErrorCode.Infinity;
            else if ((double)getCellValue() == Double.NEGATIVE_INFINITY)
                return ErrorCode.Infinity;
        }
        else if (getCellValue() != null && getCellValue() instanceof ErrorCode)
            return (ErrorCode)getCellValue();
        
        return ErrorCode.NoError;
    }
    
    protected boolean setDerivedCellValue(Token t)
    {
        if (t.isError()) 
            return this.setCellValueNoDataTypeCheck(t.getErrorCode());
        else if (t.isNull())
        	return setCellValue(null, true);
        else
        	return setCellValue(t.getValue(), true);
    }
    
    boolean setCellValueNoDataTypeCheck(Object value)
    {
        return setCellValue(value, false);
    }
    
    protected boolean setCellValue(Object value, boolean typeSafeCheck)
    {
        if (typeSafeCheck && value != null && this.isDataTypeEnforced()) {
            if (isDatatypeMismatch(value))
                throw new DataTypeEnforcementException(getEnforcedDataType(), value);
        }
        
        boolean valuesDiffer = false;        
        if (value != m_cellValue) {
            m_cellValue = value;
            valuesDiffer = true;
        }
        
        return valuesDiffer;
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
    
    protected Class<? extends Object> getEnforcedDataType()
    {
        if (getColumn() != null && getColumn().getDataType() != null)
            return getColumn().getDataType();
        else
            return getDataType();
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
        vetElement();
        if (getTable() != null)
            return getTable().getTableContext();
        else
            return null;
    }
    
    @Override
    public ColumnImpl getColumn()
    {
        vetElement();
        return m_col;
    }
    
    @Override
    public RowImpl getRow()
    {
        vetElement();
        if (getTable() != null)
            return getTable().getRowByCellOffset(getCellOffset());
        else
            return null;
    }
    
    @Override
    public Class<? extends Object> getDataType()
    {
        if (m_cellValue != null)
            return m_cellValue.getClass();
        else
            return null;
    }

    protected int getCellOffset()
    {
    	return m_cellOffset;
    }
    
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
            return this.isEnforceDataType();
    }

    @Override
    public boolean isNullsSupported()
    {
        return isSupportsNull() &&
                (getColumn() != null ? getColumn().isNullsSupported() : false) &&
                (getRow() != null ? getRow().isNullsSupported() : false);
    }
    
    @Override
    protected boolean isWriteProtected()
    {
        return isReadOnly() ||
                (getColumn() != null ? getColumn().isWriteProtected() : false) ||
                (getRow() != null ? getRow().isWriteProtected() : false);
                
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
                return getDataType();
                
            case CellValue:
                return getCellValue();
                
            case Derivation:
                return getDerivation();
                
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
    public int getNumCells()
    {
        return 1;
    }
    
    @Override
    public void fill(Object o) 
    {
        setCellValue(o);
    }

    @Override
    public void clear() 
    {
        fill(null);
    }

    @Override
    public void delete()
    {
        clear();
        invalidate();
    }
	
	/*
	 * Derivation-related methods
	 */
	@Override
    public Derivation getDerivation()
    {
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        return deriv;
    }

    @Override
    public void setDerivation(String expr)
    {
        vetElement();
        
        // clear out any existing derivations
        clearDerivation();
        
        if (expr != null && expr.trim().length() > 0) {
            Derivation deriv = Derivation.create(expr.trim(), this);
            
            // mark the rows/columns that impact the deriv, and evaluate values
            if (deriv != null && deriv.isConverted()) {
                for (TableElement d : deriv.getAffectedBy()) {
                    ((TableElementImpl)d).registerAffects(this);
                }
                
                this.getRow().setInUse(true);
                this.getColumn().setInUse(true);
                getTable().registerDerivedCell(this, deriv);
                
                recalculate();
            }  
        }
    }

    @Override
    public void clearDerivation()
    {
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null) {
            Derivable elem = deriv.getTarget();
            for (TableElement d : deriv.getAffectedBy()) {
                TableElementImpl tse = (TableElementImpl)d;
                tse.deregisterAffects(elem);
            }
            
            getTable().deregisterDerivedCell(this);            
            deriv.destroy();
        }        
    }

    @Override
    public boolean isDerived()
    {
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        return deriv != null;       
    }

    @Override
    public List<TableElement> getAffectedBy()
    {
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null)
            return deriv.getAffectedBy();
        else
            return null;
    }

    @Override
    public List<Derivable> getAffects()
    {
        TableImpl table = getTable();
        if (table != null)
            return table.getCellAffects(this);
        else
            return null;
    }

    @Override
    public void recalculate()
    {
        vetElement();
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null) {
            deriv.recalculateTarget();
            
            // recalculate dependent columns
            TableImpl parentTable = getTable();
            if (parentTable != null) 
                parentTable.recalculateAffected(this);
        }
    }    

    @Override
    public void registerAffects(Derivable d)
    {
        /**
         * To minimize cell footprint, the set of elements
         * this cell affects is maintained in the parent table
         */
        vetElement();
        TableImpl table = getTable();
        if (table != null)
            table.registerAffects(this, d);
    }

    @Override
    public void deregisterAffects(Derivable d)
    {
        /**
         * To minimize cell footprint, the set of elements
         * this cell affects is maintained in the parent table
         */
        TableImpl table = getTable();
        if (table != null)
            table.deregisterAffects(this, d);
    }
    
    /*
     * Range-related methods
     */
    protected boolean add(RangeImpl r)
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null)
            return parent.registerRangeCell(this, r);
        
        return false;
    }
    
    protected boolean remove(RangeImpl r)
    {
        if (r != null) {
            /*
             * if the range contains the element, use the range method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
            if (r.contains(this))
                r.remove(this);
            
            TableImpl parent = getTable();
            assert parent != null : "Parent table required";
            if (parent != null)
                return parent.deregisterRangeCell(this, r);
        }
        
        return false;
    }
    
    @Override
    public List<Range> getRanges()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) {
            Set<RangeImpl> ranges = parent.getCellRanges(this);
            if (ranges != null) {
                List<Range> rangesList = new ArrayList<Range>(ranges.size());
                rangesList.addAll(ranges);
                return rangesList;
            }
        }
        
        return Collections.emptyList();
    }

    protected Set<RangeImpl> getRangesInternal()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) 
            return parent.getCellRanges(this);
        else
            return Collections.emptySet();
    }
    
    /**
     * Invalidate a cell in response to the parent row/column being deleted
     */
    protected void invalidateCell()
    {
        // clear the cell value and cell derivation
        clearDerivation();
        
        // clear any derivations on elements affected by this cell
        List<Derivable> affects = getAffects();
        if (affects != null) 
            affects.forEach(d -> d.clearDerivation());
        
        // remove the cell from any ranges
        if (getRangesInternal() != null) {
            for (RangeImpl r : getRangesInternal()) {
                if (r != null && r.isValid())
                    r.remove(this);
            }
        }
         
        m_cellValue = null;
        
        m_col = null;
        m_cellOffset = -1;
        
        invalidate();
    }
    
	@Override
	public String toString()
	{
        String label = (String)getProperty(TableProperty.Label);
        if (label != null)
            label = ": " + label;
        else
            label = "";
        
        return String.format("[%s%s <%s>]", getElementType(), label, isNull() ? "null" : getCellValue().toString());
	}
	
	/*
	 * Methods to get cells iterable
	 */
    @Override
    public Iterable<Cell> cells()
    {
        vetElement();
        return new CellIterable();
    }
    
    protected class CellIterable implements Iterator<Cell>, Iterable<Cell>
    {
        private boolean m_hasNext = true;
        
        @Override
        public Iterator<Cell> iterator()
        {
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return m_hasNext;
        }

        @Override
        public Cell next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            
            m_hasNext = false;
            return CellImpl.this;
        }        
    }
}
