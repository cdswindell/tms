package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidException;
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
        
        assertThat(t.getRowsCapacity(), is(Context.sf_ROW_CAPACITY_INCR_DEFAULT));
        
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

        try {
            t.calcIndex(null, Access.First);
            fail("calcIndex sucessfully called with null ElementType");
        }
        catch (UnimplementedException e) 
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.Unimplemented));
        }
        
        try {
            t.calcIndex(ElementType.Cell, Access.First);
            fail("calcIndex sucessfully called with Cell ElementType");
        }
        catch (UnimplementedException e) 
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.Unimplemented));
            assertThat(e.getTMSElementType(), is(ElementType.Cell));
        }
        
        try {
            t.calcIndex(ElementType.Row, Access.ByIndex, true, "abc");
            fail("calcIndex sucessfully called with invalid ByIndex metadata");
        }
        catch (InvalidException e) 
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.Invalid));
            assertThat(e.getTMSElementType(), is(ElementType.Table));
        }
        
        // test the various access modes in "get" (not add) more
        for (ElementType et : new ElementType []{ElementType.Row, ElementType.Column}) {
            assertThat(t.calcIndex(et, Access.First), is(-1));
            assertThat(t.calcIndex(et, Access.Last), is(-1));
            assertThat(t.calcIndex(et, Access.Previous), is(-1));
            assertThat(t.calcIndex(et, Access.Current), is(-1));
            assertThat(t.calcIndex(et, Access.Next), is(-1));
            
            assertThat(t.calcIndex(et, Access.ByIndex), is(-1));
            assertThat(t.calcIndex(et, Access.ByIndex, false, -1), is(-1));
            assertThat(t.calcIndex(et, Access.ByIndex, false, 1), is(-1));
            
            // now test in "add" mode
            assertThat(t.calcIndex(et, Access.First, true), is(0));
            assertThat(t.calcIndex(et, Access.Last, true), is(0));
            assertThat(t.calcIndex(et, Access.Previous, true), is(0));
            assertThat(t.calcIndex(et, Access.Current, true), is(0));
            assertThat(t.calcIndex(et, Access.Next, true), is(0));
    
            assertThat(t.calcIndex(et, Access.ByIndex, true, -1), is(-1));
            assertThat(t.calcIndex(et, Access.ByIndex, true, 1), is(0));        
            assertThat(t.calcIndex(et, Access.ByIndex, true, 11), is(10));
        }
    }
}
