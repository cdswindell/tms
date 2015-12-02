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
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.tds.ContextImpl;
import org.tms.tds.TableImpl;

public class PendingOperatorTest extends BaseTest
{
    @Test
    public final void testPendingTwoVariableStatEngine() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(1000);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        tm.registerOperator(new PendingOperator2());
        
        int numRows = 2500;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending2(col 1, 10)").getTarget(); // c2
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
        
    @Test
    public final void testPendingColumnOperatorSimple() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(10);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 500;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, 50)").getTarget(); // c2
        Column c3 = (Column)t.addColumn(); // cell derivations column, c3        
        Column c4 = (Column)t.addColumn().setDerivation("normalize(col 2)").getTarget(); // c4       
        
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
        
    @Test
    public final void testPendingColumnOperator() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(1000);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 250;
        t.addRow(Access.ByIndex, numRows);
        
        int factor = 2;
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 500)").getTarget(); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, 25) + col 2").getTarget(); // c3
        Column c3 = (Column)t.addColumn(); // cell derivations column, c4        
        Column c4 = (Column)t.addColumn().setDerivation("normalize(col 3)").getTarget(); // c45    
        
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
        
        ((ContextImpl)tc).shutdownDerivableThreadPool();
    }
        
    @Test
    public final void testPendingBlockedPendingOperator() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 500;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 500)").getTarget(); // c2, will block c3
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending(col 1, 50)/2").getTarget(); // c3
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();       
    }
    
    @Test
    public final void testPendingColumnOperatorComplex() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        tm.registerOperator(new PendingOperator2());
        
        int numRows = 1000;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, randInt(50) + 1)").getTarget(); // c2, will block c3
        Column c3 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending2(col 1, randInt(100) + 1)/2").getTarget(); // c3
        Column c4 = (Column)t.addColumn().setDerivation("col 3 - 70 - col 1 * 2").getTarget(); // c4
        Column c5 = (Column)t.addColumn(); // cell derivations column, c5        
        Column c6 = (Column)t.addColumn().setDerivation("normalize(col 3)").getTarget(); // c6
        assertThat(((TableImpl)t).isPendings(), is(true));          
        
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
        
        Row curRow = t.getRow();
        assertThat(curRow.getIndex(), is(3));
        
        Column curCol = t.getColumn();        
        assertThat(curCol.getIndex(), is(5));
        
        assertThat(((TableImpl)t).isPendings(), is(true));          
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getColumn(), is(curCol));
        assertThat(t.getRow(), is(curRow));
        
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
        ((ContextImpl)tc).setPendingMaximumPoolSize(1000);
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        t.recalculate();
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
    
    @Test
    public final void testDeleteRowsWhilePending() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        int numRows = 1000;
        t.addRow(Access.ByIndex, numRows);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget(); // c1
        Column c1a = (Column)t.addColumn().setDerivation("pending(col 1, 50)").getTarget(); // c2, will block c3
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + col 2 + pending(col 1, 50 + col 2)/2").getTarget(); // c3
        Column c3 = (Column)t.addColumn().setDerivation("col 3 - 70 - col 1 * 2").getTarget(); // c4
        Column c4 = (Column)t.addColumn(); // cell derivations column, c5        
        Column c5 = (Column)t.addColumn().setDerivation("normalize(col 3)").getTarget(); // c6
        
        // delete some rows while calculations are pending
        t.getRow(Access.ByIndex, 800).delete();
        t.getRow(Access.ByIndex, 832).delete();
        
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
              
        Row curRow = t.getRow();
        Column curCol = t.getColumn();
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }        
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getRow(), is(curRow));
        assertThat(t.getColumn(), is(curCol));
        
        numRows = t.getNumRows();
        if (!cR1C4.isErrorValue())
        	assertThat(String.format("Expected: 0, Observed: %s", cR2C4.getCellValue()), cR1C4.getCellValue(), is(0.0));
        assertThat(String.format("Expected: 220, Observed: %s", cR2C4.getCellValue()), cR2C4.getCellValue(),  is(220.0));
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
        ((ContextImpl)tc).setPendingMaximumPoolSize(500);
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        t.recalculate();
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
    
    @Test
    public final void testClearDerivationWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(500);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 1000);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)").getTarget();
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2").getTarget();
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
        
        Row curRow = t.getRow();
        Column curCol = t.getColumn();
        
        // clear derivation
        c2.clearDerivation();
  
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        assertThat(t.getRow(), is(curRow));
        assertThat(t.getColumn(), is(curCol));
              
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
    
    @Test
    public final void testDeleteColumnWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(500);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 10000);
        
        t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2").getTarget();
        Column c3 = (Column)t.addColumn().setDerivation("col 2 - 70").getTarget();
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
    }
    
    @Test
    public final void testDeleteTableWhilePending() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        ((ContextImpl)tc).setPendingMaximumPoolSize(500);
        ((ContextImpl)tc).setPendingKeepAliveTime(100, TimeUnit.MILLISECONDS);
        
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 10000);
        
        t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 5000) + pending(col 1, 50)/2").getTarget();
        Column c3 = (Column)t.addColumn().setDerivation("col 2 - 70").getTarget();
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
        
        ((DerivableThreadPool)tc).shutdownDerivableThreadPool();
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
        public Class<?> getResultType()
        {
            return double.class;
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
            PendingOperator po = new PendingOperator(DerivationImpl.getTransactionID(), args);
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
        
        @Override
        public Class<?> getResultType()
        {
            return double.class;
        }
        
        public UUID getTransactionId()
        {
            return this.m_transId;
        }
    }
}
