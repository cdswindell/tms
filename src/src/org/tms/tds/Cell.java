package org.tms.tds;

import org.tms.api.ElementType;

public class Cell extends TableElement
{
    private boolean m_null;
    
    public Cell(Table t)
    {
        super(ElementType.Cell, t);
      
    }

    public boolean isNull()
    {
        return m_null;
    }

    public void setNull(boolean isNull)
    {
        m_null = isNull;
    }

    @Override
    protected void reset()
    {
        setNull(true);       
    }
}
