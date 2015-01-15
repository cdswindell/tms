package org.tms.tds;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Cell;
import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
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
    private Set<Derivable> m_affects;

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
    public String getDerivation()
    {
        if (m_deriv != null)
            return m_deriv.getAsEnteredExpression();
        else
            return null;
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
                for (Derivable d : m_deriv.getAffectedBy()) {
                    TableSliceElement tse = (TableSliceElement)d;
                    tse.addToAffects(elem);
                }
                
                m_inUse = true;
                recalculate();
            }  
        }
    }
    
    @Override
    public List<Derivable> getAffectedBy()
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
            for (Derivable d : m_deriv.getAffectedBy()) {
                TableSliceElement tse = (TableSliceElement)d;
                tse.removeFromAffects(elem);
            }
            
            m_deriv.destroy();
            m_deriv = null;
        }        
    }
    
    @Override
    public void recalculate()
    {
        if (isDerived()) {
            deactivateAutoRecalculation();
            m_deriv.recalculateTarget();
            activateAutoRecalculation();
            
            // recalculate dependent columns
            if (getTable().isAutoRecalculate()) {
                List<Derivable> affects = m_deriv.getTarget().getAffects();
                if (affects != null) {
                    for (Derivable d : affects) {
                        d.recalculate();
                    }
                }
            }
        }
    }
       
    @Override
    public List<Derivable> getAffects()
    {
        int numAffects = 0;
        List<Derivable> affects = new ArrayList<Derivable>(m_affects != null ? (numAffects = m_affects.size()) : 0);
        
        // attempt to order the elements so that they can be recalculated in one pass
        // (independent elements first, dependent elements last)
        if (numAffects == 1)
            affects.addAll(m_affects);
        else if (numAffects > 1) {
            // TODO: implement
            affects.addAll(m_affects);
        }        
        
        return affects;
    }
    
    /*
     * Class-specific methods
     */
    
    protected void addToAffects(Derivable elem)
    {
        m_affects.add(elem);
    }
    
    protected void removeFromAffects(Derivable elem)
    {
        m_affects.remove(elem);
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
    
    protected void deactivateAutoRecalculation()
    {
        if (getTable() != null)
            getTable().deactivateAutoRecalculation();
    }
    
    protected void activateAutoRecalculation()
    {
        if (getTable() != null)
            getTable().activateAutoRecalculation();
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
        m_affects = new LinkedHashSet<Derivable>();
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
        
        deactivateAutoRecalculation();
        pushCurrent();
        
        // Clear derivation, since fill should override
        clearDerivation();
        
        boolean setSome = false;
        try {
            boolean readOnlyExceptionEncountered = false;
            for (Cell c : cells()) {
                if (c != null) {
                    try {
                        ((CellImpl)c).setCellValue(o, true);
                        setSome = true;
                    }
                    catch (ReadOnlyException e) {
                        readOnlyExceptionEncountered = true;
                    }
                }
            }
            
            if (setSome)
                this.setInUse(true); 
            else if (readOnlyExceptionEncountered)
                throw new ReadOnlyException(this, TableProperty.CellValue);
        }
        finally { 
            popCurrent();
            activateAutoRecalculation();
        }
        
        if (setSome && parent != null && parent.isAutoRecalculate()) {
            List<Derivable> affecteds = getAffects();
            if (affecteds != null) {
                for (Derivable affected : affecteds) {
                    affected.recalculate();
                }
            }
        }
    }

	@Override
	public boolean isNull() 
	{
		return getNumCells() == 0;
	}   
}
