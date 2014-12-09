package org.tms.api.exceptions;

import org.tms.api.ElementType;

public class DataTypeEnforcementException extends InvalidException
{
    private static final long serialVersionUID = -237552622743138049L;

    private Class<? extends Object> m_allowed;
    private Class<? extends Object> m_rejected;
    
    public DataTypeEnforcementException(Class<? extends Object> expected, Object value)
    {
        super(ElementType.Cell, 
                String.format("DataType Mismatch: Allowed %s, Rejected: %s",                         
                        expected != null ? expected.getSimpleName() : "<null>", 
                        value != null ? value.getClass().getSimpleName() : "<null>"));

        m_allowed = expected;
        m_rejected = value != null ? value.getClass() : null;
    }

    public DataTypeEnforcementException(String message)
    {
        super(ElementType.Cell, message);
    }

    public DataTypeEnforcementException()
    {
        super(ElementType.Cell, "DataType Mismatch");
    }
    
    public Class<? extends Object> getAllowed()
    {
        return m_allowed;
    }
    
    public Class<? extends Object> getRejected()
    {
        return m_rejected;
    }
}
