package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.NotUniqueException;
import org.tms.api.exceptions.TableErrorClass;

public class RowTest extends BaseTest
{
	//@Rule public SimpleRepeatRule repeatRule = new SimpleRepeatRule(100);
	@Test
	public void fillManyJSONRowTest() throws ParseException
	{
        // create a JSON object to use as our test
        int numRows = 5000;
        TableImpl t = new TableImpl(numRows, 6);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
        
        t.setColumnLabelsIndexed(true);
                
        // create a row, fill it with JSON
        t.addColumn(Access.ByIndex, 6);
        for (int i = 0; i < numRows; i++) {
    		String jText = "{\"Balance\":1000.21,\"Num\":100,\"Nick Name\":null,\"Name\":\"Sam Sneed\",\"Tricky\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"VIP\":true}";
            JSONObject json = buildJSONObject(jText);
            assertThat(json, notNullValue());
            
	        Row r = t.addRow();
	        assertThat(r, notNullValue());
	        assertThat(r.getNumCells(), is(0));
	        
	        // do the json fill
	        boolean setAny = r.fill(json);
	        assertThat(setAny, is(true));      
	        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));  
        }
        
        assertThat(t.getNumRows(), is(numRows));
	}
	
	@Test
	public void fillJSONRowTest() throws ParseException
	{
        // create a JSON object to use as our test
		String jText = "{\"Balance\":1000.21,\"Num\":100,\"Nick Name\":null,\"Name\":\"Sam Sneed\",\"Tricky\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"VIP\":true}";
        JSONObject json = buildJSONObject(jText);
        assertThat(json, notNullValue());
        
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
                
        // create a row, fill it with JSON
        Row r = t.addRow();
        assertThat(r, notNullValue());
        assertThat(r.getNumCells(), is(0));
        
        // do the json fill
        boolean setAny = r.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));      
        
        // Add another row. Column labels are set this time from prev fill
        Row r2 = t.addRow();
        setAny = r2.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r2), is(true));      
      
        t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
        
        t.addColumn(Access.ByLabel, "Balance");
        t.addColumn(Access.ByLabel, "Num");
        t.addColumn(Access.ByLabel, "Name");
                
        // create a row, fill it with JSON
        r = t.addRow();
        assertThat(r, notNullValue());
        
        // fill another row
        setAny = r.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));      
        
        // all numeric column references
        // create a JSON object to use as our test
		jText = "{\"1\":1000.21,\"2\":100,\"3\":null,\"4\":\"Sam Sneed\",\"5\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"6\":true}";
        json = buildJSONObject(jText);
        assertThat(json, notNullValue());
        
        t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
        
        // create a row, fill it with JSON
        r = t.addRow();
        assertThat(r, notNullValue());
        
        // fill another row
        setAny = r.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));      

		jText = "{\"1\":1000.21,\"2\":100,\"3\":null,\"4\":\"Sam Sneed\",\"5\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"VIP\":true,\"6\":false}";
        json = buildJSONObject(jText);
        assertThat(json, notNullValue());
        
        // create a row, fill it with JSON
        r = t.addRow();
        t.addColumn(Access.ByIndex, 6).setLabel("VIP");
        assertThat(r, notNullValue());
        
        // this should fail
        try {
        	r.fill(json);
        	fail("Set duplicate element");
        }
        catch (InvalidException ie) { /* noop */}
        catch (Exception e) { fail(e.getMessage()); }      
	}

	@Test
	public void fillJSONRowComplexTest() throws ParseException
	{
        // create a JSON object to use as our test
		String jText = "{\"A\":{\"Balance\":1000.21,\"Num\":100},\"Nick Name\":null,\"Name\":\"Sam Sneed\",\"Tricky\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"VIP\":true}";
        JSONObject json = buildJSONObject(jText);
        assertThat(json, notNullValue());
        
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
                
        // create a row, fill it with JSON
        Row r = t.addRow();
        assertThat(r, notNullValue());
        assertThat(r.getNumCells(), is(0));
        
        // do the json fill
        boolean setAny = r.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));   
	}
        
	@Test
	public void fillJSONColumnTest() throws ParseException
	{
        // create a JSON object to use as our test
		String jText = "{\"Balance\":1000.21,\"Num\":100,\"Nick Name\":null,\"Name\":\"Sam Sneed\",\"Tricky\":\"Tricky Text: \\, /, \t, \r, Tricky\",\"VIP\":true}";
        JSONObject json = buildJSONObject(jText);
        assertThat(json, notNullValue());
        
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
                
        // create a row, fill it with JSON
        Column r = t.addColumn();
        assertThat(r, notNullValue());
        assertThat(r.getNumCells(), is(0));
        
        // do the json fill
        boolean setAny = r.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true));      
        
        // Add another row. Column labels are set this time from prev fill
        Column r2 = t.addColumn();
        setAny = r2.fill(json);
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r2), is(true));      
      
        t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(0));
        assertThat(t.getNumColumns(), is(0));
        
        t.addRow(Access.ByLabel, "Balance");
        t.addRow(Access.ByLabel, "Num");
        t.addRow(Access.ByLabel, "Name");
                
        // create a row, fill it with JSON
        r = t.addColumn();
        assertThat(r, notNullValue());
        
        // fill another row
        setAny = r.fill(json);
        json.toJSONString();
        assertThat(setAny, is(true));      
        assertThat(validateJSONFill(json, (TableSliceElementImpl)r), is(true)); 
	}

    @Test
    public void getPropertiesTest()
    {
        RowImpl r = new RowImpl(null);

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
        RowImpl r = new RowImpl(null);

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
        RowImpl r = new RowImpl(null);

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
        RowImpl r = new RowImpl(null);

        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Row initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void testIndexedRows()
    {
        TableImpl t = new TableImpl();
        assertThat(t, notNullValue());
        
        t.setRowLabelsIndexed(true);
        assertThat(t.isRowLabelsIndexed(), is(true));
        
        RowImpl r1 = t.addRow();
        r1.setLabel("Unique Label 1");
        
        RowImpl r2 = t.addRow();
        r2.setLabel("Unique Label 3");
        
        // test that we can relabel an existing row
        r2.setLabel("Unique Label 2");

        // and use the old label somewhere else
        RowImpl r3 = t.addRow();
        r3.setLabel("Unique Label 3");
        
        // delete row, should free up label
        r2.delete();
        r2 = t.addRow();
        r2.setLabel("Unique Label 2");
        
        // try to set a row to a label that's in use
        try {
            r3.setLabel("Unique Label 2");
            fail("set label to not unique value");
        }
        catch (NotUniqueException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.NotUnique));
        }
        
        // disable indexing and try to set label again
        t.setRowLabelsIndexed(false);
        assertThat(t.isRowLabelsIndexed(), is(false));
        r3.setLabel("Unique Label 2");
        
        // try to reindex rows, should fail, as row labels are not unique
        // try to set a row to a label that's in use
        try {
            t.setRowLabelsIndexed(true);
            fail("reindexed non-unique labels");
        }
        catch (NotUniqueException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.NotUnique));
        }
        
        // clear non-unique value and try again
        r3.setLabel(null);
        t.setRowLabelsIndexed(true);
        assertThat(t.isRowLabelsIndexed(), is(true));
    }
    
    @Test
    public void testRowIdent()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        
        t.addRow();
        t.addRow();
        
        RowImpl r = t.addRow();
        r.setLabel("Test Case");
        
        t.addRow();
        
        long ident = r.getIdent();
        assertThat(ident > 0, is(true));
        
        // get row by ident
        Row r2 = t.getRow(Access.ByIdent, ident);
        assertThat(r2, is(r));
    }

    @Test
    public void addRowTest()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        
        RowImpl r = new RowImpl(t);
        r.setLabel("Test Case");
        
        t.add(r, Access.ByIndex, 6);
        assertThat(r.getIndex(), is(6));
        assertThat(t.getNumRows(), is(6));
        assertThat(t.calcIndex(ElementType.Row, Access.ByIndex, false, 6), is(5));
        
        // check row retrieval
        RowImpl retRow = t.getRow(Access.Current);
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
        
        RowImpl r2 = new RowImpl(t);
        t.add(r2, Access.ByIndex, 6);
        assertThat(r2.getIndex(), is(6));
        assertThat(r.getIndex(), is(7));
        assertThat(t.getNumRows(), is(7));
        
        t.addRow(Access.First); // Row 1
        assertThat(t.getNumRows(), is(8));
        
        t.addRow(Access.First); // Row 2
        assertThat(t.getNumRows(), is(9));
        
        t.addRow(Access.First); // Row 3
        assertThat(t.getNumRows(), is(10));
        assertThat(r2.getIndex(), is(9));
        assertThat(r.getIndex(), is(10));
        
        t.addRow(Access.First); // Row 4
        assertThat(t.getNumRows(), is(11));
        assertThat(r2.getIndex(), is(10));
        assertThat(r.getIndex(), is(11));
    }
    
    @Test
    public void addRowColumnTest()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        
        RowImpl r1 = new RowImpl(t);
        r1.setLabel("Test Case");
        
        t.add(r1, Access.ByIndex, t.getRowsCapacity());
        assertThat(r1.getIndex(), is(t.getRowsCapacity()));
        assertThat(t.getNumRows(), is(t.getRowsCapacity()));
        assertThat(t.calcIndex(ElementType.Row, Access.ByIndex, false, t.getRowsCapacity()), is(t.getRowsCapacity()-1));
        assertThat(r1.getNumCells(), is(0));
        
        // add a column
        ColumnImpl c1 = t.addColumn(Access.ByIndex, 3);
        assertThat(c1, notNullValue());
        assertThat(c1.getNumCells(), is(0));
        assertThat(r1.getNumCells(), is(0));
        
        // add another row and make sure cell count is still 0
        RowImpl r2 = t.addRow(Access.Last);
        assertThat(r2, notNullValue());
        assertThat(r2.getNumCells(), is(0));
        
        // make sure row and col are marked not in use
        assertThat(r2.isInUse(), is(false));
        assertThat(c1.isInUse(), is(false));
        assertThat(r2.getNumCells(), is(0));
        assertThat(c1.getNumCells(), is(0));
        
        // force cell creation
        CellImpl cellR2C1 = t.getCell(r2, c1);
        assertThat(cellR2C1, notNullValue());
        cellR2C1.setCellValue("R2 C1");
        
        // make sure row and col are marked not in use
        assertThat(r2.isInUse(), is(true));
        assertThat(c1.isInUse(), is(true));
        assertThat(r2.getNumCells(), is(1));
        assertThat(c1.getNumCells(), is(1));
        
        RowImpl r3 = t.addRow(Access.ByIndex, t.getRowsCapacity());
        CellImpl cellR3C1 = t.getCell(r3, c1);
        assertThat(cellR3C1, notNullValue());
        assertThat(r3.getNumCells(), is(1));
        assertThat(c1.getNumCells(), is(2));
        assertThat(t.getNumCells(), is(2));
        
        // now add another column and check total cell count
        ColumnImpl c2 = t.addColumn(Access.ByIndex, 3);
        assertThat(c2, notNullValue());
        assertThat(c2.getNumCells(), is(0));
        assertThat(r1.getNumCells(), is(0));
        assertThat(r2.getNumCells(), is(1));
        assertThat(r3.getNumCells(), is(1));
        assertThat(t.getNumCells(), is(2));
        
        // now get the cells in the new column and make sure the cell count goes up
        CellImpl cellR1C2 = t.getCell(r1, c2);
        assertThat(cellR1C2, notNullValue());
        assertThat(c2.getNumCells(), is(1));
        assertThat(r1.getNumCells(), is(1));
        assertThat(r2.getNumCells(), is(1));
        assertThat(r3.getNumCells(), is(1));
        assertThat(t.getNumCells(), is(3));
        
        CellImpl cellR3C2 = t.getCell(r3, c2);
        assertThat(cellR3C2, notNullValue());
        assertThat(c2.getNumCells(), is(2));
        assertThat(r1.getNumCells(), is(1));
        assertThat(r2.getNumCells(), is(1));
        assertThat(r3.getNumCells(), is(2));
        assertThat(t.getNumCells(), is(4));
    }
}
