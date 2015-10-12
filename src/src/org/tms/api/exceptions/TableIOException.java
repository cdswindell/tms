package org.tms.api.exceptions;

import java.io.IOException;

import net.sf.jasperreports.engine.JRException;

import org.tms.api.ElementType;

public class TableIOException extends TableException
{
    private static final long serialVersionUID = 620369785058070520L;
    
    private Exception m_rootCause;

    public TableIOException(IOException e)
    {
        super(ElementType.Table, e.getMessage(), TableErrorClass.IO);
        m_rootCause = e;
    }
    
    public TableIOException(JRException e)
    {
        super(ElementType.Table, e.getMessage(), TableErrorClass.IO);
        m_rootCause = e;
    }
    
    public Exception getRootCause()
    {
        return m_rootCause;
    }
}
