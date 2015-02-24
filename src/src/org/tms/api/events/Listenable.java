package org.tms.api.events;

import java.util.List;

public interface Listenable
{
    public boolean addListeners(TableElementEventType evT, TableElementListener... tel);
    public boolean removeListeners(TableElementEventType evT, TableElementListener... tel);
    public List<TableElementListener> getListeners(TableElementEventType... evTs);
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs);
    public boolean hasListeners(TableElementEventType... evTs);
}
