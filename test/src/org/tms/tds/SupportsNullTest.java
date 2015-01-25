package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SupportsNullTest
{
    @Test
    public void testSupportsNull()
    {
        ContextImpl context = new ContextImpl();
        assertThat (context, notNullValue());
        assertThat(context.isSupportsNull(), is(true));
        
        TableImpl t = new TableImpl(10, 10, context);        
        assertThat (t, notNullValue());
        assertThat(t.isSupportsNull(), is(true));
        assertThat(t.isNullsSupported(), is(true));
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        assertThat(c1.isSupportsNull(), is(true));
        assertThat(c1.isNullsSupported(), is(true));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isSupportsNull(), is(true));
        assertThat(r1.isNullsSupported(), is(true));
        
        // set a value, it should sort to the first row
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isSupportsNull(), is(true));
        assertThat(c.isNullsSupported(), is(true));
        
        // set context Supports Nulls to false, all items below, isNullsSupported should be false
        context.setSupportsNull(false);
        assertThat(context.isSupportsNull(), is(false));
        
        assertThat(t.isSupportsNull(), is(true));
        assertThat(t.isNullsSupported(), is(false));
        
        assertThat(c1.isSupportsNull(), is(true));
        assertThat(c1.isNullsSupported(), is(false));

        assertThat(r1.isSupportsNull(), is(true));
        assertThat(r1.isNullsSupported(), is(false));
        
        assertThat(c.isSupportsNull(), is(true));
        assertThat(c.isNullsSupported(), is(false));
        
        // set table Supports Nulls to false, all items below, isNullsSupported should be false
        context.setSupportsNull(true);
        assertThat(context.isSupportsNull(), is(true));
        
        t.setSupportsNull(false);
        assertThat(t.isSupportsNull(), is(false));
        assertThat(t.isNullsSupported(), is(false));
        
        assertThat(c1.isSupportsNull(), is(true));
        assertThat(c1.isNullsSupported(), is(false));

        assertThat(r1.isSupportsNull(), is(true));
        assertThat(r1.isNullsSupported(), is(false));
        
        assertThat(c.isSupportsNull(), is(true));
        assertThat(c.isNullsSupported(), is(false));
        
        // set column Supports Nulls to false, all items below, isNullsSupported should be false for cell
        assertThat(context.isSupportsNull(), is(true));
        
        t.setSupportsNull(true);
        assertThat(t.isSupportsNull(), is(true));
        assertThat(t.isNullsSupported(), is(true));
        
        c1.setSupportsNull(false);
        assertThat(c1.isSupportsNull(), is(false));
        assertThat(c1.isNullsSupported(), is(false));

        assertThat(r1.isSupportsNull(), is(true));
        assertThat(r1.isNullsSupported(), is(true));
        
        assertThat(c.isSupportsNull(), is(true));
        assertThat(c.isNullsSupported(), is(false));
        
        // set row Supports Nulls to false, all items below, isNullsSupported should be false for cell
        assertThat(context.isSupportsNull(), is(true));
        
        t.setSupportsNull(true);
        assertThat(t.isSupportsNull(), is(true));
        assertThat(t.isNullsSupported(), is(true));
        
        c1.setSupportsNull(true);
        assertThat(c1.isSupportsNull(), is(true));
        assertThat(c1.isNullsSupported(), is(true));

        r1.setSupportsNull(false);
        assertThat(r1.isSupportsNull(), is(false));
        assertThat(r1.isNullsSupported(), is(false));
        
        assertThat(c.isSupportsNull(), is(true));
        assertThat(c.isNullsSupported(), is(false));
        
        // set cell Supports Nulls to false, all items below, isNullsSupported should be false for cell
        assertThat(context.isSupportsNull(), is(true));
        
        t.setSupportsNull(true);
        assertThat(t.isSupportsNull(), is(true));
        assertThat(t.isNullsSupported(), is(true));
        
        c1.setSupportsNull(true);
        assertThat(c1.isSupportsNull(), is(true));
        assertThat(c1.isNullsSupported(), is(true));

        r1.setSupportsNull(true);
        assertThat(r1.isSupportsNull(), is(true));
        assertThat(r1.isNullsSupported(), is(true));
        
        c.setSupportsNull(false);
        assertThat(c.isSupportsNull(), is(false));
        assertThat(c.isNullsSupported(), is(false));
    } 
}
