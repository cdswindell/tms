package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Cell extends TableElement
{
    private Object m_cellValue;
    
    public Cell(Table t)
    {
        super(ElementType.Cell, t);
    }

    /*
     * Field getters/setters
     */
    
    protected Object getCellValue()
    {
        return m_cellValue;
    }
    
    protected void setCellValue(Object value)
    {
        m_cellValue = value;
    }
    
    /*
     * Overridden methods
     */
    
    @Override
    protected int getNumCells()
    {
        // Degenerate case
        return 1;
    }
    
    /**
     * A cell is empty if it is null
     */
    @Override
    protected boolean isEmpty()
    {
        return m_cellValue == null;
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
        m_cellValue = null;
    }
}
