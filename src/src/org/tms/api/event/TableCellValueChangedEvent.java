package org.tms.api.event;

import org.tms.api.Cell;
import org.tms.api.TableElement;

public class TableCellValueChangedEvent extends TableElementEvent
{
    private static final long serialVersionUID = 3025887968647194535L;

    public static final TableCellValueChangedEvent createNewValueEvent(Cell cell, Object oldValue, Object newValue)
    {
        return new TableCellValueChangedEvent(cell, TableElementEventType.OnNewValue, oldValue, newValue);
    }
    
    public static final TableCellValueChangedEvent createBeforeNewValueEvent(Cell cell, Object oldValue, Object newValue)
    {
        return new TableCellValueChangedEvent(cell, TableElementEventType.OnBeforeNewValue, oldValue, newValue);
    }

    private Object m_oldValue;
    private Object m_newValue;
    
    private TableCellValueChangedEvent(TableElement source, TableElementEventType evT, Object oldValue, Object newValue)
    {
        super(evT, source);
        
        m_oldValue = oldValue;
        m_newValue = newValue;
    }
    
    public Object getOldValue() { return m_oldValue; }
    
    public Object getNewValue() { return m_newValue; }
}
