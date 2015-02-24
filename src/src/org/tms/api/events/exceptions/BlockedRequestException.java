package org.tms.api.events.exceptions;

import org.tms.api.events.TableElementEvent;
import org.tms.api.exceptions.TableException;

public class BlockedRequestException extends TableException
{
    private static final long serialVersionUID = -1429942665494697443L;
    
    private TableElementEvent m_event;
    
    public BlockedRequestException(TableElementEvent e)
    {
        super("Request blocked by: " + e);
        m_event = e;
    }
    
    public TableElementEvent getEvent()
    {
        return m_event;
    }
}
