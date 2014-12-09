package org.tms.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

public class WeakHashSetTest
{

    @Test
    public void testWeakHashSet()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        whs = new WeakHashSet<String>(10);
        assertThat(whs, notNullValue());
    }

	@Test
	public final void testWeakHashSetSetOfT() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s);       
        assertThat(whs, notNullValue());        
        assertThat(whs.size(), is(s.size()));
	}

	@Test
	public final void testIterator() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        
        Iterator<String> sIter = whs.iterator();
        assertThat(sIter, notNullValue());
        
        assertThat(sIter.hasNext(), is(true));
        while(sIter.hasNext()) {
        	String str = sIter.next();
        	assertThat(whs.contains(str), is(true));
        	assertThat(s.remove(str), is(true));
        }
        
        assertThat(s.isEmpty(), is(true));
	}

	@Test
	public final void testToArray() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        
        Object [] o = whs.toArray();
        assertThat(o, notNullValue());        
        assertThat(o.length, is(s.size()));
        
        for (Object str : o) {
        	assertThat(whs.contains(str), is(true));
        }
	}

	@Test
	public final void testToArraySArray() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        
        String [] sa = whs.toArray(new String [] {});
        assertThat(sa, notNullValue());        
        assertThat(sa.length, is(s.size()));
        
        for (String str : sa) {
        	assertThat(whs.contains(str), is(true));
        }
	}

	@Test
	public final void testContainsAll() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        
        assertThat(whs.containsAll(s), is(true));
        assertThat(s.containsAll(whs), is(true));        
	}

	@Test
	public final void testAddAll() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(); 
        
        assertThat(whs.addAll(s), is(true));
        assertThat(whs.containsAll(s), is(true));
        assertThat(s.containsAll(whs), is(true));        
	}

	@Test
	public final void testRetainAll() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        whs.add("123");
        whs.add("456");
        
        assertThat(whs.size(), is(s.size() + 2));
        assertThat(whs.containsAll(s), is(true));
        assertThat(s.containsAll(whs), is(false));
        
        assertThat(whs.retainAll(s), is(true));
        
        assertThat(whs.size(), is(s.size()));
        assertThat(whs.containsAll(s), is(true));
        assertThat(s.containsAll(whs), is(true));        
	}

	@Test
	public final void testRemoveAll() 
	{
		Set<String> s = createTestSet();
        Set<String> whs = new WeakHashSet<String>(s); 
        whs.add("123");
        whs.add("456");
        
        assertThat(whs.size(), is(s.size() + 2));
        assertThat(whs.containsAll(s), is(true));
        assertThat(s.containsAll(whs), is(false));
        
        assertThat(whs.removeAll(s), is(true));
        
        assertThat(whs.size(), is(2));
        assertThat(whs.containsAll(s), is(false));
        
        for (String str : s) {
        	assertThat(whs.contains(str), is(false));
        }
	}

	@Test
	public final void testClone() 
	{
		Set<String> s = createTestSet();
		WeakHashSet<String> whs = new WeakHashSet<String>(s); 
        
        Set<String> s2 = whs.clone();
        assertThat(s2, notNullValue());
        assertThat(s.containsAll(s2), is(true));
        assertThat(s2.containsAll(s), is(true));
	}

	@Test
	public final void testToString() 
	{
		Set<String> s = createTestSet();
		WeakHashSet<String> whs = new WeakHashSet<String>(s); 
		assertThat(whs.toString(), notNullValue());
	}
	
    @Test
    public void testSize() throws InterruptedException
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.size(), is(0));
        
        String s1 = new String("abc");
        whs.add(s1);
        assertThat(whs.size(), is(1));
        
        String s2 = new String("def");
        whs.add(s2);
        assertThat(whs.size(), is(2));
        
        s1 = null;
        System.gc();
        
        assertThat(whs.contains(s2), is(true));
        
        Thread.sleep(1000);
                
        assertThat(whs.size(), is(1));      
    }

    @Test
    public void testIsEmpty()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.isEmpty(), is(true));       
        
        String s1 = new String("abc");
        whs.add(s1);
        assertThat(whs.isEmpty(), is(false));       
        
        whs.clear();
        assertThat(whs.isEmpty(), is(true));       
    }

    @Test
    public void testContains()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.size(), is(0));
        
        String s1 = new String("abc");
        assertThat(whs.add(s1), is(true));
        assertThat(whs.size(), is(1));
        assertThat(whs.contains(s1), is(true));
        
        assertThat(whs.remove(s1), is(true));
        assertThat(whs.contains(s1), is(false));
    }

    @Test
    public void testAdd()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.size(), is(0));
        
        String s1 = new String("abc");
        assertThat(whs.add(s1), is(true));
        assertThat(whs.size(), is(1));
        assertThat(whs.contains(s1), is(true));
    }

    @Test
    public void testRemove()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.size(), is(0));
        
        String s1 = new String("abc");
        assertThat(whs.add(s1), is(true));
        assertThat(whs.size(), is(1));
        assertThat(whs.contains(s1), is(true));
        
        assertThat(whs.remove(s1), is(true));
        assertThat(whs.contains(s1), is(false));
    }

    @Test
    public void testClear()
    {
        Set<String> whs = new WeakHashSet<String>();       
        assertThat(whs, notNullValue());
        
        assertThat(whs.size(), is(0));
        assertThat(whs.isEmpty(), is(true));       
        
        String s1 = new String("abc");
        whs.add(s1);
        assertThat(whs.size(), is(1));
        assertThat(whs.isEmpty(), is(false));       
        
        whs.clear();
        assertThat(whs.size(), is(0));
        assertThat(whs.isEmpty(), is(true));       
    }
    
	private Set<String> createTestSet()
	{
	    Set<String> s = new LinkedHashSet<String>(4);
	    
	    s.add(new String("abc"));
	    s.add(new String("def"));
	    s.add(new String("ghi"));
	    s.add(new String("jkl"));
	    
	    return s;
	}
}
