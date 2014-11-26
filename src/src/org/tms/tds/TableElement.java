package org.tms.tds;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class TableElement extends BaseElement
{
    private int m_index = -1;
    private Table m_parentTable;

    public TableElement(ElementType eType)
    {
        super(eType);
    }
    
    public TableElement(ElementType eType, Table parentTable)
    {
        this(eType);
        m_parentTable = parentTable;
    }

    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Index:
                return m_index;
            case Table:
                return m_parentTable;
            default:
                break;
        }
        
        return super.getProperty(key);
    }
    
    public int getIndex()
    {
        return m_index ;
    }
    
    protected void setIndex(int idx)
    {
        m_index = idx;
    }
    
}
