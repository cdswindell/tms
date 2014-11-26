package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class Table extends TableElement
{
    private Cell [] m_cells;
    private Row [] m_rows;
    private Column [] m_cols;
    
    public Table()
    {
        super(ElementType.Table);
        reset();
    }
    
    @Override
    protected void reset()
    {
        m_cells = null;
        m_rows = null;
        m_cols = null;
        
        setIndex(-1);
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
    }
}
