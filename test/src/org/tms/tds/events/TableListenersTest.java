package org.tms.tds.events;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.events.BlockedRequestException;
import org.tms.api.events.CellValueChangedEvent;
import org.tms.api.events.TableElementEvent;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.factories.TableFactory;
import org.tms.tds.events.TableElementListeners;

public class TableListenersTest extends BaseTest
{
    @Test
    public void testOnBeforeCellValueChangedEvent() throws InterruptedException
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);      
        
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        assertThat(tbl.hasListeners(), is(false));
        assertThat(r1.hasListeners(), is(false));
        assertThat(r2.hasListeners(), is(false));
        assertThat(c1.hasListeners(), is(false));
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(false));
        
        TableCellListener stl = new TableCellListener();
        assertThat(stl.getNumFired(), is(0));
        
        tbl.addListeners(TableElementEventType.OnBeforeNewValue, stl); 
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(true));
        assertThat(tbl.hasListeners(), is(true));
        assertThat(tbl.hasListeners(TableElementEventType.OnBeforeNewValue), is(true));
        assertThat(tbl.hasListeners(TableElementEventType.OnNewValue), is(false));

        r1.addListeners(TableElementEventType.OnBeforeNewValue, stl);
        assertThat(tbl.hasListeners(TableElementEventType.OnBeforeNewValue), is(true));
        assertThat(tbl.hasListeners(TableElementEventType.OnNewValue), is(false));
        
        assertThat(c1.hasListeners(TableElementEventType.OnBeforeNewValue), is(false));
        assertThat(c1.hasListeners(TableElementEventType.OnNewValue), is(false));
        c1.addListeners(TableElementEventType.OnBeforeNewValue, stl);
        assertThat(c1.hasListeners(TableElementEventType.OnBeforeNewValue), is(true));
        assertThat(c1.hasListeners(TableElementEventType.OnNewValue), is(false));
        
        // set cell, cell listener should fire 3 times
        Cell c = tbl.getCell(r1,  c1);
        c.setCellValue(12);
        assertThat(stl.getNumFired(), is(3));

        // set the cell listener, slightly different event firing path
        c.addListeners(TableElementEventType.OnBeforeNewValue, stl);
        c.setCellValue(12);
        assertThat(stl.getNumFired(), is(0)); // shouldn't fire, same value
        
        c.setCellValue(14);
        assertThat(stl.getNumFired(), is(4)); // cell, row, column, and table watchers fired
        
        // set a different cell, should only fire column and table listeners
        Cell cR2C1 = tbl.getCell(r2,  c1);
        cR2C1.setCellValue(12);
        assertThat(stl.getNumFired(), is(2));
        
        // try setting an odd number, should be blocked
        c.setCellValue(13);
        assertThat(c.getCellValue(), is(14));
        assertThat(stl.getNumFired(), is(1)); // event is thrown after first event       
        
        // set a different cell to odd, should only fire column and table listeners
        cR2C1 = tbl.getCell(r2,  c1);
        cR2C1.setCellValue(13);
        assertThat(cR2C1.getCellValue(), is(13));
        assertThat(stl.getNumFired(), is(2));
        
        // remove all listeners
        tbl.removeAllListeners();
        assertThat(tbl.hasListeners(), is(false));
        assertThat(r1.hasListeners(), is(false));
        assertThat(r2.hasListeners(), is(false));
        assertThat(c1.hasListeners(), is(false));
        assertThat(c.hasListeners(), is(false));
        assertThat(cR2C1.hasListeners(), is(false));
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(false));
        
        // install a onChange event
        c.addListeners(TableElementEventType.OnNewValue, stl);
        assertThat(tbl.hasListeners(), is(false));
        assertThat(c.hasListeners(), is(true));
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(true));
        c.setCellValue(15);
        c.setCellValue(14);
        c.setCellValue(15);
        c.setCellValue(14);
        synchronized (stl) {
            if (!stl.isFired())
                stl.wait();
        }
        
        assertThat(stl.getNumFired() > 0, is(true));
    }  
    @Test
    public void testOnBeforeDeleteEvent() throws InterruptedException
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r10 = tbl.addRow(Access.ByIndex, 10);      
        
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        assertThat(tbl.hasListeners(), is(false));
        assertThat(r1.hasListeners(), is(false));
        assertThat(r10.hasListeners(), is(false));
        assertThat(c1.hasListeners(), is(false));
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(false));
        
        DeleteRowListener stl = new DeleteRowListener();
        assertThat(stl.getNumFired(), is(0));
        
        tbl.addListeners(TableElementEventType.OnBeforeDelete, stl); 
        tbl.addListeners(TableElementEventType.OnDelete, stl); 
        assertThat(TableElementListeners.hasAnyListeners(tbl), is(true));
        assertThat(tbl.hasListeners(), is(true));
        assertThat(tbl.hasListeners(TableElementEventType.OnBeforeNewValue), is(false));
        assertThat(tbl.hasListeners(TableElementEventType.OnBeforeDelete), is(true));
        
        int numRows = tbl.getNumRows();
        assertThat(tbl.getNumRows(), is(numRows));
        
        // delete row 1, should be allowed
        r1.delete();
        assertThat(r1.isInvalid(), is(true));
        assertThat(tbl.getNumRows(), is(--numRows));
        
        // delete row 8 should not be allowed, even row index
        Row r8 = tbl.getRow(Access.ByIndex, 8);
        r8.delete();
        assertThat(r8.isInvalid(), is(false));
        assertThat(tbl.getNumRows(), is(numRows));
        
        // now block row 1 deletions
        r1 = tbl.getRow(Access.First);
        r1.addListeners(TableElementEventType.OnBeforeDelete, stl); 
        
        r1.delete();
        assertThat(r1.isInvalid(), is(false));
        assertThat(tbl.getNumRows(), is(numRows));

        // remove row listener, make sure r1 can be deleted
        r1.removeListeners(TableElementEventType.OnBeforeDelete);
        r1.delete();
        assertThat(r1.isInvalid(), is(true));
        assertThat(tbl.getNumRows(), is(--numRows));

        // remove all row listener, make sure r1 can be deleted
        tbl.removeAllListeners(); // should also remove row listeners
        
        Row r2 = tbl.getRow(Access.ByIndex, 2);
        r2.delete();
        assertThat(r2.isInvalid(), is(true));
        assertThat(tbl.getNumRows(), is(--numRows));
    }  
    
    private static class TableCellListener implements TableElementListener
    {
        private int m_fired;
        public TableCellListener() 
        {
            m_fired = 0;
        }

        synchronized public boolean isFired()
        {
            try {
                return m_fired > 0;
            }
            finally {
                notifyAll();
            }
        }

        @Override
        public void eventOccured(TableElementEvent e)
        {
            synchronized (this) {
                m_fired++;
                notifyAll();
            }
            
            // prevent odd numbers, if a Before event
            if (e.getSource() instanceof Cell) {
                CellValueChangedEvent cvce = (CellValueChangedEvent) e;
                System.out.println(String.format("%s Old: %s New %s", cvce, cvce.getOldValue(), cvce.getNewValue()));    
                
                if (cvce.isBefore() && cvce.isOldValueAvailable()) {
                    Object nv = cvce.getNewValue();
                    if (nv != null && nv instanceof Number) {
                        int iVal = (int)nv;
                        if (iVal % 2 != 0)
                            throw new BlockedRequestException(e);
                    }
                }
            }
            else
                System.out.println(e +  " " + isFired());                    
        }
        
        public int getNumFired()
        {
            int tmp = m_fired;
            m_fired = 0;
            return tmp;
        }        
    }  
    private static class DeleteRowListener implements TableElementListener
    {
        private int m_fired;
        public DeleteRowListener() 
        {
            m_fired = 0;
        }

        synchronized public boolean isFired()
        {
            try {
                return m_fired > 0;
            }
            finally {
                notifyAll();
            }
        }

        @Override
        public void eventOccured(TableElementEvent e)
        {
            synchronized (this) {
                m_fired++;
                notifyAll();
            }
            
            // prevent even row indexes from being deleted
            // if row is the source, don't allow row 1 to be deleted
            System.out.println(String.format("%s Fired: %b", e, isFired()));                    
            if (e.getSource() instanceof Row && e.getType() == TableElementEventType.OnBeforeDelete) {
                Row r = (Row)e.getSource();
                if ((r != null) && (r.getIndex() == 1))
                    throw new BlockedRequestException(e);
            }            
            else if (e.isTriggered() && e.getTrigger() instanceof Row && e.getType() == TableElementEventType.OnBeforeDelete) {
                Row r = (Row)e.getTrigger();
                if ((r != null) && (r.getIndex() % 2 == 0))
                    throw new BlockedRequestException(e);
            }            
        }
        
        public int getNumFired()
        {
            int tmp = m_fired;
            m_fired = 0;
            return tmp;
        }        
    }  
}
