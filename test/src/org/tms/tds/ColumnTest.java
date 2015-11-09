package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.NotUniqueException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.tds.dbms.DbmsTableImpl;

public class ColumnTest
{
    @Test
    public void getPropertiesTest()
    {
        ColumnImpl r = new ColumnImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Col property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        ColumnImpl r = new ColumnImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Column boolean property: " + p);
            Object value = r.getPropertyBoolean(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        ColumnImpl r = new ColumnImpl(null);
        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            System.out.print("Col int property: " + p);
            Object value = r.getPropertyInt(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }

    @Test
    public void getInitializablePropertiesTest()
    {
        ColumnImpl r = new ColumnImpl(null);
        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Column initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void testIndexedColumns()
    {
        TableImpl t = new TableImpl();
        assertThat(t, notNullValue());
        
        t.setColumnLabelsIndexed(true);
        assertThat(t.isColumnLabelsIndexed(), is(true));
        
        ColumnImpl r1 = t.addColumn();
        r1.setLabel("Unique Label 1");
        
        ColumnImpl r2 = t.addColumn();
        r2.setLabel("Unique Label 3");
        
        // test that we can relabel an existing Column
        r2.setLabel("Unique Label 2");

        // and use the old label somewhere else
        ColumnImpl r3 = t.addColumn();
        r3.setLabel("Unique Label 3");
        
        // delete Column, should free up label
        r2.delete();
        r2 = t.addColumn();
        r2.setLabel("Unique Label 2");
        
        // try to set a Column to a label that's in use
        try {
            r3.setLabel("Unique Label 2");
            fail("set label to not unique value");
        }
        catch (NotUniqueException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.NotUnique));
        }
        
        // disable indexing and try to set label again
        t.setColumnLabelsIndexed(false);
        assertThat(t.isColumnLabelsIndexed(), is(false));
        r3.setLabel("Unique Label 2");
        
        // try to reindex Columns, should fail, as Column labels are not unique
        // try to set a Column to a label that's in use
        try {
            t.setColumnLabelsIndexed(true);
            fail("reindexed non-unique labels");
        }
        catch (NotUniqueException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.NotUnique));
        }
        
