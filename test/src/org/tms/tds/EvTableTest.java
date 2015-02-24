package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.api.TableProperty;
import org.tms.api.event.TableElementEventType;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.api.exceptions.UnimplementedException;

public class EvTableTest
{
    @Test
    public void testEvTableBasicProperties()
    {
        EvTableImpl t = new EvTableImpl();        
        assert (t != null);
        
        t.addListeners(TableElementEventType.OnNewValue, e -> System.out.println(String.format("%s %s", e.getTable().getClass(), e)));
        
        assertThat(t.getRowsCapacity(), is(ContextImpl.sf_ROW_CAPACITY_INCR_DEFAULT));       
        
        t.setCellValue(t.addRow(), t.addColumn(), "Hello World");
        
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
            t.setProperty(BaseElementImpl.sf_RESERVED_PROPERTY_PREFIX + "Label", "abc");
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
        TableImpl t = new EvTableImpl();        
        assert (t != null);

        List<TableProperty> props = t.getInitializableProperties();
        for (TableProperty p : props) {         
            System.out.print("EvTable initializable property: " + p);
            Object value = t.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        TableImpl t = new EvTableImpl();        
        assert (t != null);
        
        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            System.out.print("EvTable boolean property: " + p);
            Object value = t.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        TableImpl t = new EvTableImpl();        
        assert (t != null);
        
        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            System.out.print("EvTable int property: " + p);
            Object value = t.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getPropertiesTest()
    {
        TableImpl t = new EvTableImpl();        
        assert (t != null);

        List<TableProperty> props = t.getProperties();
        for (TableProperty p : props) {          
            System.out.print("EvTable property: " + p);
            Object value = t.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
}
