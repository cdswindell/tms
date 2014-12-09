package org.tms.api.exceptions;

import org.tms.api.ElementType;

public class WriteProtectionException extends InvalidException
{
    private static final long serialVersionUID = -5537432726159673773L;

    public WriteProtectionException(ElementType et, String msg)
    {
        super(et, msg);
    }

    public WriteProtectionException(ElementType et)
    {
        super(et, "Write-protected");
    }

    public WriteProtectionException(String message)
    {
        super(ElementType.Cell, message);
    }

    public WriteProtectionException()
    {
        super(ElementType.Cell, "Write-protected");
    }
}
