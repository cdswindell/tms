package org.tms.api;

public enum TableProperty
{
    Label,
    Description;
    
    protected boolean m_system;
    protected boolean m_readOnly;
    
    private TableProperty()
    {
        this(false /* isSystem */,
             false /* isReadOnly */);
    }
    
    private TableProperty(boolean isSystem, boolean isReadOnly)
    {
        m_system = isSystem;
        m_readOnly = isReadOnly;
    }
    
    public boolean isSystem()
    {
        return m_system;
    }
    
    public boolean isReadOnly()
    {
        return m_readOnly;
    }
}
