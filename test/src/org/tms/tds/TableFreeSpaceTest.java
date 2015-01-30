package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
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
        assertThat(t1.getPropertyInt(TableProperty.numColumnsCapacity), is(14));
        
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
    public void testCellFreeSpaceReclamation()
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
}
