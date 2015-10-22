package org.tms.tds;

import java.io.Serializable;

public class Tag implements Serializable
{
    private static final long serialVersionUID = -2947388357311876386L;
    
    private String m_label;
    
    public Tag(String label)
    {
        m_label = label;
    }
    
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public String toString()
    {
        return "Tag [" + m_label + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        if (m_label == null)
        {
            if (other.m_label != null) return false;
        }
        else if (!m_label.equals(other.m_label)) return false;
        return true;
    }  
}
