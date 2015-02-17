package org.tms.api.event;

import java.util.List;

public interface Observable
{
    public boolean addListener(TableElementEventType evT, TableElementListener... tel);
    public boolean removeListener(TableElementEventType evT, TableElementListener... tel);
    public List<TableElementListener> getListeners(TableElementEventType... evTs);
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs);
}
