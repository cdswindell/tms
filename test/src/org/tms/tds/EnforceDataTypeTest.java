package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EnforceDataTypeTest
{
    @Test
    public void testEnforceDataType()
    {
        ContextImpl context = new ContextImpl();
        assertThat (context, notNullValue());
        assertThat(context.isEnforceDataType(), is(false));
        
        TableImpl t = new TableImpl(10, 10, context);        
        assertThat (t, notNullValue());
        assertThat(t.isEnforceDataType(), is(false));
        assertThat(t.isDataTypeEnforced(), is(false));
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        assertThat(c1.isEnforceDataType(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isEnforceDataType(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        
        // set a value, it should sort to the first row
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isEnforceDataType(), is(false));
        assertThat(c.isDataTypeEnforced(), is(false));
        
        // set context Supports Nulls to false, all items below, isDataTypeEnforced should be false
        context.setEnforceDataType(true);
        assertThat(context.isEnforceDataType(), is(true));
        
        assertThat(t.isEnforceDataType(), is(false));
        assertThat(t.isDataTypeEnforced(), is(true));
        
        assertThat(c1.isEnforceDataType(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(true));

        assertThat(r1.isEnforceDataType(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(true));
        
        assertThat(c.isEnforceDataType(), is(false));
        assertThat(c.isDataTypeEnforced(), is(true));
        
        // set table Supports Nulls to false, all items below, isDataTypeEnforced should be false
        context.setEnforceDataType(false);
        assertThat(context.isEnforceDataType(), is(false));
        
        t.setEnforceDataType(true);
        assertThat(t.isEnforceDataType(), is(true));
        assertThat(t.isDataTypeEnforced(), is(true));
        
        assertThat(c1.isEnforceDataType(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(true));

        assertThat(r1.isEnforceDataType(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(true));
        
        assertThat(c.isEnforceDataType(), is(false));
        assertThat(c.isDataTypeEnforced(), is(true));
        
        // set column Supports Nulls to false, all items below, isDataTypeEnforced should be false for cell
        assertThat(context.isEnforceDataType(), is(false));
        
        t.setEnforceDataType(false);
        assertThat(t.isEnforceDataType(), is(false));
        assertThat(t.isDataTypeEnforced(), is(false));
        
        c1.setEnforceDataType(true);
        assertThat(c1.isEnforceDataType(), is(true));
        assertThat(c1.isDataTypeEnforced(), is(true));

        assertThat(r1.isEnforceDataType(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        
        assertThat(c.isEnforceDataType(), is(false));
        assertThat(c.isDataTypeEnforced(), is(true));
        
        // set row Supports Nulls to false, all items below, isDataTypeEnforced should be false for cell
        assertThat(context.isEnforceDataType(), is(false));
        
        t.setEnforceDataType(false);
        assertThat(t.isEnforceDataType(), is(false));
        assertThat(t.isDataTypeEnforced(), is(false));
        
        c1.setEnforceDataType(false);
        assertThat(c1.isEnforceDataType(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));

        r1.setEnforceDataType(true);
        assertThat(r1.isEnforceDataType(), is(true));
        assertThat(r1.isDataTypeEnforced(), is(true));
        
        assertThat(c.isEnforceDataType(), is(false));
        assertThat(c.isDataTypeEnforced(), is(true));
        
        // set cell Supports Nulls to false, all items below, isDataTypeEnforced should be false for cell
        assertThat(context.isEnforceDataType(), is(false));
        
        t.setEnforceDataType(false);
        assertThat(t.isEnforceDataType(), is(false));
        assertThat(t.isDataTypeEnforced(), is(false));
        
        c1.setEnforceDataType(false);
        assertThat(c1.isEnforceDataType(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));

        r1.setEnforceDataType(false);
        assertThat(r1.isEnforceDataType(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        
        c.setEnforceDataType(true);
        assertThat(c.isEnforceDataType(), is(true));
        assertThat(c.isDataTypeEnforced(), is(true));
    } 
}
