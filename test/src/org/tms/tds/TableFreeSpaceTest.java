package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;

public class TableFreeSpaceTest extends BaseTest
{
    @Test
    public void testColumnFreeSpaceReclamation()
    {
        ContextImpl c = (ContextImpl)TableContextFactory.createTableContext();
        c.setColumnCapacityIncr(2);
        
        Table t1 = TableFactory.createTable(2, 1, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(2));
        
        Column c2 = t1.addColumn(Access.ByIndex, 2);
        assertThat(c2, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(2));
        
        Column c21 = t1.addColumn(Access.ByIndex, 21);
        assertThat(c21, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(22));
        
        // delete the first 10 columns and retest
        for (int i = 0; i < 10; i++)
            t1.getColumn(Access.First).delete();
        
        assertThat(t1.getNumColumns(), is(11));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(12));
        
        // delete all of the columns, at this point, the capacity should be the incr, which is 2
        Column col = null;
        while ((col = t1.getColumn(Access.First)) != null)
            col.delete();
        
        assertThat(t1.getNumColumns(), is(0));
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(2));
    }
    
    @Test
    public void testColumnFreeSpaceReclamationViaTable()
    {
        ContextImpl c = (ContextImpl)TableContextFactory.createTableContext();
        c.setColumnCapacityIncr(2);
        
        Table t1 = TableFactory.createTable(2, 1, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(2));
        
        Column c21 = t1.addColumn(Access.ByIndex, 21);
        assertThat(c21, notNullValue());
        assertThat(t1.getNumColumns(), is(21));
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(22));
        
        // put the first 18 columns in a set and delete them enmass
        List<Column> cols = new ArrayList<Column>(18);
        for (int i = 0; i < 18; i++)
            cols.add(t1.getColumn(Access.ByIndex, i+1));
        
        // delete them via table
        t1.delete(cols.toArray(new Column [] {}));
        
        assertThat(t1.getNumColumns(), is(3));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(3));
        
        // delete all of the columns, at this point, the capacity should be the incr, which is 2
        Column col = null;
        while ((col = t1.getColumn(Access.First)) != null)
            col.delete();
        
        assertThat(t1.getNumColumns(), is(0));
        assertThat(t1.getPropertyInt(TableProperty.ColumnCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(2));
    } 
    
    @Test
    public void testRowFreeSpaceReclamation()
    {
        ContextImpl c = (ContextImpl)TableContextFactory.createTableContext();
        c.setRowCapacityIncr(2);
        
        Table t1 = TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        Row r2 = t1.addRow(Access.ByIndex, 2);
        assertThat(r2, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        Row r21 = t1.addRow(Access.ByIndex, 21);
        assertThat(r21, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(22));
        
        // delete the first 10 rows and retest
        for (int i = 0; i < 10; i++)
            t1.getRow(Access.First).delete();
        
        assertThat(t1.getNumRows(), is(11));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(14));
        
        // delete all of the rows, at this point, the capacity should be the incr, which is 2
        Row row = null;
        while ((row = t1.getRow(Access.First)) != null)
            row.delete();
        
        assertThat(t1.getNumRows(), is(0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
    }
    
    @Test
    public void testCellFreeSpaceReclamationSparse()
    {
        ContextImpl c = (ContextImpl)TableContextFactory.createTableContext();
        c.setRowCapacityIncr(2);
        
        Table t1 = TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        Row r2 = t1.addRow(Access.ByIndex, 2);
        assertThat(r2, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        Row r21 = t1.addRow(Access.ByIndex, 21);
        assertThat(r21, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(22));
        
        Column c1 = t1.addColumn();
        
        t1.setCellValue(t1.getRow(Access.ByIndex, 17), c1, "C");
        t1.setCellValue(t1.getRow(Access.ByIndex, 16), c1, "B");
        t1.setCellValue(t1.getRow(Access.ByIndex, 15), c1, "A");
        
        // delete the first 10 rows and retest
        for (int i = 0; i < 10; i++)
            t1.getRow(Access.First).delete();
        
        assertThat(t1.getNumRows(), is(11));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(14));
        
        // delete all of the rows, at this point, the capacity should be the incr, which is 2
        Row row = null;
        while ((row = t1.getRow(Access.First)) != null)
            row.delete();
        
        assertThat(t1.getNumRows(), is(0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
    }
    
    @Test
    public void testCellFreeSpaceReclamation()
    {
        ContextImpl c = (ContextImpl)TableContextFactory.createTableContext();
        c.setRowCapacityIncr(2);
        
        TableImpl t1 = (TableImpl)TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        ColumnImpl c1 = (ColumnImpl)t1.addColumn();
        
        // initialize column with alphabet, in reverse order
        t1.addRow(Access.ByIndex, 26);
        char value = 'Z';
        for (int i = 0; i < 26; i++) 
            t1.setCellValue(t1.getRow(Access.ByIndex, 26 - i), c1, value--);
        
        assertThat(t1.getNextCellOffset(), is(26));
        assertThat(t1.getNumRows(), is(26));
        
        // delete first row, make sure other cells are as expected
        Row r = null;
        int iter = 0;
        while ((r = t1.getRow(Access.First)) != null) {
            r.delete();
            
            int offset =  ++iter;
            for (int i = offset; i < 26; i++) {
                Row remaining = t1.getRow(Access.ByIndex, i - iter + 1);
                assertThat(remaining, notNullValue());
                
                Cell cell = t1.getCell(remaining,  c1);
                assertThat(cell, notNullValue());
                assertThat(cell.isNull(), is(false));
                
                value = (char)('A' + i);
                assertThat(cell.getCellValue(), is(value));
            }
           
        }
        
        assertThat(t1.isNull(), is(true));
        assertThat(t1.getNumRows(), is(0));
        assertThat(t1.getNextCellOffset(), is(0));
        
        // do this again, but in reverse order
        t1.delete();
        assertThat(c1.isInvalid(), is(true));

        t1 = (TableImpl)TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        c1 = (ColumnImpl)t1.addColumn();
        
        // initialize column with alphabet, in reverse order
        t1.addRow(Access.ByIndex, 26);
        value = 'Z';
        for (int i = 0; i < 26; i++) 
            t1.setCellValue(t1.getRow(Access.ByIndex, 26 - i), c1, value--);
        
        assertThat(t1.getNextCellOffset(), is(26));
        assertThat(t1.getNumRows(), is(26));
        
        // delete first row, make sure other cells are as expected
        r = null;
        iter = 0;
        while ((r = t1.getRow(Access.Last)) != null) {
            r.delete();
            
            int offset =  ++iter;
            for (int i = 1; i < (26 - offset); i++) {
                Row remaining = t1.getRow(Access.ByIndex, i + 1);
                assertThat(remaining, notNullValue());
                
                Cell cell = t1.getCell(remaining,  c1);
                assertThat(cell, notNullValue());
                assertThat(cell.isNull(), is(false));
                
                value = (char)('A' + i);
                assertThat(cell.getCellValue(), is(value));
            }         
        }
        
        assertThat(t1.isNull(), is(true));
        assertThat(t1.getNumRows(), is(0));
        assertThat(t1.getNextCellOffset(), is(0));
        
        // do this again, but add some rows and make sure they go in correctly
        t1.delete();
        assertThat(c1.isInvalid(), is(true));

        t1 = (TableImpl)TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        c1 = (ColumnImpl)t1.addColumn();
        
        // initialize column with alphabet, in reverse order
        t1.addRow(Access.ByIndex, 26);
        value = 'Z';
        for (int i = 0; i < 26; i++) 
            t1.setCellValue(t1.getRow(Access.ByIndex, 26 - i), c1, value--);
        
        assertThat(t1.getNextCellOffset(), is(26));
        assertThat(t1.getNumRows(), is(26));
        
        // delete first row, make sure other cells are as expected
        r = null;
        iter = 0;
        int idx = 26;
        while (idx > 1) {
            r = t1.getRow(Access.ByIndex, idx--);
            r.delete();
            
            Row newRow = t1.addRow(Access.Last);
            t1.setCellValue(newRow, c1, (char)('A' + iter));
            
            int offset =  ++iter;
            for (int i = 1; i < (26 - offset); i++) {
                Row remaining = t1.getRow(Access.ByIndex, i + 1);
                assertThat(remaining, notNullValue());
                
                Cell cell = t1.getCell(remaining,  c1);
                assertThat(cell, notNullValue());
                assertThat(cell.isNull(), is(false));
                
                value = (char)('A' + i);
                assertThat(cell.getCellValue(), is(value));
            }         
        }
        
        assertThat(t1.isNull(), is(false));
        assertThat(t1.getNumRows(), is(26));
        assertThat(t1.getNextCellOffset(), is(26));
        
        // repeat one last time, only delete 7 rows, and add back 10, making sure we reuse
        // freed cells and allocate new ones
        t1.delete();
        assertThat(c1.isInvalid(), is(true));

        t1 = (TableImpl)TableFactory.createTable(1, 2, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(t1.getPropertyInt(TableProperty.RowCapacityIncr), is(2));
        assertThat(t1.getPropertyInt(TableProperty.numRowsCapacity), is(2));
        
        c1 = (ColumnImpl)t1.addColumn();
        
        // initialize column with alphabet, in reverse order
        t1.addRow(Access.ByIndex, 26);
        value = 'Z';
        for (int i = 0; i < 26; i++) 
            t1.setCellValue(t1.getRow(Access.ByIndex, 26 - i), c1, value--);
        
        assertThat(t1.getNextCellOffset(), is(26));
        assertThat(t1.getNumRows(), is(26));
        
        // delete first row, make sure other cells are as expected
        r = null;
        iter = 0;
        idx = 26;
        while (idx > 19) {
            r = t1.getRow(Access.ByIndex, idx--);
            r.delete();
            
            int offset =  ++iter;
            for (int i = 1; i < (26 - offset); i++) {
                Row remaining = t1.getRow(Access.ByIndex, i + 1);
                assertThat(remaining, notNullValue());
                
                Cell cell = t1.getCell(remaining,  c1);
                assertThat(cell, notNullValue());
                assertThat(cell.isNull(), is(false));
                
                value = (char)('A' + i);
                assertThat(cell.getCellValue(), is(value));
            }         
        }
        
        assertThat(t1.isNull(), is(false));
        assertThat(t1.getNumRows(), is(19));
        assertThat(t1.getNextCellOffset(), is(22));
        
        // now add 10 rows, make sure next cell offset is bumped up
        for (int i = 0; i < 10; i++)
            t1.setCellValue(t1.addRow(Access.Last), c1, i);
        
        assertThat(t1.getNumRows(), is(29));
        assertThat(t1.getNextCellOffset(), is(29));
    }
}
