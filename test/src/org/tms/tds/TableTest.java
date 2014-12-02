package org.tms.tds;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.tds.Table;

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

        Set<TableProperty> props = t.getInitializableProperties();
        for (TableProperty p : props) {
            
            Object value = t.getProperty(p);
            if (value == null)
                fail("Initializable Parameter not present: " + p);
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        Set<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            t.getPropertyBoolean(p);
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        Set<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            t.getPropertyInt(p);
        }
    }
    
    @Ignore
    public void getPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);

        Set<TableProperty> props = t.getProperties();
        for (TableProperty p : props) {
            
            Object value = t.getProperty(p);
            if (!p.isOptional() && value == null)
                fail("Parameter not retrieved: " + p);
        }
    }
}
