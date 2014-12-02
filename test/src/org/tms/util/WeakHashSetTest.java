package org.tms.util;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.Is.*;

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
}
