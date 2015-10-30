package org.tms.api.derivables.exceptions;

import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;

public class InvalidOperatorException extends TableException 
{
    private static final long serialVersionUID = -1355100992819109213L;

    public InvalidOperatorException(String msg)
    {
        super(msg, TableErrorClass.Invalid);
    }
    
    public InvalidOperatorException(Exception e)
    {
        super(e, TableErrorClass.Invalid);
    }
}
