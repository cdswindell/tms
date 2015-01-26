package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.DataTypeEnforcementException;

public class CellTest
{

    @Test
    public void getPropertiesTest()
    {
        CellImpl c = new CellImpl(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) {
            System.out.print("CellImpl property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getInitializablePropertiesTest()
    {
        CellImpl c = new CellImpl(null, -1);

        List<TableProperty> props = c.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("CellImpl initializable property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void getIntPropertiesTest()
    {
        CellImpl c = new CellImpl(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) { 
        	if (!p.isIntValue()) continue;
            // will fail if property getter not implemented
            System.out.print("CellImpl int property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    
    @Test
    public void getBooleanPropertiesTest()
    {
        CellImpl c = new CellImpl(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) { 
        	if (!p.isBooleanValue()) continue;
            // will fail if property getter not implemented
            System.out.print("CellImpl boolean property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void enforceCellDataTypeHierarchyTest()
    {
        TableImpl t = new TableImpl();
        RowImpl r1 = t.addRow(Access.Next);
        ColumnImpl c1 = t.addColumn(Access.Next);
        CellImpl cR1C1 = t.getCell(r1,  c1);
        
        // sample the various combinations of enforcing cell data types
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));
        assertThat(cR1C1.isDataTypeEnforced(), is(false));
        
        // setting the table property to true should set enforcement for all
        t.setEnforceDataType(true);
        assertThat(t.isDataTypeEnforced(), is(true));
        assertThat(r1.isDataTypeEnforced(), is(true));
        assertThat(c1.isDataTypeEnforced(), is(true));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        // setting the row property to true should set enforcement for row and cell
        t.setEnforceDataType(false);
        r1.setEnforceDataType(true);
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(true));
        assertThat(c1.isDataTypeEnforced(), is(false));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        // setting the column property to true should set enforcement for row and cell,
        // but only once a datatype is specified
        r1.setEnforceDataType(false);
        c1.setEnforceDataType(true);
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(true));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        c1.setDataType(Integer.class);
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(true));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        // Finally, check the cell machinery
        // setting the column property to true should set enforcement for row and cell,
        r1.setEnforceDataType(false);
        c1.setEnforceDataType(false);
        cR1C1.setEnforceDataType(true);
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        cR1C1.setCellValue(42);
        assertThat(t.isDataTypeEnforced(), is(false));
        assertThat(r1.isDataTypeEnforced(), is(false));
        assertThat(c1.isDataTypeEnforced(), is(false));
        assertThat(cR1C1.isDataTypeEnforced(), is(true));
        
        try {
            cR1C1.setCellValue("abc");
            fail("set cell value");
        }
        catch (DataTypeEnforcementException e)
        {
            assertThat(e, notNullValue());
        }
        
        cR1C1.setEnforceDataType(false);
        cR1C1.setCellValue("abc");
        assertThat(cR1C1.getDataType(), is((Object)String.class));
        
        // Set the datatype on the column and try again
        c1.setEnforceDataType(true);
        c1.setDataType(Double.class);
        
        try {
            cR1C1.setCellValue("abc");
            fail("set cell value");
        }
        catch (DataTypeEnforcementException e)
        {
            assertThat(e, notNullValue());
        }
        
        cR1C1.setCellValue(Double.valueOf(12.6));
        assertThat(cR1C1.getDataType(), is((Object)Double.class));
        
        // test with row EnforceDataType set and a column datatype set
        r1.setEnforceDataType(true);
        c1.setEnforceDataType(false);
        c1.setDataType(String.class);

        cR1C1.setCellValue("def");
        assertThat(cR1C1.getDataType(), is((Object)String.class));
               
        try {
            cR1C1.setCellValue(123);
            fail("set cell value");
        }
        catch (DataTypeEnforcementException e)
        {
            assertThat(e, notNullValue());
            assertThat(e.getAllowed(), is((Object)String.class));
            assertThat(e.getRejected(), is((Object)Integer.class));
        }
        
        cR1C1.setCellValue(null);
        try {
            cR1C1.setCellValue(123);
            fail("set cell value");
        }
        catch (DataTypeEnforcementException e)
        {
            assertThat(e, notNullValue());
            assertThat(e.getAllowed(), is((Object)String.class));
            assertThat(e.getRejected(), is((Object)Integer.class));
        }       
        
        cR1C1.setCellValue(null);
        c1.setDataType(null);
        cR1C1.setCellValue(123);
        
        try {
            cR1C1.setCellValue("ABC");
            fail("set cell value");
        }
        catch (DataTypeEnforcementException e)
        {
            assertThat(e, notNullValue());
            assertThat(e.getAllowed(), is((Object)Integer.class));
            assertThat(e.getRejected(), is((Object)String.class));
        }       
    }
}
