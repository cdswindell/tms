package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class ColumnTest
{
    @Test
    public void getPropertiesTest()
    {
        Column r = new Column(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Col property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Column r = new Column(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Column boolean property: " + p);
            Object value = r.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        Column r = new Column(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Col int property: " + p);
            Object value = r.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }

    @Test
    public void getInitializablePropertiesTest()
    {
        Column r = new Column(null);
        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Column initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void addColumnsTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        Column c1 = t.addColumn(Access.Next);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(1));    
        
        Column c2 = new Column(t);
        t.add(c2, Access.ByIndex, 3);
        assertThat(c2.getIndex(), is(3));
        assertThat(t.getNumColumns(), is(3));
        assertThat(t.calcIndex(ElementType.Column, Access.ByIndex, false, 3), is(2));
        
        Column c3 = new Column(t);
        t.add(c3, Access.ByIndex, 3);
        assertThat(c3.getIndex(), is(3));
        assertThat(c2.getIndex(), is(4));
        assertThat(c3.getIndex(), is(3));
        assertThat(t.getNumColumns(), is(4));       
        assertThat(t.getNumRows(), is(0));       

        Column c4 = new Column(t);
        t.add(c4, Access.ByIndex, 2);
        assertThat(c1.getIndex(), is(1));
        assertThat(c4.getIndex(), is(2));
        assertThat(c3.getIndex(), is(4));
        assertThat(c2.getIndex(), is(5));
        assertThat(t.getNumColumns(), is(5));       
        assertThat(t.getNumRows(), is(0));   
        
        Column c = t.getColumn(Access.First);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(1));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(2));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(3));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(4));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(5));
        
        c = t.getColumn(Access.Last);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(5));
        
        c = t.getColumn(Access.Next);
        assertThat(c, nullValue());
        
        Column cx = t.addColumn(Access.ByIndex, 20);
        assertThat(cx, notNullValue());
        assertThat(t.getNumColumns(), is(20));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(19));    
        
        cx = t.getColumn(Access.Current);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(19));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(18));    
        
        cx = t.getColumn(Access.Current);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(18));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(17));    
        
        cx = t.getColumn(Access.Last);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(20));    
        
        cx = t.addColumn(Access.Last);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(21));    
    }
    
    @Test 
    public void deleteColumnsTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
        
        Range r = new Range(t);
        assertThat(r, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        Column c1 = t.addColumn(Access.Next);
        assertThat(c1, notNullValue());
        r.add(c1);
        assertThat(c1.getIndex(), is(1));
        assertThat(t.getNumColumns(), is(1));    
        
        Column c2 = t.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        r.add(c2);
        assertThat(c2.getIndex(), is(2));
        assertThat(t.getNumColumns(), is(2));
             
        assertThat(r.getNumColumns(), is(2));
        assertThat(r.contains(c1), is(true));
        assertThat(r.contains(c2), is(true));
        
        c1.delete();        
        assertThat(c2, notNullValue());
        assertThat(c2.getIndex(), is(1));
        assertThat(t.getNumColumns(), is(1));
        assertThat(c1.isInUse(), is(false));
        
        assertThat(r.getNumColumns(), is(1));
        assertThat(r.contains(c1), is(false));
        assertThat(r.contains(c2), is(true));
        
        c2.delete();       
        assertThat(t.getNumColumns(), is(0));    
    }
    
    @Test
    public void columnIterableTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        Column c1 = t.addColumn(Access.ByIndex, 100);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(100));    
        
        int idx = 0;
        for (Column c : t.columnIterable()) {
        	idx++;
        	if (c != null)
        		assertThat(c.getIndex(), is(idx));
        }
        
        assertThat(idx, is(100));   	
    }
    
    @Test
    public void fillTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        Column c1 = t.addColumn(Access.ByIndex, 16);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(16)); 
        assertThat(t.getNumCells(), is(0));
        
        Row r1 = t.addRow(Access.Next);
        assertThat(t.getNumCells(), is(0));
        c1.fill(42);
        assertThat(t.getNumCells(), is(1));
        r1.fill(64);
        assertThat(t.getNumCells(), is(16));       
    }
        
}
