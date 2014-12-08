package org.tms.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

public class JustInTimeSetTest
{

    @Test
    public final void testSize()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>();
        
        assertThat(jit.size(), is(0));
        jit.addAll(s);
        assertThat(jit.size(), is(s.size()));
    }

    @Test
    public final void testIsEmpty()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>();
        
        assertThat(jit.isEmpty(), is(true));
        jit.addAll(s);
        assertThat(jit.isEmpty(), is(false));
        jit.removeAll(s);
        assertThat(jit.isEmpty(), is(true));
    }

    @Test
    public final void testContains()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        assertThat(jit.isEmpty(), is(false));
        
        String lastStr = null;
        for (String str : s) {
            lastStr = str;
            assertThat(jit.contains(str), is(true));
        }
        
        assertThat(s.contains(lastStr), is(true));
        s.remove(lastStr);
        assertThat(s.contains(lastStr), is(false));
        
        assertThat(jit.contains(lastStr), is(true));
        jit.remove(lastStr);
        assertThat(jit.contains(lastStr), is(false));      
    }

    @Test
    public final void testIterator()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        Iterator<String> sIter = jit.iterator();
        assertThat(sIter, notNullValue());
        assertThat(sIter.hasNext(), is(true));
        
        while(sIter.hasNext()) {
            String str = sIter.next();
            assertThat(jit.contains(str), is(true));
            assertThat(s.contains(str), is(true));
            s.remove(str);
        }
        
        assertThat(sIter.hasNext(), is(false));
        assertThat(s.size(), is(0));
    }

    @Test
    public final void testToArray()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        assertThat(jit.isEmpty(), is(false));
        
        Object [] sa = jit.toArray();
        assertThat(sa, notNullValue());
        assertThat(sa.length, is(s.size()));
        
        assertThat(jit.contains(sa[0]), is(true));
        assertThat(jit.contains(sa[3]), is(true));        
    }

    @Test
    public final void testToArrayTArray()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        assertThat(jit.isEmpty(), is(false));
        
        String [] sa = jit.toArray(new String[] {});
        assertThat(sa, notNullValue());
        assertThat(sa.length, is(s.size()));
        
        assertThat(jit.contains(sa[0]), is(true));
        assertThat(jit.contains(sa[3]), is(true));        
    }

    @Test
    public final void testAdd()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>();
        
        for (String str : s) {
            assertThat(jit.contains(str), is(false));
            jit.add(str);
            assertThat(jit.contains(str), is(true));
        }
        
        assertThat(jit.size(), is(4));
    }

    @Test
    public final void testAddAll()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>();
        
        assertThat(jit.size(), is(0));
        jit.addAll(s);
        assertThat(jit.size(), is(s.size()));
 
        for (String str : s) {
            assertThat(jit.add(str), is(false));
            assertThat(jit.contains(str), is(true));
        }
    }

    @Test
    public final void testRemove()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>();
        
        for (String str : s) {
            assertThat(jit.contains(str), is(false));
            jit.add(str);
            assertThat(jit.contains(str), is(true));
            jit.remove(str);
            assertThat(jit.contains(str), is(false));
        }
        
        assertThat(jit.size(), is(0));
    }

    @Test
    public final void testContainsAll()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        assertThat(jit.containsAll(s), is(true));
    }

    @Test
    public final void testRetainAll()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        assertThat(jit.retainAll(s), is(false));
        
        jit.add("123");
        jit.add("456");       
        assertThat(jit.size(), is(s.size() + 2));
        
        assertThat(jit.retainAll(s), is(true));
        assertThat(jit.size(), is(s.size()));
    }

    @Test
    public final void testRemoveAll()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        assertThat(jit.retainAll(s), is(false));
        
        jit.add("123");
        jit.add("456");       
        assertThat(jit.size(), is(s.size() + 2));
        
        for (String str : s) {
            assertThat(jit.contains(str), is(true));
        }
        
        assertThat(jit.removeAll(s), is(true));
        assertThat(jit.size(), is(2));
        
        for (String str : s) {
            assertThat(jit.contains(str), is(false));
        }
    }

    @Test
    public final void testClear()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        assertThat(jit.isEmpty(), is(false));
        jit.clear();
        assertThat(jit.isEmpty(), is(true));
    }

    @Test
    public final void testClone()
    {
        Set<String> s = createTestSet();
        JustInTimeSet<String> jit = new JustInTimeSet<String>(s);
        
        Set<String> s2 = jit.clone();
        assertThat(s2, notNullValue());
        assertThat(s2.size(), is(s.size()));
        assertThat(s2.size(), is(jit.size()));
        
        assertThat(s2.containsAll(s), is(true));
        assertThat(s.containsAll(s2), is(true));
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
