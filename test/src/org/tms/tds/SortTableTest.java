package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.api.Access;

public class SortTableTest
{
    @Test
    public void testSortSparseTable()
    {
        TableImpl t = new TableImpl();        
        assertThat (t, notNullValue());
        
        ColumnImpl c1 = t.addColumn();
        assertThat(c1, notNullValue());
        assertThat(c1.getIndex(), is(1));
        
        RowImpl r10 = t.addRow(Access.ByIndex, 10);
        assertThat(r10, notNullValue());
        assertThat(r10.getIndex(), is(10));
        
        RowImpl r5000 = t.addRow(Access.ByIndex, 5000);
        assertThat(r5000, notNullValue());
        assertThat(r5000.getIndex(), is(5000));
        assertThat(r10.getIndex(), is(10));
        
        // sort the column, should be no difference in row indexes
        // as null cells and null rows are equivalent
        c1.sort();
        assertThat(r5000.getIndex(), is(5000));
        assertThat(r10.getIndex(), is(10));
        
        // set a value, it should sort to the first row
        CellImpl cR5000C1 = t.getCell(r5000, c1);
        assertThat(cR5000C1, notNullValue());
        cR5000C1.setCellValue(1);
        
        c1.sort();
        assertThat(r5000.getIndex(), is(1)); // r5000 should be the first
        assertThat(r10.getIndex(), is(11));  // r10 should be moved down 1 row
        
        // set a 2nd value, it should sort to the first row
        CellImpl cR10C1 = t.getCell(r10, c1);
        assertThat(cR10C1, notNullValue());
        cR10C1.setCellValue(0);
        
        c1.sort();
        assertThat(r5000.getIndex(), is(2)); // r5000 should be the 2nd
        assertThat(r10.getIndex(), is(1));  // r10 should be the 1st
        
        // add a new column, sort row, and verify original column is back to idx 1
        ColumnImpl c2 = t.addColumn(Access.First);
        assertThat(c2, notNullValue());
        assertThat(c1.getIndex(), is(2));
        assertThat(c2.getIndex(), is(1));
        
        // resort
        r5000.sort();
        assertThat(c1.getIndex(), is(1));
        assertThat(c2.getIndex(), is(2));
        
        // add a text cell to c1
        RowImpl r1 = t.addRow(Access.First);
        assertThat(r1, notNullValue());
        assertThat(r1.getIndex(), is(1));
        assertThat(r10.getIndex(), is(2));
        assertThat(r5000.getIndex(), is(3));
        
        CellImpl cR1C1 = t.getCell(r1, c1);
        assertThat(cR1C1, notNullValue());
        
        cR1C1.setCellValue("abc");
        c1.sort();
        assertThat(r1.getIndex(), is(3));
        assertThat(r10.getIndex(), is(1));
        assertThat(r5000.getIndex(), is(2));      
    } 
}
