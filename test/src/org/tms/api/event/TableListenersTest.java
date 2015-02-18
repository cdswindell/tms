package org.tms.api.event;

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
import org.tms.api.event.exceptions.BlockedRequestException;
import org.tms.api.factories.TableFactory;

public class TableListenersTest extends BaseTest
{
    @Test
    public void testOnBeforeCellValueChangedEvent()
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);      
        
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        TableCellListener stl = new TableCellListener();
        assertThat(stl.getNumFired(), is(0));
        
        tbl.addListener(TableElementEventType.OnBeforeNewValue, stl);        
        r1.addListener(TableElementEventType.OnBeforeNewValue, stl);
        c1.addListener(TableElementEventType.OnBeforeNewValue, stl);
        
        // set cell, cell listener should fire 3 times
        Cell c = tbl.getCell(r1,  c1);
        c.setCellValue(12);
        assertThat(stl.getNumFired(), is(3));

        // set the cell listener, slightly different event firing path
        c.addListener(TableElementEventType.OnBeforeNewValue, stl);
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
    }  
    
    private static class TableCellListener implements TableElementListener
    {
        private int m_fired;
        public TableCellListener() 
        {
            m_fired = 0;
        }

        @Override
        public void eventOccured(TableElementEvent e)
        {
            m_fired++;
            System.out.println(e);    
            
            // prevent odd numbers
            if (e.getSource() instanceof Cell) {
                CellValueChangedEvent cvce = (CellValueChangedEvent) e;
                
                if (cvce.isBefore()) {
                    Object nv = cvce.getNewValue();
                    if (nv != null && nv instanceof Number) {
                        int iVal = (int)nv;
                        if (iVal % 2 != 0)
                            throw new BlockedRequestException(e);
                    }
                }
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
