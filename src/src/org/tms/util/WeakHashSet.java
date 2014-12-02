package org.tms.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A weak HashSet. An element stored in the WeakHashSet might be
 * garbage collected, if there is no strong reference to this element.
 */

public class WeakHashSet<T> implements Set<T> 
{
    private WeakHashMap<T, Object> m_backingMap;
    public WeakHashSet()
    {
        m_backingMap = new WeakHashMap<T, Object>();
    }
    
    public WeakHashSet(int capacity)
    {
        m_backingMap = new WeakHashMap<T, Object>(capacity);
    }
    
    @Override
    public int size()
    {
        return m_backingMap.size();
    }

    @Override
    public boolean isEmpty()
    {
        return m_backingMap.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return m_backingMap.containsKey(o);
    }

    @Override
    public Iterator<T> iterator()
    {
        return m_backingMap.keySet().iterator();
    }

    @Override
    public Object[] toArray()
    {
        throw new IllegalStateException("Unsupported: toArray");
    }

    @Override
    public <S> S[] toArray(S[] a)
    {
        throw new IllegalStateException("Unsupported: toArray");
    }

    @Override
    public boolean add(T e)
    {
        boolean isNewElement = !contains(e);
        m_backingMap.put(e, null);
        return isNewElement;
    }

    @Override
    public boolean remove(Object o)
    {
        return m_backingMap.keySet().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return m_backingMap.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        boolean addedAny = false;
        
        if (c != null) {
            Iterator<? extends T> iter = c.iterator();
            while (iter != null && iter.hasNext()) {
                if (add(iter.next()))
                    addedAny = true;
            }
        }
            
        return addedAny;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return m_backingMap.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return m_backingMap.keySet().removeAll(c);
    }

    @Override
    public void clear()
    {
        m_backingMap.clear();
    }  
}