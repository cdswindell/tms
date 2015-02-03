package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.tms.api.Subset;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidParentException;

public class SubsetTest
{

    @Test
    public void getPropertiesTest()
    {
        SubsetImpl r = new SubsetImpl(null);

        List<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            System.out.print("Subset property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getInitializablePropertiesTest()
    {
        SubsetImpl r = new SubsetImpl(null);

        List<TableProperty> props = r.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Subset initializable property: " + p);
            Object value = r.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void createSubsetTest() throws InterruptedException
    {
        TableImpl t = new TableImpl(10, 10);
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(0));
        
        SubsetImpl r = new SubsetImpl(t);
        assert (r != null);
        
        int numRows = r.getPropertyInt(TableProperty.numRows);
        assertThat(numRows, is(0));
        assertThat(r.getTable(), is(t));
        assertThat(r.getTableContext(), is(t.getTableContext()));
        
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(1));
        
        t.remove(r);
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(0));
        
        // test weak reference ability
        t.add(r);
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(1));
        
        r = null;
        System.gc();
        
        Thread.sleep(1000);
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(0));
   }
    
    @Test
    public void rowsTest()
    {
        TableImpl t = new TableImpl(100, 100);
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numSubsets), is(0));
        
        SubsetImpl r = new SubsetImpl(t);
        assert (r != null);
        
        RowImpl r1 = new RowImpl(t);
        RowImpl r2 = new RowImpl(t);
        RowImpl r3 = new RowImpl(t);
        
        Set<RowImpl> rs = new HashSet<RowImpl>();
        rs.add(r1);
        rs.add(r2);
        rs.add(r3);
        
        assertThat(r.addAll(rs), is(true));
        assertThat(r.getNumRows(), is(3));
        assertThat(r.contains(r1), is(true));
        assertThat(r.contains(r2), is(true));
        assertThat(r.contains(r3), is(true));
        assertThat(r.containsAll(rs), is(true));
        
        List<Subset> rowSubsets = r1.getSubsets();
        assertThat(rowSubsets, notNullValue());
        assertThat(rowSubsets.isEmpty(), is(false));
        assertThat(rowSubsets.contains(r), is(true));
        
        // make sure subsets retrieved from rows are immutable
        try {
            rowSubsets.clear();
            fail("Modified immutable set");
        }
        catch (UnsupportedOperationException e) {
            assertThat(e, notNullValue());
        }
        
        // make sure we can't add rows from another table
        TableImpl t2 = new TableImpl(100, 100);
        RowImpl rf = t2.addRow();
        try {
            r.add(rf);
            fail("added foriegn");
        }
        catch (InvalidParentException e) {
            assertThat(e, notNullValue());
        }
        
        // test range removal methods
        r.remove(r1, r2, r3);
        assertThat(r.getNumRows(), is(0));
        
        assertThat(r.removeAll(rs), is(false));
    }
}
