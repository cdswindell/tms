package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.api.exceptions.UnimplementedException;

public class TableTest
{

    @Test
    public void testTableBasicProperties()
    {
        Table t = new Table(7, 10);        
        assert (t != null);
        
        assertThat(t.getNumAllocRows(), is(Context.sf_ROW_ALLOC_INCR_DEFAULT));
        
        assertThat(t.hasProperty(TableProperty.Label), is(false));
        t.setLabel("abcdef");
        assertThat(t.hasProperty(TableProperty.Label), is(true));
        
        String l = t.getLabel();       
        assertThat(l, is("abcdef"));
        
        t.clearProperty(TableProperty.Label);
        assertThat(t.hasProperty(TableProperty.Label), is(false));
        
        assertThat(t.hasProperty("abc"), is(false));
        t.setProperty("abc", "ABC");
        assertThat(t.hasProperty("abc"), is(true));
        
        t.clearProperty("abc");
        assertThat(t.hasProperty("abc"), is(false));
        
        // expect failure
        try {
            t.setProperty(BaseElement.sf_RESERVED_PROPERTY_PREFIX + "Label", "abc");
            fail("set invalid property");
        }
        catch (TableException te)
        {
            // noop
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
        // expect failure
        try {
            t.setProperty(TableProperty.Row, "abc");
            fail("set unimplemented property");
        }
        catch (UnimplementedException te)
        {
            assertThat(te.getTableErrorClass(), is(TableErrorClass.Unimplemented));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        
        // expect failure
        try {
            t.setProperty(TableProperty.Rows, "abc");
            fail("set read only property");
        }
        catch (ReadOnlyException te)
        {
            assertThat(te.getTableErrorClass(), is(TableErrorClass.ReadOnly));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void getInitializablePropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);

        List<TableProperty> props = t.getInitializableProperties();
        for (TableProperty p : props) {         
            System.out.print("Table initializable property: " + p);
            Object value = t.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            System.out.print("Table boolean property: " + p);
            Object value = t.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            System.out.print("Table int property: " + p);
            Object value = t.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);

        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) {          
            System.out.print("Table property: " + p);
            Object value = t.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void basicRowAccessTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);

        // test the various access modes in "get" (not add) more
        assertThat(t.getRowIndex(Access.First), is(-1));
        assertThat(t.getRowIndex(Access.Last), is(-1));
        assertThat(t.getRowIndex(Access.Previous), is(-1));
        assertThat(t.getRowIndex(Access.Current), is(-1));
        assertThat(t.getRowIndex(Access.Next), is(-1));
        
        // now test in "add" mode
        assertThat(t.getRowIndex(Access.First, true), is(0));
        assertThat(t.getRowIndex(Access.Last, true), is(0));
        assertThat(t.getRowIndex(Access.Previous, true), is(0));
        assertThat(t.getRowIndex(Access.Current, true), is(0));
        assertThat(t.getRowIndex(Access.Next, true), is(0));
    }
}
