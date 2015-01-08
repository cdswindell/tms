package org.tms.tds;

import java.util.ArrayList;
import java.util.List;

import org.tms.api.Derivable;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
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
    
    public boolean isDerived()
    {
        return m_deriv != null;       
    }
    
    public String getDerivation()
    {
        if (m_deriv != null)
            return m_deriv.getInfixExpression();
        else
            return null;
    }
    
    public void setDerivation(String expr)
    {
        if (m_deriv != null)
            m_deriv.destroy();
        
        m_deriv = Derivation.create(expr, this);
    }
    
    /*
     * Class-specific methods
     */
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
	protected boolean isEmpty() 
	{
		return getNumCells() == 0;
	}   
}
