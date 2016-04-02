package org.tms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Tuple<T> implements Iterator<T>, Iterable<T>
{
    private List<T> m_elems;
    private int m_idx;
    private int m_size;
    
    public Tuple(T e1, T e2) 
    {
    	m_elems = new ArrayList<T>(2);
    	m_elems.add(e1);
    	m_elems.add(e2);
    	
    	m_idx = 0;
    	m_size = 2;
    }

    @SafeVarargs
	public Tuple(T... elems ) 
    {
    	if (elems != null) {
	    	m_elems = new ArrayList<T>(elems.length);
	    	for (T elem : elems) {
	    		m_elems.add(elem);
	    	}
    	}
    	else
    		m_elems = Collections.emptyList();
    	
    	m_idx = 0;
    	m_size = m_elems.size();
    }

	public Tuple(Collection<T> elems ) 
    {
    	if (elems != null) 
    		m_elems = new ArrayList<T>(elems);
    	else
    		m_elems = Collections.emptyList();
    	
    	m_idx = 0;
    	m_size = m_elems.size();
    }

    public int size()
    {
    	return m_size;
    }
    
    public T get(int idx) 
    throws IndexOutOfBoundsException
    {
    	return m_elems.get(idx);
    }
    
    @SuppressWarnings("unchecked")
	public T [] toArray()
    {
    	return (T[])m_elems.toArray();
    }
    
    public T getFirstElement()
    {
        return m_size > 0 ? m_elems.get(0) : null;
    }
    
    public T getSecondElement()
    {
        return m_size > 1 ? m_elems.get(1) : null;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        for (T t: m_elems) {
        	result = prime * result + ((t == null) ? 0 : t.hashCode());
        }

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
        
        @SuppressWarnings("unchecked")
		Tuple<T> other = (Tuple<T>) obj;
        
        int thisSize = size();
        if (thisSize != other.size())
        	return false;
        
        for (int i = 0; i < thisSize; i++) {
        	T elem = get(i);
        	T otherElem = other.get(i);
        	
        	if (elem == null) {
                if (otherElem != null) 
                    return false;        		
        	}
            else if (!elem.equals(otherElem)) 
                return false;
        }
        
        return true;
    }

	@Override
	public Iterator<T> iterator() 
	{
		m_idx = 0;
		return this;
	}

	@Override
	public boolean hasNext() 
	{
		return m_idx < m_size;
	}

	@Override
	public T next() 
	{
		if (!hasNext())
            throw new NoSuchElementException();
			
		return get(m_idx++);
	}
}
