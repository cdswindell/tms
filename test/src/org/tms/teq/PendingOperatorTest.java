package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.DerivableThreadPool;
import org.tms.api.Operator;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.tds.TableImpl;

public class PendingOperatorTest extends BaseTest
{
    @Test
    public final void testPendingTwoVariableStatEngine() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(1000);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        tm.registerOperator(new PendingOperator2());
        
        int numRows = 25000;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending2(col 1, 10)"); // c2
        Column c3 = (Column)t.addColumn(); // cell derivations column, c3   
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        // toss a sort in, just for fun
        c1.sort();
        
        Cell cR1C3 = t.getCell(t.getRow(Access.First), c3);
        cR1C3.setDerivation("slope(col 1, col 2)");
        
        Cell cR2C3 = t.getCell(t.getRow(Access.ByIndex, 2), c3);
        cR2C3.setDerivation("intercept(col 1, col 2)");
        
        Cell cR3C3 = t.getCell(t.getRow(Access.ByIndex, 3), c3);
        cR3C3.setDerivation("r2(col 1, col 2)");        
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(cR1C3.getCellValue(), is(2.0));
        assertThat(cR2C3.getCellValue(), is(0.0));
        assertThat(cR3C3.getCellValue(), is(1.0));
        
        double last = -1;
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            assertThat(v1 >= last, is(true));
            last = v1;
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(v1*2));
        }  
        
        ((DerivableThreadPool)tc).shutdown();
    }
        
    @Test
    public final void testPendingColumnOperatorSimple() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(10);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 250;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, 50)"); // c2
        Column c3 = (Column)t.addColumn(); // cell derivations column, c3        
        Column c4 = (Column)t.addColumn().setDerivation("normalize(col 2)"); // c4       
        
        Cell cR1C3 = t.getCell(t.getRow(Access.First), c3);
        cR1C3.setDerivation("mean(Col 4)");
        
        Cell cR2C3 = t.getCell(t.getRow(Access.ByIndex, 2), c3);
        cR2C3.setDerivation("max(Col 2)");
        
        Cell cR3C3 = t.getCell(t.getRow(Access.ByIndex, 3), c3);
        cR3C3.setDerivation("count(Col 2)");
        
        //assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(closeTo(cR1C3.getCellValue(), 0.0, 0.00000000001), is(true));
        assertThat(cR2C3.getCellValue(), is(100.0));
        assertThat(cR3C3.getCellValue(), is(numRows * 1.0));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(v1*2));
            
            c = t.getCell(r, c4);
            assertThat(c, notNullValue());
        }  
        
        ((DerivableThreadPool)tc).shutdown();
    }
        
    @Test
    public final void testPendingColumnOperator() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(1000);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 250;
        t.addRow(Access.ByIndex, numRows);
        
        int factor = 2;
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 500)"); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, 25) + col 2"); // c3
        Column c3 = (Column)t.addColumn(); // cell derivations column, c4        
        Column c4 = (Column)t.addColumn().setDerivation("normalize(col 3)"); // c45    
        
        assertThat(c1a.isValid(), is(true));
        
        Cell cR1C3 = t.getCell(t.getRow(Access.First), c3);
        cR1C3.setDerivation("median(col 2) + mean(Col 5) + 0 - median(col 2)");
        
        Cell cR2C3 = t.getCell(t.getRow(Access.ByIndex, 2), c3);
        cR2C3.setDerivation("max(Col 3)");
        
        Cell cR3C3 = t.getCell(t.getRow(Access.ByIndex, 3), c3);
        cR3C3.setDerivation("count(Col 3)");
        
        //assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(closeTo(cR1C3.getCellValue(), 0.0, 0.00000000001), is(true));
        assertThat(((double)cR2C3.getCellValue()>=.95 * 100.0 * factor), is(true));
        assertThat(cR3C3.getCellValue(), is(numRows * 1.0));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(v1*2 * factor));
            
            c = t.getCell(r, c4);
            assertThat(c, notNullValue());
        }       
        
        ((DerivableThreadPool)tc).shutdown();
    }
        
    @Test
    public final void testPendingBlockedPendingOperator() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 500;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 500)"); // c2, will block c3
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending(col 1, 50)/2"); // c3
        Column c4 = (Column)t.addColumn(); // cell derivations column, c5
        
        assertThat(c1a, notNullValue());
        
        Cell cR1C4 = t.getCell(t.getRow(Access.First), c4);
        cR1C4.setDerivation("mean(Col 3)");
        
        Cell cR2C4 = t.getCell(t.getRow(Access.Next), c4);
        cR2C4.setDerivation("max(Col 3)");
        
        Cell cR3C4 = t.getCell(t.getRow(Access.Next), c4);
        cR3C4.setDerivation("count(Col 3)");
        
        //assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(closeTo(cR1C4.getCellValue(), 145, 10.0), is(true));
        assertThat(cR2C4.getCellValue(), is(220.0));
        assertThat(cR3C4.getCellValue(), is(numRows * 1.0));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(7.0*5.0*2.0 + v1*2 + v1*2.0/2.0));
        }
        
        ((DerivableThreadPool)tc).shutdown();       
    }
    
    @Test
    public final void testPendingColumnOperatorComplex() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        tm.registerOperator(new PendingOperator2());
        
        int numRows = 2000;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, randInt(50) + 1)"); // c2, will block c3
        Column c3 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending2(col 1, randInt(100) + 1)/2"); // c3
        Column c4 = (Column)t.addColumn().setDerivation("col 3 - 70 - col 1 * 2"); // c4
        Column c5 = (Column)t.addColumn(); // cell derivations column, c5        
        Column c6 = (Column)t.addColumn().setDerivation("normalize(col 3)"); // c6
        
        assertThat(c2, notNullValue());
        assertThat(c6, notNullValue());
        
        Cell cR1C5 = t.getCell(t.getRow(Access.First), c5);
        cR1C5.setDerivation("mean(col 2) + mean(Col 6) - mean(col 2)");
        
        Cell cR2C5 = t.getCell(t.getRow(Access.Next), c5);
        assertThat(cR2C5, notNullValue());
        assertThat(cR2C5.getRow().getIndex(), is(2));
        cR2C5.setDerivation("max(Col 3)");
        
        Cell cR3C5 = t.getCell(t.getRow(Access.Next), c5);
        assertThat(cR3C5, notNullValue());
        assertThat(cR3C5.getRow().getIndex(), is(3));
        cR3C5.setDerivation("count(Col 3)");
        
        Row curRow = t.getCurrentRow();
        assertThat(curRow.getIndex(), is(3));
        
        Column curCol = t.getCurrentColumn();        
        assertThat(curCol.getIndex(), is(5));
        
        assertThat(((TableImpl)t).isPendings(), is(true));          
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getCurrentColumn(), is(curCol));
        assertThat(t.getCurrentRow(), is(curRow));
        
        assertThat(cR1C5.getCellValue(), is(0.0));
        assertThat(cR2C5.getCellValue(), is(220.0));
        assertThat(cR3C5.getCellValue(), is(numRows * 1.0));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c3);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(7.0*5.0*2.0 + v1*2 + v1*2.0/2.0));
            
            c = t.getCell(r, c4);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(v1));
        }
        
        // try again, with more threads
        ((DerivableThreadPool)tc).setMaximumPoolSize(1000);
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        t.recalculate();
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        ((DerivableThreadPool)tc).shutdown();
    }
    
    @Test
    public final void testDeleteRowsWhilePending() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 2000;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)"); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 50)"); // c2, will block c3
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending(col 1, 50 + col 2)/2"); // c3
        Column c3 = (Column)t.addColumn().setDerivation("col 3 - 70 - col 1 * 2"); // c4
        Column c4 = (Column)t.addColumn(); // cell derivations column, c5
        
        Column c5 = (Column)t.addColumn().setDerivation("normalize(col 3)"); // c6
        
        // delete some rows
        t.getRow(Access.ByIndex, 1800).delete();
        t.getRow(Access.ByIndex, 1832).delete();
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        assertThat(c1a, notNullValue());
        assertThat(c5, notNullValue());
        
        Cell cR1C4 = t.getCell(t.getRow(Access.First), c4);
        cR1C4.setDerivation("mean(col 2) + mean(Col 6) - mean(col 2)");
        
        Cell cR2C4 = t.getCell(t.getRow(Access.Next), c4);
        assertThat(cR2C4, notNullValue());
        assertThat(cR2C4.getRow().getIndex(), is(2));
        cR2C4.setDerivation("max(Col 3)");
        
        Cell cR3C4 = t.getCell(t.getRow(Access.Next), c4);
        assertThat(cR3C4, notNullValue());
        assertThat(cR3C4.getRow().getIndex(), is(3));
        cR3C4.setDerivation("count(Col 3)");
              
        Row curRow = t.getCurrentRow();
        Column curCol = t.getCurrentColumn();
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getCurrentRow(), is(curRow));
        assertThat(t.getCurrentColumn(), is(curCol));
        
        numRows = t.getNumRows();
        assertThat(closeTo(cR1C4.getCellValue(), 0.0, 0.00000000001), is(true));
        assertThat(cR2C4.getCellValue(), is(220.0));
        assertThat(cR3C4.getCellValue(), is(numRows * 1.0));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(7.0*5.0*2.0 + v1*2 + v1*2.0/2.0));
            
            c = t.getCell(r, c3);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(v1));
        }
        
        // try again, with more threads
        ((DerivableThreadPool)tc).setMaximumPoolSize(500);
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        t.recalculate();
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        ((DerivableThreadPool)tc).shutdown();
    }
    
    @Test
    public final void testClearDerivationWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(500);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 10000);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2");
        t.addColumn().setDerivation("col 2 - 70");
        Column c4 = (Column)t.addColumn(); // cell derivations column
        
        t.addColumn().setDerivation("normalize(col 2)"); // c5
        
        Cell cR1C4 = t.getCell(t.getRow(Access.First), c4);
        cR1C4.setDerivation("mean(Col 5)");
        
        Cell cR2C4 = t.getCell(t.getRow(Access.Next), c4);
        assertThat(cR2C4.getRow().getIndex(), is(2));
        cR2C4.setDerivation("max(Col 2)");
        
        Cell cR3C4 = t.getCell(t.getRow(Access.Next), c4);
        assertThat(cR3C4.getRow().getIndex(), is(3));
        cR3C4.setDerivation("count(Col 2)");
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        Row curRow = t.getCurrentRow();
        Column curCol = t.getCurrentColumn();
        
        // clear derivation
        c2.clearDerivation();
  
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getCurrentRow(), is(curRow));
        assertThat(t.getCurrentColumn(), is(curCol));
              
        assertThat(cR1C4.isNull(), is(true));
        assertThat(cR2C4.isNull(), is(true));
        assertThat(cR3C4.isNull(), is(true));
                
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat((c.isNull() || c.getCellValue().equals(7.0*5.0*2.0 + v1*2.0/2.0)), is(true));
        }
        
        // repeat test, only use fill rather than clear
        c2.setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2");
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        c2.fill(50);
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        for (Row r : t.rows()) {
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(50));
        }
        
        ((DerivableThreadPool)tc).shutdown();
    }
    
    @Test
    public final void testDeleteColumnWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(500);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 10000);
        
        t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2");
        Column c3 = (Column)t.addColumn().setDerivation("col 2 - 70");
        Column c4 = (Column)t.addColumn(); // cell derivations column
        
        t.addColumn().setDerivation("normalize(col 2)"); // c5
        
        Cell cR1C4 = t.getCell(t.getRow(Access.First), c4);
        cR1C4.setDerivation("mean(Col 5)");
        
        Cell cR2C4 = t.getCell(t.getRow(Access.Next), c4);
        assertThat(cR2C4.getRow().getIndex(), is(2));
        cR2C4.setDerivation("max(Col 2)");
        assertThat(cR2C4.getRow().getIndex(), is(2));
       
        Cell cR3C4 = t.getCell(t.getRow(Access.Next), c4);
        cR3C4.setDerivation("count(Col 2)");
        assertThat(cR3C4.getRow().getIndex(), is(3));
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        // delete column
        c2.delete();
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(cR1C4.isNull(), is(true));
        assertThat(cR2C4.isNull(), is(true));
        assertThat(cR3C4.isNull(), is(true));
                
        for (Row r : t.rows()) {
            Cell c = t.getCell(r, c3);
            assertThat(c, notNullValue());
            assertThat(c.isNull(), is(true));
        }
        
        ((DerivableThreadPool)tc).shutdown();
    }
    
    @Test
    public final void testDeleteTableWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((DerivableThreadPool)tc).setMaximumPoolSize(500);
        ((DerivableThreadPool)tc).setKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 10000);
        
        t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2");
        Column c3 = (Column)t.addColumn().setDerivation("col 2 - 70");
        Column c4 = (Column)t.addColumn(); // cell derivations column
        
        t.addColumn().setDerivation("normalize(col 2)"); // c5
        
        Cell cR1C4 = t.getCell(t.getRow(Access.First), c4);
        cR1C4.setDerivation("mean(Col 5)");
        
        Cell cR2C4 = t.getCell(t.getRow(Access.Next), c4);
        cR2C4.setDerivation("max(Col 2)");
        
        Cell cR3C4 = t.getCell(t.getRow(Access.Next), c4);
        cR3C4.setDerivation("count(Col 2)");
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        // delete column
        t.delete();
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.isInvalid(), is(true));
        assertThat(c2.isInvalid(), is(true));
        assertThat(c3.isInvalid(), is(true));
        assertThat(c4.isInvalid(), is(true));
        assertThat(cR1C4.isInvalid(), is(true));
        assertThat(cR2C4.isInvalid(), is(true));
        assertThat(cR3C4.isInvalid(), is(true));
        
        ((DerivableThreadPool)tc).shutdown();
    }
    
    public class PendingOperator implements Operator, Runnable
    {
        private Token[] m_args;
        private UUID m_transId;
        
        public PendingOperator(UUID transId, Token[] args)
        {
            m_args = args;
            m_transId = transId;
        }

        public PendingOperator()
        {
            m_args = null;
        }

        @Override
        public void run()
        {
            int sleepTime = m_args[1].getNumericValue().intValue();
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                System.out.println(Thread.currentThread().getName() + " interrupted...");
            }
            catch (Exception e)
            {
                System.out.println(Thread.currentThread().getName() + " exception...");
            }
            
            double rslt = 2.0 * (Double)m_args[0].getValue();
            m_args[0].postResult(rslt);
        }

        @Override
        public String getLabel()
        {
            return "pending";
        }

        @Override
        public TokenType getTokenType()
        {
            return TokenType.BinaryFunc;
        }

        @Override
        public Class<?>[] getArgTypes()
        {
             return new Class<?>[] {double.class, double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {   
            PendingOperator po = new PendingOperator(Derivation.getTransactionID(), args);
            return Token.createPendingToken(po);
        }  
        
        public UUID getTransactionId()
        {
            return this.m_transId;
        }
    }
    
    public class PendingOperator2 implements Operator, Runnable
    {
        private Token[] m_args;
        private UUID m_transId;
        
        public PendingOperator2(UUID transId, Token[] args)
        {
            m_args = args;
            m_transId = transId;
        }

        public PendingOperator2()
        {
            m_args = null;
        }

        @Override
        public void run()
        {
            int sleepTime = m_args[1].getNumericValue().intValue();
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                System.out.println(Thread.currentThread().getName() + " interrupted...");
            }
            catch (Exception e)
            {
                System.out.println(Thread.currentThread().getName() + " exception...");
            }
            
            double rslt = 2.0 * (Double)m_args[0].getValue();
            Derivation.postResult(getTransactionId(), rslt);
        }

        @Override
        public String getLabel()
        {
            return "pending2";
        }

        @Override
        public TokenType getTokenType()
        {
            return TokenType.BinaryFunc;
        }

        @Override
        public Class<?>[] getArgTypes()
        {
             return new Class<?>[] {double.class, double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {   
            PendingOperator2 po = new PendingOperator2(Derivation.getTransactionID(), args);
            return Token.createPendingToken(po);
        }  
        
        public UUID getTransactionId()
        {
            return this.m_transId;
        }
    }
}
