package org.tms.api.exceptions;

import org.tms.api.Access;
import org.tms.api.ElementType;

public class InvalidAccessException extends InvalidException
{
    private static final long serialVersionUID = 8226984907708323592L;
    
    private ElementType m_child;
    private Access m_access;
    private boolean m_insert;
    private Object[] m_metaData;
    
    public InvalidAccessException(ElementType parent, ElementType child, Access access, boolean insert, Object... md)
    {
        super(parent, 
              String.format("Invalid %s Request: %s Child: %s", (insert ? "Insert" : "Get"), access, child));
        
        m_child = child;
        m_access = access;
        m_insert = insert;
        m_metaData = md;
    }

    public ElementType getChildType()
    {
        return m_child;
    }

    public Access getAccess()
    {
        return m_access;
    }

    public boolean isInsert()
    {
        return m_insert;
    }

    public Object getMetaData()
    {
        return m_metaData;
    }
}