        // clear non-unique value and try again
        r3.setLabel(null);
        t.setColumnLabelsIndexed(true);
        assertThat(t.isColumnLabelsIndexed(), is(true));
    }
    
    @Test
    public void addColumnsTest()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        ColumnImpl c1 = t.addColumn(Access.Next);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(1));    
        
        ColumnImpl c2 = new ColumnImpl(t);
        t.add(c2, Access.ByIndex, 3);
        assertThat(c2.getIndex(), is(3));
        assertThat(t.getNumColumns(), is(3));
        assertThat(t.calcIndex(ElementType.Column, Access.ByIndex, false, 3), is(2));
        
        ColumnImpl c3 = new ColumnImpl(t);
        t.add(c3, Access.ByIndex, 3);
        assertThat(c3.getIndex(), is(3));
        assertThat(c2.getIndex(), is(4));
        assertThat(c3.getIndex(), is(3));
        assertThat(t.getNumColumns(), is(4));       
        assertThat(t.getNumRows(), is(0));       

        ColumnImpl c4 = new ColumnImpl(t);
        t.add(c4, Access.ByIndex, 2);
        assertThat(c1.getIndex(), is(1));
        assertThat(c4.getIndex(), is(2));
        assertThat(c3.getIndex(), is(4));
        assertThat(c2.getIndex(), is(5));
        assertThat(t.getNumColumns(), is(5));       
        assertThat(t.getNumRows(), is(0));   
        
        ColumnImpl c = t.getColumn(Access.First);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(1));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(2));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(3));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(4));
        
        c = t.getColumn(Access.Next);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(5));
        
        c = t.getColumn(Access.Last);
        assertThat(c, notNullValue());
        assertThat(c.getIndex(), is(5));
        
        c = t.getColumn(Access.Next);
        assertThat(c, nullValue());
        
        ColumnImpl cx = t.addColumn(Access.ByIndex, 20);
        assertThat(cx, notNullValue());
        assertThat(t.getNumColumns(), is(20));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(19));    
        
        cx = t.getColumn(Access.Current);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(19));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(18));    
        
        cx = t.getColumn(Access.Current);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(18));    
        
        cx = t.getColumn(Access.Previous);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(17));    
        
        cx = t.getColumn(Access.Last);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(20));    
        
        cx = t.addColumn(Access.Last);
        assertThat(cx, notNullValue());
        assertThat(cx.getIndex(), is(21));    
    }
    
    @Test 
    public void deleteColumnsTest()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        
        SubsetImpl r = new SubsetImpl(t);
        assertThat(r, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        ColumnImpl c1 = t.addColumn(Access.Next);
        assertThat(c1, notNullValue());
        r.add(c1);
        assertThat(c1.getIndex(), is(1));
        assertThat(t.getNumColumns(), is(1));    
        
        ColumnImpl c2 = t.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        r.add(c2);
        assertThat(c2.getIndex(), is(2));
        assertThat(t.getNumColumns(), is(2));
             
        assertThat(r.getNumColumnsInternal(), is(2));
        assertThat(r.contains(c1), is(true));
        assertThat(r.contains(c2), is(true));
        
        c1.delete();        
        assertThat(c2, notNullValue());
        assertThat(c2.getIndex(), is(1));
        assertThat(t.getNumColumns(), is(1));
        assertThat(c1.isInUse(), is(false));
        
        assertThat(r.getNumColumnsInternal(), is(1));
        assertThat(r.contains(c1), is(false));
        assertThat(r.contains(c2), is(true));
        
        c2.delete();       
        assertThat(t.getNumColumns(), is(0));    
    }
    
    @Test
    public void columnIterableTest()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        ColumnImpl c1 = t.addColumn(Access.ByIndex, 100);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(100));    
        
        int idx = 0;
        for (Column c : t.columns()) {
        	idx++;
        	if (c != null)
        		assertThat(((ColumnImpl)c).getIndex(), is(idx));
        }
        
        assertThat(idx, is(100));   	
    }
    
    @Test
    public void testColumnFill()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
            
        assertThat(t.getNumColumns(), is(0));
        ColumnImpl c1 = t.addColumn(Access.ByIndex, 16);
        assertThat(c1, notNullValue());
        assertThat(t.getNumColumns(), is(16)); 
        assertThat(t.getNumCells(), is(0));
        
        RowImpl r1 = t.addRow(Access.Next);
        assertThat(t.getNumCells(), is(0));
        assertThat(c1.fill(42), is(true));
        assertThat(t.getNumCells(), is(1));

        assertThat(c1.fill(42), is(false));
        
        assertThat(t.fill(412), is(true));
        assertThat(t.fill(412), is(false));
        
        assertThat(t.clear(), is(true));
        assertThat(t.clear(), is(false));        
        assertThat(t.fill(412), is(true));
        
        r1.fill(64);
        assertThat(t.getNumCells(), is(16));       
    }
        
    @Test
    public void testAddColumnsByValue()
    {
        TableImpl t = new TableImpl(10, 10);
        assertThat(t, notNullValue());
        
        Column c = t.addColumn(Access.ByLabel, "Test Col");
        assertThat(c, notNullValue());
        
        assertThat(1, is(c.getIndex()));
        assertThat("Test Col", is(c.getLabel()));
        
        c = t.getColumn(Access.ByLabel, "Test Col");
        assertThat(c, notNullValue());
        
        try {
        	//this should fail
        	c = t.addColumn(Access.ByLabel, "Test Col");
        	fail("Collumn added");
        }
        catch (Exception e) {}
        
    	//this should succeed
    	c = t.addColumn(Access.ByLabel, "Test Col", true);
        assertThat(c, notNullValue());        
        assertThat(2, is(c.getIndex()));   
        
        // ByDataType
        c = t.addColumn(Access.ByDataType, TableImpl.class);
        assertThat(c, notNullValue());
        
        assertThat(3, is(c.getIndex()));
        assertThat(c.getDataType() == TableImpl.class, is(true));
        
        c = t.getColumn(Access.ByDataType, TableImpl.class);
        assertThat(c, notNullValue());
        assertThat(3, is(c.getIndex()));
        
        try {
        	//this should fail
            c = t.addColumn(Access.ByDataType, TableImpl.class);
        	fail("Column added");
        }
        catch (Exception e) {}
        
    	//this should succeed
        c = t.addColumn(Access.ByDataType, TableImpl.class, true, true);
        assertThat(c, notNullValue());        
        assertThat(4, is(c.getIndex()));     
        
        // retrieve it again, should get the first one
        c = t.getColumn(Access.ByDataType, DbmsTableImpl.class, false);
        assertThat(c, notNullValue());
        assertThat(3, is(c.getIndex()));
    }
}
