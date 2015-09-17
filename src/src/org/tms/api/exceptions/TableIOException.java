package org.tms.api.exceptions;

import java.io.IOException;

import org.tms.api.ElementType;

public class TableIOException extends TableException
{
    private static final long serialVersionUID = 6105145136515017640L;

    public TableIOException(IOException e)
    {
        super(ElementType.Table, e.getMessage(), TableErrorClass.IO);
    }
}
