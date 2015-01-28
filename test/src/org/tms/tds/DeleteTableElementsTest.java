package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.exceptions.DeletedElementException;
import org.tms.api.exceptions.TableErrorClass;

public class DeleteTableElementsTest
{
    @Test
    public void testDeleteRowsBasic()
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
        assertThat(rng.getNumCells(), is(0));
        
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
    
    @Test 
    public void testDeleteDerivedRow()
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
        
        RowImpl r2 = t.addRow();
        assertThat(r2, notNullValue());
        assertThat(r2.getIndex(), is(2));
        assertThat(r2.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(2));
               
        // set a value, it should become invalid when the row is deleted
        CellImpl cell1 = t.getCell(r1, c1);
        cell1.setDerivation("mean(row 2)");
        assertThat(cell1, notNullValue());
        assertThat(cell1.getRow(), is(r1));
        assertThat(cell1.getColumn(), is(c1));
        assertThat(cell1.isInvalid(), is(false));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell2 = t.getCell(r2, c1);
        cell2.setDerivation("count(row 1)");
        assertThat(cell2, notNullValue());
        assertThat(cell2.getRow(), is(r2));
        assertThat(cell2.getColumn(), is(c1));
        assertThat(cell2.isInvalid(), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(0));
        assertThat(t.getNumDerivedCells(), is(2));
        
        // fill row 2 and set a derivation on row 1
        r2.fill(25);
        r1.setDerivation("randInt(row 2)");
        
        assertThat(r2.getAffects().size(), is(2));
        assertThat(r2.getAffects().contains(r1), is(true));
        
        // delete r1, vet that r2.getAffects is updated accordingly
        r1.delete();
        
        assertThat(r1.isValid(), is(false));
        assertThat(r1.isInvalid(), is(true));
        assertThat(r2.isValid(), is(true));
        assertThat(r2.isInvalid(), is(false));
        
        assertThat(cell1.isInvalid(), is(true));
        assertThat(cell1.isDerived(), is(false));
        assertThat(t.getCellDerivation(cell1), nullValue());
        
        assertThat(r2.getAffects().size(), is(0));
        assertThat(r2.getAffects().contains(r1), is(false));
        assertThat(r2.getAffects().contains(cell1), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(0));
        assertThat(t.getNumDerivedCells(), is(0));
    }
    
    @Test 
    public void testDeleteAffectsRow()
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
        
        RowImpl r2 = t.addRow();
        assertThat(r2, notNullValue());
        assertThat(r2.getIndex(), is(2));
        assertThat(r2.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(2));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isInvalid(), is(false));
        
        // fill row 2 and set a derivation on row 1
        r2.fill(25);
        r1.setDerivation("randInt(row 2)");
        
        assertThat(r2.getAffects().size(), is(1));
        assertThat(r2.getAffects().contains(r1), is(true));
        
        // delete r2, vet that r1 derivation is cleared
        r2.delete();
        
        assertThat(r2.isValid(), is(false));
        assertThat(r2.isInvalid(), is(true));
        assertThat(r1.isValid(), is(true));
        assertThat(r1.isInvalid(), is(false));
        
        assertThat(r1.getDerivation(), nullValue());
    }
}
