package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.tms.api.TableProperty;

public class RangeTest
{

    @Test
    public void getPropertiesTest()
    {
        Range r = new Range(null);

        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Range property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getInitializablePropertiesTest()
    {
        Range r = new Range(null);

        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Range initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void createRangeTest() throws InterruptedException
    {
        Table t = new Table(10, 10);
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(0));
        
        Range r = new Range(t);
        assert (r != null);
        
        int numRows = r.getPropertyInt(TableProperty.numRows);
        assertThat(numRows, is(0));
        assertThat(r.getTable(), is(t));
        assertThat(r.getContext(), is(t.getContext()));
        
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(1));
        
        t.remove(r);
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(0));
        
        // test weak reference ability
        t.add(r);
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(1));
        
        r = null;
        System.gc();
        
        Thread.sleep(1000);
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(0));
   }
    
    @Test
    public void rowsTest()
    {
        Table t = new Table(100, 100);
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numRanges), is(0));
        
        Range r = new Range(t);
        assert (r != null);
        
        Row r1 = new Row(t);
        Row r2 = new Row(t);
        Row r3 = new Row(t);
        
        Set<Row> rs = new HashSet<Row>();
        rs.add(r1);
        rs.add(r2);
        rs.add(r3);
        
        assertThat(r.addAll(rs), is(true));
        assertThat(r.getNumRows(), is(3));
        assertThat(r.contains(r1), is(true));
        assertThat(r.contains(r2), is(true));
        assertThat(r.contains(r3), is(true));
        assertThat(r.containsAll(rs), is(true));
        
        r.remove(r1, r2, r3);
        assertThat(r.getNumRows(), is(0));
        
        assertThat(r.removeAll(rs), is(false));
    }
}
