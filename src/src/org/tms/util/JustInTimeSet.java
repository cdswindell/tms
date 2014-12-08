package org.tms.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class JustInTimeSet<E> implements Set<E>
{
    private Set<E> m_backingSet;

    synchronized private void createBackingSet()
    {
        if (m_backingSet == null)
            m_backingSet = new WeakHashSet<E>();
    }
    
    @Override
    public int size()
    {
        return m_backingSet == null ? 0 : m_backingSet.size();
    }

    @Override
    public boolean isEmpty()
    {
        return m_backingSet == null ? true : m_backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return m_backingSet == null ? false : m_backingSet.contains(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator()
    {
        return m_backingSet == null ? (Iterator<E>)Collections.emptySet().iterator() : clone().iterator();
    }

    @Override
    public Object[] toArray()
    {
        return m_backingSet == null ? new Object [] {} : m_backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {        
        return m_backingSet == null ? null : m_backingSet.toArray(a);
    }

    @Override
    public boolean add(E e)
    {
        createBackingSet();
        return m_backingSet.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        createBackingSet();
        return m_backingSet.addAll(c);
    }

    @Override
    public boolean remove(Object o)
    {
        return m_backingSet == null ? false : m_backingSet.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return m_backingSet == null ? false : m_backingSet.containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return m_backingSet == null ? false : m_backingSet.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return m_backingSet == null ? false : m_backingSet.removeAll(c);
    }

    @Override
    public void clear()
    {
        if (m_backingSet != null)
            m_backingSet.clear();
    }
    
    public Set<E> clone()
    {
        if (m_backingSet == null)
            return Collections.emptySet();
        else
            return ((WeakHashSet<E>)m_backingSet).clone();
    }
}
