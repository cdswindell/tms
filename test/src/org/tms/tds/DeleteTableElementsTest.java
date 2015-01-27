package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.exceptions.DeletedElementException;
import org.tms.api.exceptions.TableErrorClass;

public class DeleteTableElementsTest
{
    @Test
    public void testDeleteRowsNull()
    {
        TableImpl t = new TableImpl(10, 10);        
        assertThat (t, notNullValue());
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(1));
        
        RangeImpl rng = new RangeImpl(t);
        assertThat(rng, notNullValue());
        
        rng.add(r1);
        assertThat(rng.getNumRows(), is(1));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isInvalid(), is(false));
        
        rng.add(c);
        
        // delete the row
        r1.delete();
        assertThat(t.getNumRows(), is(0));
        assertThat(r1.isInvalid(), is(true));        
        assertThat(c.isInvalid(), is(true));  
        
        // assert that range contains no rows
        assertThat(rng.getNumRows(), is(0));
        
        // verify that no operations can be performed
        try {
            r1.getNumCells();
            fail("getNumCells succeeded");
        }
        catch (DeletedElementException de) {
            assertThat(de.getTableErrorClass(), is(TableErrorClass.Deleted));
        }
        
        // try adding the deleted row back to the range, it should fail
        try {
            rng.add(r1);
            fail("range.add succeeded");
        }
        catch (DeletedElementException de) {
            assertThat(de.getTableErrorClass(), is(TableErrorClass.Deleted));
        }
        
        // try deletion call again, should not fail
        r1.delete();
        assertThat(r1.isInvalid(), is(true));                
    } 
}
