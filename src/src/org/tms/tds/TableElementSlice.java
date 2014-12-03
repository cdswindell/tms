package org.tms.tds;

import java.util.Collections;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.util.WeakHashSet;

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
                return getRangesField(FieldAccess.ReturnEmptyIfNull).size();
                
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
            
            return getRangesField().add(r);
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
            
            return getRangesField().remove(r);
        }
        
        return false;
    }
    
    private Set<Range> getRangesField(FieldAccess... fas)
    {
        FieldAccess fa = FieldAccess.checkAccess(fas);
        if (m_ranges == null) {
            if (fa == FieldAccess.ReturnEmptyIfNull)
                return Collections.emptySet();
            else
                m_ranges = new WeakHashSet<Range>();
        }
        
        return m_ranges;
    }
}
