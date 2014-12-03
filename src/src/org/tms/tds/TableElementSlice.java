package org.tms.tds;

import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

abstract class TableElementSlice extends TableElement
{
    private Set<Range> m_ranges;

    public TableElementSlice(ElementType eType, TableElement e)
    {
        super(eType, e);
    }

    @Override
    protected Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numRanges:
                return getRanges().size();
                
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
                    throw new IllegalStateException("No initialization available for " + 
                                                    this.getClass().getSimpleName() +" Property: " + tp);                       
            }
        }
        
        // initialize other member fields
        m_ranges = null;
    }
    
    protected boolean add(Range r)
    {
        if (r != null) {
            /*
             *  if the range doesn't contain the row, use the range method to do all the work
             *  TableElementSlice.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(this);
            
            return getRanges().add(r);
        }
        
        return false;
    }

    protected boolean remove(Range r)
    {
        if (r != null) {
            /*
             * if the range contains the row, use the range method to do all the work
             * TableElementSlice.remove will be called again to finish up
             */
            if (r.contains(this))
                return r.remove(this);
            
            return getRanges().remove(r);
        }
        
        return false;
    }
    
    private Set<Range> getRanges()
    {
        if (m_ranges == null)
            m_ranges = new LinkedHashSet<Range>();
        
        return m_ranges;
    }
}
