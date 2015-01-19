package org.tms.tds;

import java.util.ArrayList;
import java.util.List;

import org.tms.api.Cell;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.teq.Derivation;
import org.tms.util.JustInTimeSet;

abstract class TableSliceElement extends TableCellsElementImpl implements Derivable
{
    abstract protected TableSliceElement insertSlice(int idx);
    abstract protected TableSliceElement setCurrent();
    
    private JustInTimeSet<RangeImpl> m_ranges;
    private boolean m_inUse;
    private Derivation m_deriv;

    public TableSliceElement(ElementType eType, TableElementImpl e)
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
        m_inUse = inUse;
    }
    
    protected List<RangeImpl> getRanges()
    {
        return new ArrayList<RangeImpl>(m_ranges.clone());
    } 
    
    @Override
    public boolean isDerived()
    {
        return m_deriv != null;       
    }
    
    @Override
    public Derivation getDerivation()
    {
        return m_deriv;
    }
    
    @Override
    public void setDerivation(String expr)
    {
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
        if (isDerived()) {
            m_deriv.recalculateTarget();
            
            // recalculate dependent columns
            TableImpl table = getTable();
            if (table != null) 
                table.recalculateAffected(this);
        }
    }
       
    void compactIfNeeded(ArrayList<? extends TableSliceElement> cols, int capacity) 
    {
		// TODO Auto-generated method stub
		
	}
    
    protected boolean add(RangeImpl r)
    {
        if (r != null) {
            /*
             *  if the range doesn't contain the row, use the range method to do all the work
             *  TableSliceElement.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(this);
            
            return m_ranges.add(r);
        }
        
        return false;
    }

    protected boolean remove(RangeImpl r)
    {
        if (r != null) {
            /*
             * if the range contains the element, use the range method to do all the work
             * TableSliceElement.remove will be called again to finish up
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
    
    protected Iterable<RangeImpl> rangeIterable()
    {
        return new BaseElementIterable<RangeImpl>(m_ranges);
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
        return (getTable() != null ? getTable().isReadOnly() : false) || super.isReadOnly();
    }
    
    @Override
    public boolean isSupportsNull()
    {
        return (getTable() != null ? getTable().isSupportsNull() : false) || super.isSupportsNull();
    }
    
    @Override
    public void clear() 
    {
        fill(null);
    }

    @Override
    public void fill(Object o) 
    {
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

	@Override
	public boolean isNull() 
	{
		return getNumCells() == 0;
	}   
}
