package org.tms.api.derivables;

import org.tms.api.exceptions.TableException;

public class InvalidOperandsException extends TableException 
{
    private static final long serialVersionUID = -6350331317761531218L;

    public InvalidOperandsException(String msg)
    {
        super(msg);
    }
}
