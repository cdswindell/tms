package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Row extends TableElementSlice
{
    protected Row(Table parentTable)
    {
        super(ElementType.Row, parentTable);
    }

    @Override
    protected boolean isEmpty()
    {
        return true;
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
                    throw new IllegalStateException("No initialization available for Row Property: " + tp);                       
            }
        }
    }
    
}
