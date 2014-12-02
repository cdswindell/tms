package org.tms.api.exceptions;

import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class TableException extends RuntimeException
{
    private static final long serialVersionUID = -4100871283289669659L;
    
    private TableErrorClass m_errorClass;
    private ElementType m_elementType;
    private TableProperty m_property;
    
    protected TableException(ElementType tet, TableProperty tp, TableErrorClass ec, String msg)
    {
        this(String.format("%s: %s->%s (%s)", ec, tet, tp, msg));
        m_elementType = tet;
        m_errorClass = ec;
        m_property = tp;       
    }

    protected TableException(ElementType tet, TableProperty tp, TableErrorClass ec)
    {
        this(String.format("%s: %s->%s", ec, tet, tp));
        m_elementType = tet;
        m_errorClass = ec;
        m_property = tp;       
    }

    protected TableException(ElementType tet, String msg, TableErrorClass ec)
    {
        this(String.format("%s: %s (%s)", ec, msg, tet));
        m_elementType = tet;
        m_errorClass = ec;
        m_property = null;       
    }

    public TableException(String msg, TableErrorClass ec)
    {
        this(String.format("%s: %s", msg, ec));
        m_errorClass = ec;
    }

    protected TableException(String message)
    {
        super(message);
    }

    public ElementType getTableElementType() { return m_elementType; }
    public TableProperty    getTableProperty()    { return m_property; }
    public TableErrorClass  getTableErrorClass()  { return m_errorClass; }
    
    @Override
    public String toString() { return this.getMessage(); }
}
