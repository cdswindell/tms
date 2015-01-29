package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.TableProperty;

public class ContextTest
{
    @Test
    public void getPropertiesTest()
    {
        ContextImpl r = new ContextImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Context property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        ContextImpl r = new ContextImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("ContextImpl boolean property: " + p);
            Object value = r.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        ContextImpl r = new ContextImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Context int property: " + p);
            Object value = r.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }

    @Test
    public void getInitializablePropertiesTest()
    {
        ContextImpl r = new ContextImpl(null);
        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Context initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }

    @Test
    public void getTablesTest()
    {
        ContextImpl c = new ContextImpl();
        assertThat(c, notNullValue());
        
        TableImpl t1 = new TableImpl(c);
        assertThat(t1, notNullValue());
        t1.setLabel("t1");
        
        TableImpl t2 = new TableImpl(c);
        assertThat(t2, notNullValue());        
        t2.setLabel("t2");
               
        // get the labeled tables from the context
        TableImpl t = c.getTable(Access.ByLabel, "t1");
        assertThat(t, notNullValue());
        assertThat(t, is(t1));
        
        t = c.getTable(Access.ByReference, t1);
        assertThat(t, is(t1));
        
        t = c.getTable(Access.ByLabel, "t2");
        assertThat(t, is(t2));
        
        // delete a table and make sure we don't retrieve it
        t2.delete();
        t = c.getTable(Access.ByLabel, "t2");
        assertThat(t, nullValue());   
        
        t1.delete();
    }
}
