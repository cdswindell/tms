package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Column extends TableElementSlice
{
    public Column(Table parentTable)
    {
        super(ElementType.Column, parentTable);
    }

    @Override
    protected boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return false;
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
                    throw new IllegalStateException("No initialization available for Column Property: " + tp);                       
            }
        }
    }
}
