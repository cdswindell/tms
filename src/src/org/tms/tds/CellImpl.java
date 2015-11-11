package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Subset;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.Taggable;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.Token;
import org.tms.api.events.BlockedRequestException;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.exceptions.DataTypeEnforcementException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;
import org.tms.io.Printable;
import org.tms.teq.DerivationImpl;

public class CellImpl extends TableElementImpl implements Cell, Printable
{
    protected Object m_cellValue;  
    protected ColumnImpl m_col;
    
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
    public String getFormattedCellValue()
    {
        if (getCellValue() == null)
            return null;
        
        String format = getFormatString();
        if (format != null) {
            try {
                return String.format(format, getCellValue());
            }
            catch (Exception e) {
                // noop
            }
        }
        else if (isBooleanValue()) 
            return (Boolean)getCellValue() ? "Yes" : "No";
                
        return getCellValue().toString();
    }   
    
    @Override
    public boolean setCellValue(Object value)
    {        
        vetElement();   
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
            DerivationImpl.recalculateAffected(this);
        
        if (valuesDiffer)
            fireEvents(TableElementEventType.OnNewValue, oldValue, m_cellValue);
        
        return valuesDiffer;
    }
    
    @Override
    public boolean isNumericValue()
    {
        vetElement();
        return getCellValue() != null && (Number.class.isAssignableFrom(getCellValue().getClass()));
    }
    
    @Override
    public boolean isStringValue()
    {
        vetElement();
        return getCellValue() != null && (String.class.isAssignableFrom(getCellValue().getClass()));
    }
    
