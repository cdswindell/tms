package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Subset;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Token;
import org.tms.api.event.TableElementEventType;
import org.tms.api.event.TableElementListener;
import org.tms.api.event.exceptions.BlockedRequestException;
import org.tms.api.exceptions.DataTypeEnforcementException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.teq.Derivation;
import org.tms.teq.ErrorCode;

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
        
        TableImpl parentTable = getTable();
        if (parentTable != null) {
            synchronized(parentTable)
            {
                return setCellValueInternal(value);
            }
        }
        else
            return setCellValueInternal(value);
    }
    
    private boolean setCellValueInternal(Object value)
    {        
        if (value != null && value instanceof Token)
            return postResult((Token)value);
        
        if (isWriteProtected())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (value == null && !isNullsSupported())
            throw new NullValueException(this, TableProperty.CellValue);
        
        // explicitly set cells can't be derived
        clearDerivation();
        
        // set the cell value, taking datatype enforcement into account, if enabled
        Object oldValue = m_cellValue;
        boolean valuesDiffer = setCellValue(value, true, true);
        
        // recalculate affected table elements
        TableImpl parentTable = getTable();
        if (valuesDiffer && parentTable != null && 
                parentTable.isAutoRecalculateEnabled()) 
            Derivation.recalculateAffected(this);
        
        if (valuesDiffer)
            fireEvents(TableElementEventType.OnNewValue, oldValue, m_cellValue);
        
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
    
    protected boolean postResult(Token t)
    {
        boolean wasPending = isPendings();
        boolean nowPending = false;
        decrementPendings();
        try {
            if (t.isError()) 
                return this.setCellValueNoDataTypeCheck(t.getErrorCode());
            else if (t.isNull())
                return setCellValue(null, true, false);
            else if (t.isPending()) {
                m_cellValue = t.getValue();
                nowPending = true;
                incrementPendings();
                
                if (!wasPending)
                    fireEvents(TableElementEventType.OnPendings);
               
                return true;
            }
            else
            	return setCellValue(t.getValue(), true, false);
        }
        finally {
            if (!wasPending && nowPending)
                fireEvents(TableElementEventType.OnNoPendings);                
        }
    }

    private void incrementPendings()
    {
        if (!isPendings()) {
            setPending(true);                
            TableImpl table = m_col != null ? m_col.getTable() : null;
            if (table != null)
                table.incrementPendings();
            
            if (m_col != null)
                m_col.incrementPendings();
            
            RowImpl row = getRow();
            if (row != null)
                row.incrementPendings();
        }
    }
    
    private void decrementPendings()
    {
        if (isPendings()) {
            setPending(false);                
            TableImpl table = m_col != null ? m_col.getTable() : null;
            if (table != null)
                table.decrementPendings();
            
            if (m_col != null)
                m_col.decrementPendings();
            
            RowImpl row = getRow();
            if (row != null)
                row.decrementPendings();
        }
    }
    
    boolean setCellValueNoDataTypeCheck(Object value)
    {
        return setCellValue(value, false, false);
    }
    
    protected boolean setCellValue(Object value, boolean typeSafeCheck, boolean fireEvents)
    {
        decrementPendings();
        if (typeSafeCheck && value != null && this.isDataTypeEnforced()) {
            if (isDatatypeMismatch(value))
                throw new DataTypeEnforcementException(getEnforcedDataType(), value);
        }
        
        boolean valuesDiffer = false;        
        if (value != m_cellValue) {            
            if (fireEvents) {
                try {
                    fireEvents(TableElementEventType.OnBeforeNewValue, m_cellValue, value);
                }
                catch (BlockedRequestException e) {
                    return false;
                }
            }
            
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

    public boolean isPending()
    {
        return isPendings();
    }
    
    @Override()
    public boolean isPendings()
    {
        return isSet(sf_IS_PENDING_FLAG);
    }

    private void setPending(boolean pending)
    {
        set(sf_IS_PENDING_FLAG, pending);
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
    public Derivable setDerivation(String expr)
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
        
        return this;
    }

    @Override
    public Derivable clearDerivation()
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
        
        return this;
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
            return Collections.unmodifiableList(deriv.getAffectedBy());
        else
            return null;
    }

    @Override
    public List<Derivable> getAffects()
    {
        TableImpl table = getTable();
        if (table != null)
            return Collections.unmodifiableList(table.getCellAffects(this, true));
        else
            return null;
    }

    @Override
    public List<Derivable> getDerivedElements()
    {
        vetElement();
        if (isDerived())
            return Collections.singletonList(this);
        else
            return Collections.emptyList();
    }
    
    @Override
    public void recalculate()
    {
        vetElement();
        Derivation deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null) {
            deriv.recalculateTarget();
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
     * Subset-related methods
     */
    protected boolean add(SubsetImpl r)
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null)
            return parent.registerSubsetCell(this, r);
        
        return false;
    }
    
    protected boolean remove(SubsetImpl r)
    {
        if (r != null) {
            /*
             * if the subset contains the element, use the subset method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
            if (r.contains(this))
                r.remove(this);
            
            TableImpl parent = getTable();
            assert parent != null : "Parent table required";
            if (parent != null)
                return parent.deregisterSubsetCell(this, r);
        }
        
        return false;
    }
    
    @Override
    public int getNumSubsets()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) {
            Set<SubsetImpl> subsets = parent.getCellSubsets(this);
            if (subsets != null)
                return subsets.size();
        }
        
        return 0;
    }
    
    @Override
    public List<Subset> getSubsets()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) {
            Set<SubsetImpl> subsets = parent.getCellSubsets(this);
            if (subsets != null) {
                List<Subset> subsetsList = new ArrayList<Subset>(subsets.size());
                subsetsList.addAll(subsets);
                return Collections.unmodifiableList(subsetsList);
            }
        }
        
        return Collections.emptyList();
    }

    protected Set<SubsetImpl> getSubsetsInternal()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) 
            return parent.getCellSubsets(this);
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
        
        // remove listeners
        removeAllListeners();
        
        // clear any derivations on elements affected by this cell
        TableImpl parentTable = getTable();
        List<Derivable> affects = parentTable != null ? parentTable.getCellAffects(this, false) : getAffects();
        if (affects != null) 
            (new ArrayList<Derivable>(affects)).forEach(d -> d.clearDerivation());
        
        // remove the cell from any subsets
        if (getSubsetsInternal() != null) {
            for (SubsetImpl r : getSubsetsInternal()) {
                if (r != null && r.isValid())
                    r.remove(this);
            }
        }
        
        decrementPendings(); 
        m_cellValue = null;
        
        m_col = null;
        m_cellOffset = -1;
        
        invalidate();
    }
    
    @Override
    public void delete()
    {
        this.setCellValue(null, false, true);
    }
        
    @Override
    protected void delete(boolean compress)
    {
        invalidateCell();
    }
        
    private void fireEvents(TableElementEventType evT, Object... args)
    {
        if (getTable() != null )
            getTable().fireCellEvents(this, evT, args);
    }

    @Override
    public boolean addListeners(TableElementEventType evT, TableElementListener... tels)
    {
        if (getTable() != null)
            return getTable().addCellListener(this, evT, tels);
        else
            return false;
    }

    @Override
    public boolean removeListeners(TableElementEventType evT, TableElementListener... tels)
    {
        if (getTable() != null)
            return getTable().removeCellListener(this, evT, tels);
        else
            return false;
    }

    @Override
    public List<TableElementListener> getListeners(TableElementEventType... evTs)
    {
        if (getTable() != null)
            return getTable().getCellListeners(this, evTs);
        else
            return Collections.emptyList();
    }

    @Override
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs)
    {
        if (getTable() != null)
            return getTable().removeAllCellListeners(this, evTs);
        else
            return Collections.emptyList();
    }

    @Override
    public boolean hasListeners(TableElementEventType... evTs)
    {
        if (getTable() != null)
            return getTable().hasCellListeners(this, evTs);
        else
            return false;
    }
    
    @Override
	public String toString()
	{
        String label = (String)getProperty(TableProperty.Label);
        if (label != null)
            label = ": " + label;
        else
            label = "";
        
        RowImpl r = getRow();
        ColumnImpl c = getColumn();
        return String.format("[%s%s <%s> R%dC%d]", getElementType(), label, 
                isPendings() ? "pending" : isNull() ? "null" :getCellValue().toString(),
                r != null ? r.getIndex() : 0,
                c != null ? c.getIndex() : 0);
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
