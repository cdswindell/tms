package org.tms.tds;

import java.util.ArrayList;
import java.util.List;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.util.JustInTimeSet;

abstract class TableElementSlice extends TableElement
{
    private JustInTimeSet<Range> m_ranges;
    private boolean m_inUse;
    private int m_offset;

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
                return m_ranges.size();
                
            case Ranges:
                return getRanges();
                
            case isInUse:
                return isInUse();
                
            case Offset:
                return getOffset();
                
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
        m_ranges = new JustInTimeSet<Range>();
        m_inUse = false;
        m_offset = -1;
    }
    
    boolean isInUse()
    {
        return m_inUse;
    }
    
    void setInUse(boolean inUse)
    {
        m_inUse = inUse;
    }
    
    int getOffset()
    {
        return m_offset;
    }
    
    void setOffset(int offset)
    {
        m_offset = offset;
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
            
            return m_ranges.add(r);
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
            
            return m_ranges.remove(r);
        }
        
        return false;
    }
    
    protected List<Range> getRanges()
    {
        return new ArrayList<Range>(m_ranges.clone());
    }   
}
