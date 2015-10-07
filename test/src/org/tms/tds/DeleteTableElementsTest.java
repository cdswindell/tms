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
        
        SubsetImpl rng = new SubsetImpl(t);
        assertThat(rng, notNullValue());
        
        rng.add(r1);
        assertThat(rng.getNumRowsInternal(), is(1));
        assertThat(rng.getRows(), notNullValue());
        assertThat(rng.getRows().size(), is(1));
        
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
        
        // assert that subset contains no rows
        assertThat(rng.getNumRowsInternal(), is(0));
        assertThat(rng.getNumCells(), is(0));
        assertThat(rng.getRows(), notNullValue());
        assertThat(rng.getRows().size(), is(0));
        
        // verify that no operations can be performed
        try {
            r1.getNumCells();
            fail("getNumCells succeeded");
        }
        catch (DeletedElementException de) {
            assertThat(de.getTableErrorClass(), is(TableErrorClass.Deleted));
        }
        
        // try adding the deleted row back to the subset, it should fail
        try {
            rng.add(r1);
            fail("subset.add succeeded");
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
        
        RowImpl r3 = t.addRow();
        assertThat(r3, notNullValue());
        assertThat(r3.getIndex(), is(3));
        assertThat(r3.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(3));
               
        // set a value, it should become invalid when the row is deleted
        CellImpl cell1 = t.getCell(r1, c1);
        cell1.setLabel("cell1");
        cell1.setDerivation("mean(row 2)");
        assertThat(cell1, notNullValue());
        assertThat(cell1.getRow(), is(r1));
        assertThat(cell1.getColumn(), is(c1));
        assertThat(cell1.isInvalid(), is(false));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell2 = t.getCell(r2, c1);
        cell2.setLabel("cell2");
        cell2.setDerivation("count(row 1)");
        assertThat(cell2, notNullValue());
        assertThat(cell2.getRow(), is(r2));
        assertThat(cell2.getColumn(), is(c1));
        assertThat(cell2.isInvalid(), is(false));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell3 = t.getCell(r3, c1);
        cell3.setDerivation("cell1 + cell2");
        assertThat(cell3, notNullValue());
        assertThat(cell3.getRow(), is(r3));
        assertThat(cell3.getColumn(), is(c1));
        assertThat(cell3.isInvalid(), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(2));
        assertThat(t.getNumDerivedCells(), is(3));
        
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
        assertThat(t.getCellDerivation(cell1), nullValue());
        
        assertThat(r2.getAffects().size(), is(0));
        assertThat(r2.getAffects().contains(r1), is(false));
        assertThat(r2.getAffects().contains(cell1), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(0));
        assertThat(t.getNumDerivedCells(), is(0));
        
        assertThat(cell3.isValid(), is(true));
        assertThat(cell3.isDerived(), is(false));
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
    
    @Test
    public void testDeleteColumnsBasic()
    {
        TableImpl t = new TableImpl(10, 10);        
        assertThat (t, notNullValue());
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        assertThat(c1.isInvalid(), is(false));        
        assertThat(t.getNumColumns(), is(1));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(1));
        
        SubsetImpl rng = new SubsetImpl(t);
        assertThat(rng, notNullValue());
        
        rng.add(c1);
        assertThat(rng.getNumRowsInternal(), is(0));
        assertThat(rng.getNumColumnsInternal(), is(1));
        assertThat(rng.getColumns(), notNullValue());
        assertThat(rng.getColumns().size(), is(1));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isInvalid(), is(false));
        
        rng.add(c);
        
        // delete the row
        c1.delete();
        assertThat(t.getNumRows(), is(1));
        assertThat(t.getNumColumns(), is(0));
        assertThat(r1.isInvalid(), is(false));        
        assertThat(c1.isInvalid(), is(true));        
        assertThat(c.isInvalid(), is(true));  
        
        // assert that subset contains no rows
        assertThat(rng.getNumColumnsInternal(), is(0));
        assertThat(rng.getNumCells(), is(0));
        assertThat(rng.getColumns(), notNullValue());
        assertThat(rng.getColumns().size(), is(0));
        
        assertThat(rng.contains(c1), is(false));
        assertThat(rng.contains(c), is(false));
        
        // verify that no operations can be performed
        try {
            c1.getNumCells();
            fail("getNumCells succeeded");
        }
        catch (DeletedElementException de) {
            assertThat(de.getTableErrorClass(), is(TableErrorClass.Deleted));
        }
        
        // try adding the deleted col back to the subset, it should fail
        try {
            rng.add(c1);
            fail("subset.add succeeded");
        }
        catch (DeletedElementException de) {
            assertThat(de.getTableErrorClass(), is(TableErrorClass.Deleted));
        }
        
        // try deletion call again, should not fail
        c1.delete();
        assertThat(c1.isInvalid(), is(true));                
    } 
    
    @Test 
    public void testDeleteDerivedColumn()
    {
        TableImpl t = new TableImpl(10, 10);        
        assertThat (t, notNullValue());
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        
        ColumnImpl c2 = t.addColumn();
        assertThat(c2, notNullValue());
        assertThat(c2.getIndex(), is(2));
        assertThat(c2.isInvalid(), is(false));        
        assertThat(t.getNumColumns(), is(2));
        
        ColumnImpl c3 = t.addColumn();
        assertThat(c3, notNullValue());
        assertThat(c3.getIndex(), is(3));
        assertThat(c3.isInvalid(), is(false));        
        assertThat(t.getNumColumns(), is(3));
               
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(1));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell1 = t.getCell(r1, c1);
        cell1.setLabel("cell1");
        cell1.setDerivation("mean(col 2)");
        assertThat(cell1, notNullValue());
        assertThat(cell1.getRow(), is(r1));
        assertThat(cell1.getColumn(), is(c1));
        assertThat(cell1.isInvalid(), is(false));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell2 = t.getCell(r1, c2);
        cell2.setLabel("cell2");
        cell2.setDerivation("count(row 1)");
        assertThat(cell2, notNullValue());
        assertThat(cell2.getRow(), is(r1));
        assertThat(cell2.getColumn(), is(c2));
        assertThat(cell2.isInvalid(), is(false));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl cell3 = t.getCell(r1, c3);
        cell3.setDerivation("cell1 + cell2");
        assertThat(cell3, notNullValue());
        assertThat(cell3.getRow(), is(r1));
        assertThat(cell3.getColumn(), is(c3));
        assertThat(cell3.isInvalid(), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(2));
        assertThat(t.getNumDerivedCells(), is(3));
        
        // fill row 2 and set a derivation on row 1
        c2.fill(25);
        c1.setDerivation("randInt(col 2)");
        
        assertThat(c2.getAffects().size(), is(2));
        assertThat(c2.getAffects().contains(c1), is(true));
        
        // delete c1, vet that r2.getAffects is updated accordingly
        c1.delete();
        
        assertThat(c1.isValid(), is(false));
        assertThat(c1.isInvalid(), is(true));
        assertThat(c2.isValid(), is(true));
        assertThat(c2.isInvalid(), is(false));
        
        assertThat(cell1.isInvalid(), is(true));
        assertThat(t.getCellDerivation(cell1), nullValue());
        
        assertThat(c2.getAffects().size(), is(0));
        assertThat(c2.getAffects().contains(c1), is(false));
        assertThat(c2.getAffects().contains(cell1), is(false));
        
        assertThat(t.getNumDerivedCellsAffects(), is(0));
        assertThat(t.getNumDerivedCells(), is(1));
        
        assertThat(cell3.isValid(), is(true));
        assertThat(cell3.isDerived(), is(false));
    }
    
    @Test 
    public void testDeleteAffectsColumn()
    {
        TableImpl t = new TableImpl(10, 10);        
        assertThat (t, notNullValue());
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        
        ColumnImpl c2 = t.addColumn();
        assertThat(c2, notNullValue());
        assertThat(c2.getIndex(), is(2));
        assertThat(c2.isInvalid(), is(false));        
        assertThat(t.getNumColumns(), is(2));
        
        RowImpl r1 = t.addRow();
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r1.isInvalid(), is(false));        
        assertThat(t.getNumRows(), is(1));
        
        // set a value, it should become invalid when the row is deleted
        CellImpl c = t.getCell(r1, c1);
        assertThat(c, notNullValue());
        assertThat(c.getRow(), is(r1));
        assertThat(c.getColumn(), is(c1));
        assertThat(c.isInvalid(), is(false));
        
        // fill row 2 and set a derivation on row 1
        c2.fill(25);
        c1.setDerivation("randInt(col 2)");
        
        assertThat(c2.getAffects().size(), is(1));
        assertThat(c2.getAffects().contains(c1), is(true));
        
        // delete r2, vet that r1 derivation is cleared
        c2.delete();
        
        assertThat(c2.isValid(), is(false));
        assertThat(c2.isInvalid(), is(true));
        assertThat(r1.isValid(), is(true));
        assertThat(r1.isInvalid(), is(false));
        
        assertThat(r1.getDerivation(), nullValue());
    }
}
