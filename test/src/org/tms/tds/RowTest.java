package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;

public class RowTest
{

    @Test
    public void getPropertiesTest()
    {
        Row r = new Row(null);

        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Row property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Row r = new Row(null);

        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Row boolean property: " + p);
            Object value = r.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        Row r = new Row(null);

        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Row int property: " + p);
            Object value = r.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }

    @Test
    public void getInitializablePropertiesTest()
    {
        Row r = new Row(null);

        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Row initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void addRowTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
        
        Row r = new Row(t);
        r.setLabel("Test Case");
        
        t.add(r, Access.ByIndex, 6);
        assertThat(r.getIndex(), is(6));
        assertThat(t.getNumRows(), is(6));
        assertThat(t.calcIndex(ElementType.Row, Access.ByIndex, false, 6), is(5));
        
        // check row retrieval
        Row retRow = t.getRow(Access.Current);
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        retRow = t.getRow(Access.Last);
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        retRow = t.getRow(Access.ByReference, r);
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        retRow = t.getRow(Access.ByIndex, 6);
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        retRow = t.getRow(Access.ByLabel, "Test Case");
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        retRow = t.getRow(Access.ByLabel, "Label Does Not Exist");
        assertThat(retRow, nullValue());
        
        retRow = t.getRow(Access.ByProperty, TableProperty.Label, "Test Case");
        assertThat(retRow, notNullValue());
        assertThat(retRow, is(r));
        assertThat(retRow.getTable(), is(t));
        assertThat(retRow.getIndex(), is(6));
        
        Row r2 = new Row(t);
        t.add(r2, Access.ByIndex, 6);
        assertThat(r2.getIndex(), is(6));
        assertThat(r.getIndex(), is(7));
        assertThat(t.getNumRows(), is(7));
        
        t.addRow(Access.First);
        assertThat(t.getNumRows(), is(8));
        
        t.addRow(Access.First);
        assertThat(t.getNumRows(), is(9));
        
        t.addRow(Access.First);
        assertThat(t.getNumRows(), is(10));
        assertThat(r2.getIndex(), is(9));
        assertThat(r.getIndex(), is(10));
        
        t.addRow(Access.First);
        assertThat(t.getNumRows(), is(11));
        assertThat(r2.getIndex(), is(10));
        assertThat(r.getIndex(), is(11));
    }
    
    @Test
    public void addRowColumnTest()
    {
        Table t = new Table(10, 10);
        assertThat(t, notNullValue());
        
        Row r = new Row(t);
        r.setLabel("Test Case");
        
        t.add(r, Access.ByIndex, t.getRowsCapacity());
        assertThat(r.getIndex(), is(t.getRowsCapacity()));
        assertThat(t.getNumRows(), is(t.getRowsCapacity()));
        assertThat(t.calcIndex(ElementType.Row, Access.ByIndex, false, t.getRowsCapacity()), is(t.getRowsCapacity()-1));
        assertThat(r.getNumCells(), is(0));
        
        // add a column
        Column c = t.addColumn(Access.ByIndex, 3);
        assertThat(c, notNullValue());
        assertThat(c.getNumCells(), is(0));
        assertThat(r.getNumCells(), is(0));
        
        // add another row and make sure cell count is still 0
        Row r2 = t.addRow(Access.Last);
        assertThat(r2, notNullValue());
        assertThat(r2.getNumCells(), is(0));
        
        // make sure row and col are marked not in use
        assertThat(r2.isInUse(), is(false));
        assertThat(c.isInUse(), is(false));
        assertThat(r2.getNumCells(), is(0));
        assertThat(c.getNumCells(), is(0));
        
        // force cell creation
        Cell cell = t.getCell(r2, c);
        assertThat(cell, notNullValue());
        
        // make sure row and col are marked not in use
        assertThat(r2.isInUse(), is(true));
        assertThat(c.isInUse(), is(true));
        assertThat(r2.getNumCells(), is(1));
        assertThat(c.getNumCells(), is(1));
        
        Row r3 = t.addRow(Access.ByIndex, t.getRowsCapacity());
        cell = t.getCell(r3, c);
        assertThat(cell, notNullValue());
        assertThat(r3.getNumCells(), is(1));
        assertThat(c.getNumCells(), is(2));
        assertThat(t.getNumCells(), is(2));
        
        // now add another column and check total cell count
        c = t.addColumn(Access.ByIndex, 3);
        assertThat(c, notNullValue());
        assertThat(c.getNumCells(), is(0));
        assertThat(r.getNumCells(), is(0));
        assertThat(r2.getNumCells(), is(1));
        assertThat(r3.getNumCells(), is(1));
        assertThat(t.getNumCells(), is(2));
        
        // now get the cells in the new column and make sure the cell count goes up
        cell = t.getCell(r2, c);
        assertThat(cell, notNullValue());
        assertThat(c.getNumCells(), is(1));
        assertThat(r.getNumCells(), is(0));
        assertThat(r2.getNumCells(), is(2));
        assertThat(r3.getNumCells(), is(1));
        assertThat(t.getNumCells(), is(3));
        
        cell = t.getCell(r3, c);
        assertThat(cell, notNullValue());
        assertThat(c.getNumCells(), is(2));
        assertThat(r.getNumCells(), is(0));
        assertThat(r2.getNumCells(), is(2));
        assertThat(r3.getNumCells(), is(2));
        assertThat(t.getNumCells(), is(4));
    }
}
