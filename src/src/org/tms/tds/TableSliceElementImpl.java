package org.tms.tds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.Range;
import org.tms.api.Row;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.teq.Derivation;
import org.tms.util.JustInTimeSet;

abstract class TableSliceElementImpl extends TableCellsElementImpl implements Derivable, TableRowColumnElement
{
    abstract protected TableSliceElementImpl insertSlice(int idx);
    abstract protected TableSliceElementImpl setCurrent();
    
    private JustInTimeSet<RangeImpl> m_ranges;
    private boolean m_enforceDataType;
    private boolean m_supportsNull;
    private boolean m_readOnly;
    private boolean m_inUse;
    private Derivation m_deriv;

    public TableSliceElementImpl(ElementType eType, TableElementImpl e)
    {
        super(eType, e);
    }

    /*
     * Field getters/setters
     */
    
    boolean isInUse()
    {
        return m_inUse;
    }
    
    void setInUse(boolean inUse)
    {
        if (inUse)
            vetElement();
        m_inUse = inUse;
    }
    
    @Override
    protected boolean isEnforceDataType()
    {
        return m_enforceDataType;
    }
    
    @Override
    protected void setEnforceDataType(boolean dataTypeEnforced)
    {
        m_enforceDataType = dataTypeEnforced;
    }
    
    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else
            return this.isEnforceDataType();
    }

    @Override
    protected boolean isSupportsNull()
    {
        return m_supportsNull;
    }
    
    @Override
    protected void setSupportsNull(boolean supportsNull)
    {
        m_supportsNull = supportsNull;
    }
    
    @Override
    public boolean isNullsSupported()
    {
        return (getTable() != null ? getTable().isNullsSupported() : false) && isSupportsNull();
    }
    
    public List<Range> getRanges()
    {
        vetElement();
        return new ArrayList<Range>(m_ranges.clone());
    } 
    
    protected Set<RangeImpl> getRangesInternal()
    {
        return m_ranges;
    } 
    
    @Override
    public boolean isDerived()
    {
        vetElement();
        return m_deriv != null;       
    }
    
    @Override
    public Derivation getDerivation()
    {
        vetElement();
        return m_deriv;
    }
    
    @Override
    public void setDerivation(String expr)
    {
        vetElement();
        
        // clear out any existing derivations
        if (m_deriv != null) 
            clearDerivation();
        
        if (expr != null && expr.trim().length() > 0) {
            m_deriv = Derivation.create(expr.trim(), this);
            
            // mark the rows/columns that impact the deriv, and evaluate values
            if (m_deriv != null && m_deriv.isConverted()) {
                Derivable elem = m_deriv.getTarget();
                for (TableElement d : m_deriv.getAffectedBy()) {
                    TableElementImpl tse = (TableElementImpl)d;
                    tse.registerAffects(elem);
                }
                
                m_inUse = true;
                recalculate();
            }  
        }
    }
    
    @Override
    public List<TableElement> getAffectedBy()
    {
        if (m_deriv != null)
            return m_deriv.getAffectedBy();
        else
            return null;
    }
    
    @Override
    public void clearDerivation()
    {
        if (m_deriv != null) {
            Derivable elem = m_deriv.getTarget();
            for (TableElement d : m_deriv.getAffectedBy()) {
                TableElementImpl tse = (TableElementImpl)d;
                tse.deregisterAffects(elem);
            }
            
            m_deriv.destroy();
            m_deriv = null;
        }        
    }
    
    @Override
    public void recalculate()
    {
        vetElement();
        if (isDerived()) {
            m_deriv.recalculateTarget();
            
            // recalculate dependent columns
            TableImpl table = getTable();
            if (table != null) 
                table.recalculateAffected(this);
        }
    }
       
    @Override
    public void sort() 
    {
        vetElement();
        TableImpl parent = getTable();
        if (parent == null)
            throw new IllegalTableStateException("Table Required");
        
        parent.sort(this);
    }

    @Override
    public void sort(Comparator<Cell> cellSorter) 
    {
        vetElement();
        TableImpl parent = getTable();
        if (parent == null)
            throw new IllegalTableStateException("Table Required");
        
        parent.sort(this, cellSorter);
    }
    
    
    CellImpl getCellInternal(TableSliceElementImpl tse)
    {
        if (this instanceof RowImpl)
            return ((RowImpl)this).getCellInternal((ColumnImpl)tse, false);
        else if (this instanceof ColumnImpl)
            return ((ColumnImpl)this).getCellInternal((RowImpl)tse, false);
        else
            throw new IllegalTableStateException("Table Slice ELement Required");
    }
    
    protected boolean add(RangeImpl r)
    {
        vetElement();
        if (r != null) {
            /*
             *  if the range doesn't contain the row, use the range method to do all the work
             *  TableSliceElementImpl.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(this);
            
            return m_ranges.add(r);
        }
        
        return false;
    }

    /**
     * Remove the reference from the specified RangeImpl to this TableSliceElementImpl, removing
     * this TableSliceElementImple from the specified range, if it has not already been removed.
     * 
     * Returns true if the specified RangeImpl was successfully removed
     * 
     * @param r RangeImpl
     */
    @Override
    protected boolean remove(RangeImpl r)
    {
        if (r != null) {
            /*
             * if the range contains the element, use the range method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
        	if (r.contains(this))
        		r.remove(this);
        	
        	return m_ranges.remove(r);
        }
        
        return false;
    }
    
    protected void removeFromAllRanges()
    {
    	// remove this table slice element from all ranges
    	m_ranges.forEach(r -> {if (r != null) r.remove(this);});
    }
    
    protected void pushCurrent()
    {
        if (getTable() != null)
            getTable().pushCurrent();
    }
    
    protected void popCurrent()
    {
        if (getTable() != null)
            getTable().popCurrent();
    }
    
    /*
     * Overridden methods
     */
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
                    throw new IllegalStateException("No initialization available for " + 
                                                    this.getClass().getSimpleName() +" Property: " + tp);                       
            }
        }
        
        // initialize other member fields
        m_ranges = new JustInTimeSet<RangeImpl>();
        m_inUse = false;
    } 

    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numRanges:
                return m_ranges.size();
                
            case Ranges:
                return getRanges();
                
            case isInUse:
                return isInUse();
                
            case Derivation:
                return getDerivation();
                
            default:
                return super.getProperty(key);
        }
    }   

    @Override
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
    
    @Override 
    protected void setReadOnly(boolean readOnly)
    {
        m_readOnly = readOnly;
    }
    
    @Override
    protected boolean isWriteProtected()
    {
        return isReadOnly() ||
               (getTable() != null ? getTable().isWriteProtected() : false);
                
    }
    
    @Override
    public void clear() 
    {
        fill(null);
    }

    @Override
    public void fill(Object o) 
    {
        vetElement();
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        
        if (this.isReadOnly())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (o == null && !isSupportsNull())
            throw new NullValueException(this, TableProperty.CellValue);
        
        pushCurrent();       
        boolean setSome = false;
        try {
            // Clear derivation, since fill should override
            clearDerivation();
            
            boolean readOnlyExceptionEncountered = false;
            boolean nullValueExceptionEncountered = false;
            for (Cell c : cells()) {
                if (c != null) {
                    if (isDerived(c)) 
                        continue;
                    
                    try {
                        if (((CellImpl)c).setCellValue(o, true));
                            setSome = true;
                    }
                    catch (ReadOnlyException e) {
                        readOnlyExceptionEncountered = true;
                    }
                    catch (NullValueException e) {
                        nullValueExceptionEncountered = true;
                    }
                }
            }
            
            if (setSome)
                this.setInUse(true); 
            else if (readOnlyExceptionEncountered)
                throw new ReadOnlyException(this, TableProperty.CellValue);
            else if (nullValueExceptionEncountered)
                throw new NullValueException(this, TableProperty.CellValue);
        }
        finally { 
            popCurrent();
        }
        
        if (setSome && parent != null)
            parent.recalculateAffected(this);
    }

	private boolean isDerived(Cell c)
    {
        if (c.isDerived())
            return true;
        
        Derivable d = null;
        if (this instanceof Row) 
            d = c.getColumn();
        else 
            d = c.getRow();
        
        if (d != null && d.isDerived())
            return true;
        
        return false;
    }
	
    @Override
	public boolean isNull() 
	{
		return getNumCells() == 0;
	}   
}