    @Override
    public boolean isBooleanValue()
    {
        vetElement();
        return getCellValue() != null && (Boolean.class.isAssignableFrom(getCellValue().getClass()));
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
        if (getDataTypeInternal() == Double.class) {
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
        TableImpl parentTable = getTable();
        if (parentTable != null) {
            synchronized(parentTable) {
                boolean wasPending = isPendings();
                boolean nowPending = false;
                decrementPendings();
                try {

                    if (t.isError()) {
                        boolean isDifferent = this.setCellValueNoDataTypeCheck(t.getErrorCode());
                        switch (t.getErrorCode()) {
                            case SeeErrorMessage:
                                setErrorMessage(t.getStringValue());
                                break;
                                
                            default:
                                break;
                        }
                        
                        return isDifferent;
                    }
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
        }
        
        return false;

    }

    private void setErrorMessage(String eMsg)
    {
        if (eMsg != null && (eMsg = eMsg.trim()).length() > 0)
            setProperty(TableProperty.ErrorMessage, eMsg);  
        else
            clearProperty(TableProperty.ErrorMessage);
    }
    
    public String getErrorMessage()
    {
        return (String) super.getProperty(TableProperty.ErrorMessage);
    }

    private void incrementPendings()
    {
        if (!isPendings()) {
            setPending(true);                
            TableImpl table = getTable();
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
            TableImpl table = getTable();
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
    
    protected boolean setCellValue(Object value, boolean typeSafeCheck, boolean doPreprocess)
    {
        // clear error message, if cell is in error
        if (isErrorValue())
            setErrorMessage(null);
        
        decrementPendings();
        if (typeSafeCheck && value != null && this.isDataTypeEnforced()) {
            if (isDatatypeMismatch(value))
                throw new DataTypeEnforcementException(getEnforcedDataType(), value);
        }
        
        boolean valuesDiffer = false;        
        if ((value == null && m_cellValue != null) || (value != null && !value.equals(m_cellValue))) {  
            if (doPreprocess) {
                // validate and potentially transform new cell value
                value = applyTransformer(value);
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
    
    @Override
    public boolean isLabelIndexed()
    {
        TableImpl parent;
        if ((parent = getTable()) != null) 
            return parent.isCellLabelsIndexed();
        else
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
    public String getUnits()
    {
        return (String)getProperty(TableProperty.Units);
    }

    @Override
    public void setUnits(String units)
    {
        if (units == null || (units = units.trim()).length() == 0)
            clearProperty(TableProperty.Units);
        else
            setProperty(TableProperty.Units, units);
    }

    @Override
    public String getDisplayFormat()
    {
        return (String)getProperty(TableProperty.DisplayFormat);
    }
    
    @Override
    public void setDisplayFormat(String value)
    {
        if (value != null && (value = value.trim()).length() > 0)
            setProperty(TableProperty.DisplayFormat, value);
        else
            clearProperty(TableProperty.DisplayFormat);        
    }

    public boolean isFormatted()
    {
        return getFormatString() != null;
    }
    
    protected String getFormatString()
    {
        String format = getDisplayFormat();
        if (format != null)
            return format;
        
        RowImpl row = getRow();
        if (row != null)
            format = row.getDisplayFormat();
        
        if (format != null)
            return format;
        
        ColumnImpl col = getColumn();
        if (col != null)
            format = col.getDisplayFormat();
        
        if (format != null)
            return format;
        
        TableImpl tbl = getTable();
        if (tbl != null)
            format = tbl.getDisplayFormat();
        
        return format;
    }
    
    @Override
    public TableCellValidator getValidator()
    {
        if (isSet(sf_HAS_CELL_VALIDATOR_FLAG))
            return (TableCellValidator)getProperty(TableProperty.Validator);
        else
            return null;
    }

    @Override
    public void setValidator(TableCellValidator validator)
    {
        if (validator == null)
            clearProperty(TableProperty.Validator);
        else
            setProperty(TableProperty.Validator, validator);
        
        // accelerator to minimize map lookups
        set(sf_HAS_CELL_VALIDATOR_FLAG, validator != null);
    }

    @Override
    public void setTransformer(TableCellTransformer transformer)
    {
        setValidator(transformer);
    }
    
    private Object applyTransformer(Object newValue)
    {
        TableCellValidator validator = getValidator();
        if (validator == null)
            validator = getColumn().getValidator();
        if (validator == null)
            validator = getRow().getValidator();
        
        if (validator != null)
            newValue = validator.transform(newValue);
        
        return newValue;
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
        if (!isErrorValue())
            return getDataTypeInternal();
        else
            return null;
    }

    Class<? extends Object> getDataTypeInternal()
    {
        if (getCellValue() != null)
            return getCellValue().getClass();
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
    public boolean isWriteProtected()
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
        return getCellValue() == null;
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
                
            case Tags:
                return getTags();
                
            case ErrorMessage:
                return getErrorMessage();
                
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
                    if (!tp.isOptional())
                        throw new IllegalStateException("No initialization available for CellImpl Property: " + tp);                       
            }
        }
        
        // initialize other variables
        m_cellValue = null;
        m_cellOffset = -1;
        m_col = null;
    }
    

    @Override
    protected Map<String, Object> getElemProperties(boolean createIfEmpty)
    {
        TableImpl parent;
        if ((parent = getTable()) != null)
            return parent.getCellElemProperties(this, createIfEmpty);
        else
            return null;
    }

    @Override
    protected void resetElemProperties()
    {
        TableImpl parent;
        if ((parent = getTable()) != null)
            parent.resetCellElemProperties(this);
    }
    
    @Override
    public int getNumCells()
    {
        return 1;
    }
    
    @Override
    public boolean fill(Object o) 
    {
        return setCellValue(o);
    }

    @Override
    public boolean clear() 
    {
        return fill(null);
    }

	/*
	 * DerivationImpl-related methods
	 */
    
	@Override
    public DerivationImpl getDerivation()
    {
        DerivationImpl deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        return deriv;
    }

    @Override
    public Derivable setDerivation(String expr)
    {
        vetElement();
        
        // clear out any existing derivations
        clearDerivation();
        
        if (expr != null && expr.trim().length() > 0) {
            DerivationImpl deriv = DerivationImpl.create(expr.trim(), this);
            
            // mark the rows/columns that impact the deriv, and evaluate values
            if (deriv != null && deriv.isConverted()) {
                for (Taggable d : deriv.getAffectedBy()) {
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
        DerivationImpl deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null) {
            Derivable elem = deriv.getTarget();
            for (Taggable d : deriv.getAffectedBy()) {
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
        return isSet(sf_IS_DERIVED_CELL_FLAG);
        //DerivationImpl deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        //return deriv != null;       
    }

    @Override
    public List<TableElement> getAffectedBy()
    {
        DerivationImpl deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
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
        DerivationImpl deriv = getTable() != null ? getTable().getCellDerivation(this) : null;
        if (deriv != null) {
            deriv.recalculateTarget();
            
            fireEvents(TableElementEventType.OnRecalculate);
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

    @Override
    synchronized public Iterable<Subset> subsets()
    {
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        if (parent != null) {
            Set<SubsetImpl> subsets = parent.getCellSubsets(this);
            if (subsets != null) {
                List<Subset> subsetsList = new ArrayList<Subset>(subsets.size());
                subsetsList.addAll(subsets);
                return new BaseElementIterable<Subset>(subsets);
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
        // clear label, also resets index, if rows are indexed
        setLabel(null); 
        
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
        this.resetElemProperties();
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
                isPendings() ? "pending" : isNull() ? "null" : (isErrorValue() && getErrorMessage() != null ? getErrorMessage() : getCellValue().toString()),
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

    @Override
    public boolean isLeftAligned()
    {
        return !(isNumericValue() || isBooleanValue());
    }

    @Override
    public boolean isRightAligned()
    {
        return isNumericValue();
    }

    @Override
    public boolean isCenterAligned()
    {
        return isBooleanValue();
    }

    @Override
    public boolean tag(String... tags)
    {
        boolean anyAdded = false;
        if (tags != null && tags.length > 0) {
            Set<Tag> newTags = Tag.encodeTags(tags, getTableContext());            
            Set<Tag> curTags = getTable() != null ? getTable().getCellTags(this) : null;            
            if (curTags != null) 
                anyAdded = curTags.addAll(newTags);
            else {
                if (getTable() != null) {
                    anyAdded = true;
                    getTable().setCellTags(this, newTags);
                }
            }
        }
        
        return anyAdded;
    }

    @Override
    public boolean untag(String... tags)
    {
        boolean removedAny = false;
        if (tags != null && tags.length > 0) {
            Set<Tag> oldTags = Tag.encodeTags(tags, getTableContext(), false);            
            Set<Tag> curTags = getTable() != null ? getTable().getCellTags(this) : null;            
            if (curTags != null) 
                removedAny = curTags.removeAll(oldTags);
        }
        
        return removedAny;
    }

    @Override
    public void setTags(String... tags)
    {
        Set<Tag> newTags = null;
        if (tags != null && tags.length > 0) 
            newTags = Tag.encodeTags(tags, getTableContext());            
        
        if (getTable() != null)
            getTable().setCellTags(this, newTags);
    }

    @Override
    public String[] getTags()
    {
        return Tag.decodeTags(getTable() != null ? getTable().getCellTags(this) : null);
    }
    
    @Override
    public boolean isTagged(String... tags)
    {
        Set<Tag> curTags = getTable() != null ? getTable().getCellTags(this) : null;
        if (curTags == null || curTags.isEmpty())
            return false;
        
        // if param arg is null or empty, return true, 
        // as this element is tagged
        
        if (tags == null || tags.length == 0)
            return true;
        
        // otherwise, encode tags and use set math to return answer
        Set<Tag> queryTags = Tag.encodeTags(tags, getTableContext(), false); 
        if (queryTags == null || queryTags.isEmpty())
            return false;
        
        return curTags.containsAll(queryTags);
    }
}
