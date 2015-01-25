package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ReadOnlyTest
{
    @Test
    public void testReadOnly()
    {
        ContextImpl context = new ContextImpl();
        assertThat (context, notNullValue());
        assertThat(context.isReadOnly(), is(false));
        
        TableImpl t = new TableImpl(10, 10, context);        
        assertThat (t, notNullValue());
        assertThat(t.isReadOnly(), is(false));
        assertThat(t.isWriteProtected(), is(false));
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        assertThat(c1.isReadOnly(), is(false));
        assertThat(c1.isWriteProtected(), is(false));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isReadOnly(), is(false));
        assertThat(r1.isWriteProtected(), is(false));
        
        // set a value, it should sort to the first row
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isReadOnly(), is(false));
        assertThat(c.isWriteProtected(), is(false));
        
        // set context Supports Nulls to false, all items below, isWriteProtected should be false
        context.setReadOnly(true);
        assertThat(context.isReadOnly(), is(true));
        
        assertThat(t.isReadOnly(), is(false));
        assertThat(t.isWriteProtected(), is(true));
        
        assertThat(c1.isReadOnly(), is(false));
        assertThat(c1.isWriteProtected(), is(true));

        assertThat(r1.isReadOnly(), is(false));
        assertThat(r1.isWriteProtected(), is(true));
        
        assertThat(c.isReadOnly(), is(false));
        assertThat(c.isWriteProtected(), is(true));
        
        // set table Supports Nulls to false, all items below, isWriteProtected should be false
        context.setReadOnly(false);
        assertThat(context.isReadOnly(), is(false));
        
        t.setReadOnly(true);
        assertThat(t.isReadOnly(), is(true));
        assertThat(t.isWriteProtected(), is(true));
        
        assertThat(c1.isReadOnly(), is(false));
        assertThat(c1.isWriteProtected(), is(true));

        assertThat(r1.isReadOnly(), is(false));
        assertThat(r1.isWriteProtected(), is(true));
        
        assertThat(c.isReadOnly(), is(false));
        assertThat(c.isWriteProtected(), is(true));
        
        // set column Supports Nulls to false, all items below, isWriteProtected should be false for cell
        assertThat(context.isReadOnly(), is(false));
        
        t.setReadOnly(false);
        assertThat(t.isReadOnly(), is(false));
        assertThat(t.isWriteProtected(), is(false));
        
        c1.setReadOnly(true);
        assertThat(c1.isReadOnly(), is(true));
        assertThat(c1.isWriteProtected(), is(true));

        assertThat(r1.isReadOnly(), is(false));
        assertThat(r1.isWriteProtected(), is(false));
        
        assertThat(c.isReadOnly(), is(false));
        assertThat(c.isWriteProtected(), is(true));
        
        // set row Supports Nulls to false, all items below, isWriteProtected should be false for cell
        assertThat(context.isReadOnly(), is(false));
        
        t.setReadOnly(false);
        assertThat(t.isReadOnly(), is(false));
        assertThat(t.isWriteProtected(), is(false));
        
        c1.setReadOnly(false);
        assertThat(c1.isReadOnly(), is(false));
        assertThat(c1.isWriteProtected(), is(false));

        r1.setReadOnly(true);
        assertThat(r1.isReadOnly(), is(true));
        assertThat(r1.isWriteProtected(), is(true));
        
        assertThat(c.isReadOnly(), is(false));
        assertThat(c.isWriteProtected(), is(true));
        
        // set cell Supports Nulls to false, all items below, isWriteProtected should be false for cell
        assertThat(context.isReadOnly(), is(false));
        
        t.setReadOnly(false);
        assertThat(t.isReadOnly(), is(false));
        assertThat(t.isWriteProtected(), is(false));
        
        c1.setReadOnly(false);
        assertThat(c1.isReadOnly(), is(false));
        assertThat(c1.isWriteProtected(), is(false));

        r1.setReadOnly(false);
        assertThat(r1.isReadOnly(), is(false));
        assertThat(r1.isWriteProtected(), is(false));
        
        c.setReadOnly(true);
        assertThat(c.isReadOnly(), is(true));
        assertThat(c.isWriteProtected(), is(true));
    } 
}
