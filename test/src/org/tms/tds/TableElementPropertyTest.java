package org.tms.tds;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.tds.Table;

public class TableElementPropertyTest
{

    @Test
    public void testTableElementPropertyGetSetHasClear()
    {
        Table t = new Table();
        
        assert (t != null);
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
}
