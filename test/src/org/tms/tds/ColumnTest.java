package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.*;
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
        
        Column c = new Column(t);
        
        t.add(c, Access.ByIndex, 6);
        assertThat(c.getIndex(), is(6));
        assertThat(t.getNumColumns(), is(6));
        assertThat(t.calcIndex(ElementType.Column, Access.ByIndex, false, 6), is(5));
        
    }
}
