package org.tms.api.event;

import java.util.List;

import org.tms.api.ElementType;
import org.tms.api.Table;

public interface Listenable
{
    public ElementType getElementType();
    public Table getTable();
    
    public boolean addListener(TableElementEventType evT, TableElementListener... tel);
    public boolean removeListener(TableElementEventType evT, TableElementListener... tel);
    public List<TableElementListener> getListeners(TableElementEventType... evTs);
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs);
    public boolean hasListeners(TableElementEventType... evTs);
}
