package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.events.TableElementEventType;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.teq.RemoteValueService;

public class WeakTableReferencesTest
{
    @Test
    public void testNonpersistantTables() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        int numTables = 64;
        int nRows = 1024;
        int nCols = 256;
        
        tc.setRowCapacityIncr(nRows);
        tc.setColumnCapacityIncr(nCols);
        for (int i = 0; i < numTables; i++)
            createLargeTable(tc, nRows, nCols, "Really big table!!!");
        
        assertThat(tc, notNullValue());
        
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(500);
        }
        
        assertThat(tc.getNumTables(), is(0));      
    }
    
    @Test
    public void testPersistantTables() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        int numTables = 8;
        int nRows = 1024;
        int nCols = 256;
        
        tc.setRowCapacityIncr(nRows);
        tc.setColumnCapacityIncr(nCols);
        ((ContextImpl)tc).setPersistant(true);        
        
        assertThat(tc.getNumTables(), is(0));
        for (int i = 0; i < numTables; i++)
            createLargeTable(tc, nRows, nCols, "Really big table!!!");
        
        assertThat(tc, notNullValue());
        assertThat(tc.getNumTables(), is(numTables));
        
        System.gc();
        Thread.sleep(500);
        
        assertThat(tc.getNumTables(), is(numTables));     
        
        Table t = tc.getTable(Access.ByLabel, "persistant");
        while (t != null) {
            ((TableImpl)t).setPersistant(false);
            
            // allow garbage collector to make the now non-persistant table go away
            t = null;
            System.gc();
            
            t = tc.getTable(Access.ByLabel, "persistant");
        }
        
        System.gc();
        Thread.sleep(1000);
        System.gc();
        assertThat(tc.getNumTables(), is(0));     
    }
    
    private Table createLargeTable(TableContext tc, int rows, int cols, Object fillValue)
    {
        Table t = TableFactory.createTable(rows, cols, tc);
        assertThat(((TableImpl)t).isPersistant(), is(((ContextImpl)tc).isPersistant()));

        t.setLabel("persistant");

        Column c1 = t.addColumn();
        Column c2 = t.addColumn(Access.ByIndex, 2);
        t.addColumn(Access.ByIndex, cols);
        
        Row r1 = t.addRow(Access.ByIndex, rows);
        t.addListeners(TableElementEventType.OnNewValue, e -> noop(t + ": " + e));
        t.fill(fillValue);
        
        c1.addListeners(TableElementEventType.OnNewValue, e -> noop(c1 + ": " + e));
        c1.fill("abc");
        
        c1.setDerivation("ridx");
        c2.setDerivation("4 + CoL 1");
        
        Subset s = t.addSubset(Access.ByLabel, "Subset");
        s.add(r1, c1, c2);
        
        assertThat(c1, notNullValue());
        
        return t;
    }    
    
    public void noop(String s) {}
    
    @Test
    public void testNonpersistantTableWithRemoteValue() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        int numTables = 16;
        int nRows = 1024;
        int nCols = 256;
        
        tc.setRowCapacityIncr(nRows);
        tc.setColumnCapacityIncr(nCols);
        for (int i = 0; i < numTables; i++) {
            createLargeTable(tc, nRows, nCols, "Really big table!!!");
        }
        
        assertThat(tc, notNullValue());
        
        Table t = createLargeTable(tc, nRows, nCols, "Really big table!!!");
        Column c = t.addColumn();
        c.setDerivation("rn()");
        
        Row r1 = t.getRow(1);
        assertNotNull(r1);
        
        Cell cR1Cr = t.getCell(r1, c);
        assertNotNull(cR1Cr);
        
        String uuid = cR1Cr.lookupRemoteUUID();
        
        assertThat(RemoteValueService.numHandlers(), is(nRows));
        assertNotNull(uuid);
        
        c = null;
        r1 = null;
        cR1Cr = null;
        
        t = null;
        
        for (int i = 0; i < 5; i++) {
            System.gc();
            Thread.sleep(500);
        }
        
        assertThat(tc.getNumTables(), is(0));   
        assertThat(RemoteValueService.numHandlers(), is(0));
    }   
}
