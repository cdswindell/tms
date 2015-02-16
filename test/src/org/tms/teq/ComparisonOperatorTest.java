package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.factories.TableFactory;

public class ComparisonOperatorTest extends BaseTest
{
    @Test
    public void testEqualsOperator()
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.First);
        Row r2 = tbl.addRow(Access.Next);
        Row r3 = tbl.addRow(Access.Next);
        Row r4 = tbl.addRow(Access.Next);
        Row r5 = tbl.addRow(Access.Next);
        Row r7 = tbl.addRow(Access.Next);
        Row r6 = tbl.addRow(Access.Next);
        
        Column c1 = tbl.addColumn(Access.First);
        assertThat(c1, notNullValue());
        
        Column c2 = tbl.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        
        Column c3 = tbl.addColumn(Access.Next);
        assertThat(c3, notNullValue());        
        
        Column c4 = tbl.addColumn(Access.Next);
        assertThat(c4, notNullValue());
        c4.setDerivation("col 1=col 2");
               
        Column c5 = tbl.addColumn(Access.Next);
        assertThat(c5, notNullValue());
        c5.setDerivation("col 1== col 3");
        
        Column c6 = tbl.addColumn(Access.Next);
        assertThat(c6, notNullValue());
        c6.setDerivation("col 1!=col 3");
        
        tbl.setCellValue(r1, c1, 5);
        tbl.setCellValue(r1, c2, 5.0);
        tbl.setCellValue(r1, c3, 6.0);
        
        tbl.setCellValue(r2, c1, "abc");
        tbl.setCellValue(r2, c2, "abc");
        tbl.setCellValue(r2, c3, "def");
        
        tbl.setCellValue(r3, c1, true);
        tbl.setCellValue(r3, c2, true);
        tbl.setCellValue(r3, c3, false);
        
        tbl.setCellValue(r4, c1, 5);
        tbl.setCellValue(r4, c2, "5");
        tbl.setCellValue(r4, c3, 6.0);
        
        tbl.setCellValue(r5, c1, Thread.currentThread());
        tbl.setCellValue(r5, c2, Thread.currentThread());
        tbl.setCellValue(r5, c3, 6.0);
        
        tbl.setCellValue(r6, c1, true);
        tbl.setCellValue(r6, c2, "True");
        tbl.setCellValue(r6, c3, 6.0);
        
        tbl.setCellValue(r7, c1, null);
        tbl.setCellValue(r7, c2, null);
        tbl.setCellValue(r7, c3, 6.0);
        
        for (Row r : tbl.rows()) {
            assertThat(tbl.getCellValue(r,  c4), is(true));
            assertThat(tbl.getCellValue(r,  c5), is(false));
            assertThat(tbl.getCellValue(r,  c6), is(true));
        }        
    }    
    
    @Test
    public void testCompareOperator()
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.First);
        
        Column c1 = tbl.addColumn(Access.First);
        assertThat(c1, notNullValue());
        c1.fill(1);
        
        Column c2 = tbl.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        c2.fill(2);
        
        Column c3 = tbl.addColumn(Access.Next);
        assertThat(c3, notNullValue());  
        c3.fill(3);
        
        Column c4 = tbl.addColumn(Access.Next);
        assertThat(c4, notNullValue());
        c4.setDerivation("col 1 < col 2");
        assertThat(tbl.getCellValue(r1, c4), is(true));
               
        Column c5 = tbl.addColumn(Access.Next);
        assertThat(c5, notNullValue());
        c5.setDerivation("col 2 <= col 3");
        assertThat(tbl.getCellValue(r1, c5), is(true));
        
        Column c6 = tbl.addColumn(Access.Next);
        assertThat(c6, notNullValue());
        c6.setDerivation("col 3 > col 1");
        assertThat(tbl.getCellValue(r1, c6), is(true));
        
        Column c7 = tbl.addColumn(Access.Next);
        assertThat(c7, notNullValue());
        c7.setDerivation("col 3 <= col 1");
        assertThat(tbl.getCellValue(r1, c7), is(false));
        
        Column c8 = tbl.addColumn(Access.Next);
        assertThat(c8, notNullValue());
        c8.fill("Abc");
        
        Column c9 = tbl.addColumn(Access.Next);
        assertThat(c9, notNullValue());
        c9.fill("Def");
        
        Column c10 = tbl.addColumn(Access.Next);
        assertThat(c10, notNullValue());
        c10.setDerivation("col 8 < col 9");
        assertThat(tbl.getCellValue(r1, c10), is(true));
        
        Column c11 = tbl.addColumn(Access.Next);
        assertThat(c11, notNullValue());
        c11.setDerivation("col 9>=col 8");
        assertThat(tbl.getCellValue(r1, c11), is(true));
        
        Column c12 = tbl.addColumn(Access.Next);
        assertThat(c12, notNullValue());
        try {
            c12.setDerivation("col 1>=col 8");
            fail("Derivation succeeded");
        }
        catch (IllegalTableStateException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.Illegal));
        }
        catch (UnimplementedException e) {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.Unimplemented));
        }
    }

    @Test
    public void testIfOperator()
    {
        Table tbl = TableFactory.createTable();        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.First);
        Row r2 = tbl.addRow(Access.Next);
        
        Column c1 = tbl.addColumn(Access.First);
        assertThat(c1, notNullValue());
        c1.setDerivation("ridx");
        
        Column c2 = tbl.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        c2.fill("ABC");
        
        Column c3 = tbl.addColumn(Access.Next);
        assertThat(c3, notNullValue());  
        c3.fill("DEF");
        
        Column c4 = tbl.addColumn(Access.Next);
        assertThat(c4, notNullValue());
        
        c4.setDerivation("if((col 1 = 1), col 2, col 3)");
        assertThat(tbl.getCellValue(r1, c4), is("ABC"));
        assertThat(tbl.getCellValue(r2, c4), is("DEF"));
    }       

    @Test
    public void testLogicalsOperator()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = t.addRow(Access.First);
        Row r2 = t.addRow(Access.Next);
        Row r3 = t.addRow(Access.Next);
        Row r4 = t.addRow(Access.Next);
        
        Column c1 = t.addColumn(Access.First);
        assertThat(c1, notNullValue());
        
        Column c2 = t.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        
        t.setCellValue(r1,  c1,  true);
        t.setCellValue(r2,  c1,  true);
        t.setCellValue(r3,  c1,  false);
        t.setCellValue(r4,  c1,  false);
        
        t.setCellValue(r1,  c2,  true);
        t.setCellValue(r2,  c2,  false);
        t.setCellValue(r3,  c2,  true);
        t.setCellValue(r4,  c2,  false);
        
        Column c3 = t.addColumn(Access.Next);
        assertThat(c3, notNullValue());
        c3.setDerivation("col 1 and col 2");
        
        Column c4 = t.addColumn(Access.Next);
        assertThat(c4, notNullValue());
        c4.setDerivation("col 1 || col 2");
        
        Column c5 = t.addColumn(Access.Next);
        assertThat(c5, notNullValue());
        c5.setDerivation("col 1 xor col 2");
        
        Column c6 = t.addColumn(Access.Next);
        assertThat(c6, notNullValue());
        c6.setDerivation("~col 1");
        
        assertThat(t.getCellValue(r1, c3), is(true));
        assertThat(t.getCellValue(r2, c3), is(false));
        assertThat(t.getCellValue(r3, c3), is(false));
        assertThat(t.getCellValue(r4, c3), is(false));
        
        assertThat(t.getCellValue(r1, c4), is(true));
        assertThat(t.getCellValue(r2, c4), is(true));
        assertThat(t.getCellValue(r3, c4), is(true));
        assertThat(t.getCellValue(r4, c4), is(false));
        
        assertThat(t.getCellValue(r1, c5), is(false));
        assertThat(t.getCellValue(r2, c5), is(true));
        assertThat(t.getCellValue(r3, c5), is(true));
        assertThat(t.getCellValue(r4, c5), is(false));        
        
        assertThat(t.getCellValue(r1, c6), is(false));
        assertThat(t.getCellValue(r2, c6), is(false));
        assertThat(t.getCellValue(r3, c6), is(true));
        assertThat(t.getCellValue(r4, c6), is(true));
        
        // try again with int
        t.setCellValue(r1,  c1,  7);        
        t.setCellValue(r1,  c2,  6);

        assertThat(t.getCellValue(r1, c3), is(6.0));
        assertThat(t.getCellValue(r1, c4), is(7.0));
        assertThat(t.getCellValue(r1, c5), is(1.0));
        assertThat(t.getCellValue(r1, c6), is((double)(~7)));
    }       
}
