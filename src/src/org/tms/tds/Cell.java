package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Cell extends TableElement
{
    private boolean m_isEmpty;

    public Cell(Table t)
    {
        super(ElementType.Cell, t);

    }

    /**
     * A cell is empty if it has been explicitly set to empty
     */
    @Override
    protected boolean isEmpty()
    {
        return m_isEmpty;
    }

    private void setEmpty(boolean isEmpty)
    {
        m_isEmpty = isEmpty;
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
                    throw new IllegalStateException("No initialization available for Cell Property: " + tp);                       
            }
        }
        
        // initialize other variables
        setEmpty(true);
    }
}
