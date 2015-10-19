package org.tms.api.exceptions;

import org.tms.api.ElementType;

public class TableIOException extends TableException
{
    private static final long serialVersionUID = -4585163666422969101L;

    private Exception m_rootCause;

    public TableIOException(Exception e)
    {
        super(ElementType.Table, e.getMessage(), TableErrorClass.IO);
        m_rootCause = e;
    }
    
    public TableIOException(String msg)
    {
        super(msg);
    }

    public Exception getRootCause()
    {
        return m_rootCause;
    }
}
