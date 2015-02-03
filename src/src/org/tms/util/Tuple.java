package org.tms.util;

public class Tuple<T>
{
    private T m_elem1;
    private T m_elem2;
    
    public Tuple(T e1, T e2) 
    {
        m_elem1 = e1;
        m_elem2 = e2;
    }

    public T getFirstElement()
    {
        return m_elem1;
    }
    
    public T getSecondElement()
    {
        return m_elem2;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_elem1 == null) ? 0 : m_elem1.hashCode());
        result = prime * result + ((m_elem2 == null) ? 0 : m_elem2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) 
            return true;
        if (obj == null) 
            return false;
        if (getClass() != obj.getClass()) 
            return false;
        Tuple<?> other = (Tuple<?>) obj;
        if (m_elem1 == null) {
            if (other.m_elem1 != null) 
                return false;
        }
        
        else if (!m_elem1.equals(other.m_elem1)) 
            return false;
        if (m_elem2 == null) {
            if (other.m_elem2 != null) 
                return false;
        }
        else if (!m_elem2.equals(other.m_elem2)) 
            return false;
        
        return true;
    }
}
