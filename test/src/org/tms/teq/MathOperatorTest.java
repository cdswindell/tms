package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.factories.TableFactory;

public class MathOperatorTest extends BaseTest
{
    @Test
    public void testMathOperators()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);
        
        // extend table to 5000 rows
        tbl.addRow(Access.ByIndex, 5000);
        assertThat(tbl.getNumRows(), is(5000));
        
        Column c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, nullValue());
        
        Column c2 = tbl.addColumn(Access.ByIndex, 2);
        c2.fill(50);
        
        c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, notNullValue());
        
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        c3.setDerivation("randomInt(col 2)");
        
        Cell cR1C3 = tbl.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
        
        Cell cR1C1 = tbl.getCell(r1,  c1);
        cR1C1.setDerivation("numberOf(col 2, 50)");
        assertThat(cR1C1, notNullValue());
        assertThat(cR1C1.isNumericValue(), is(true));
        assertThat(cR1C1.getCellValue(), is(5000.0));
        
        Cell cR2C1 = tbl.getCell(r2,  c1);
        cR2C1.setDerivation("numberOf(col 3, 50)");
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
    }    
    
    @Test
    public void testLargeTable()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        
        // extend table to 5000 rows
        tbl.addRow(Access.ByIndex, 50000);
        assertThat(tbl.getNumRows(), is(50000));
        
        Column c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, nullValue());
        
        Column c2 = tbl.addColumn(Access.ByIndex, 2);
        c2.fill(50);
        
        c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, notNullValue());
        
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        c3.setDerivation("randomInt(col 2)");
        
        c1.fill(50);
        c2.setDerivation("randomInt(col 1)");
        c3.setDerivation("normalize(col 2)");
        
        Cell cR1C3 = tbl.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
    }
    
    @Test
    public void testDerivationPrecidence()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        
        // extend table to 500 rows
        tbl.addRow(Access.ByIndex, 500);
        assertThat(tbl.getNumRows(), is(500));
        
        Column c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, nullValue());
        
        Column c2 = tbl.addColumn(Access.ByIndex, 2);      
        c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, notNullValue());
        
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        
        Cell cR1C1 = tbl.getCell(r1,  c1);
        assertThat(cR1C1, notNullValue());
        
        cR1C1.setDerivation("numberOf(col 1, 50)");
        assertThat(cR1C1.getCellValue(), is (0.0));
        c1.fill(100);
        assertThat(cR1C1.getCellValue(), is (100));
        
        c1.fill(500);
        assertThat(cR1C1.getCellValue(), is (500));
        
        c1.fill(10);
        assertThat(cR1C1.getCellValue(), is (10));
        
        c1.fill(22);
        assertThat(cR1C1.getCellValue(), is (22));
        
        c1.fill("abc");
        cR1C1.setDerivation("numberOf(col 1, 'abc')");
        assertThat(cR1C1.getCellValue(), is (499.0));
        
        cR1C1.setDerivation("numberOf(col 1, 50)");
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill(50);
        assertThat(cR1C1.getCellValue(), is (50));
        
        Cell cR1C2 = tbl.getCell(r1,  c2);
        assertThat(cR1C2, notNullValue());
        cR1C2.setDerivation("count(col 2)");
        
        Cell cR1C3 = tbl.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        cR1C3.setDerivation("mean(col 3)");
        
        c2.setDerivation("randomInt(col 1)");
        c3.setDerivation("normalize(col 2)");
        
        assertThat(cR1C1.isNumericValue(), is(true));
        assertThat(cR1C1.getCellValue(), is (50));
        assertThat(cR1C2.getCellValue(), is (499.0));
        assertThat(closeTo(cR1C3.getCellValue(), 0, 0.000000000001), is (true));
        
        Column c4 = tbl.addColumn(Access.ByIndex, 4);
        c4.setDerivation("ridx");
        
        Column c5 = tbl.addColumn(Access.Next);
        assertThat(c5.getIndex(), is(5));
        c5.setDerivation("numberOf(col 2, col 4)");
        
        Cell cR1C5 = tbl.getCell(r1,  c5);
        assertThat(cR1C5, notNullValue());
        
        c2.sort();
        assertThat(cR1C5, notNullValue());
        
        // test Scale operation
        Column c6 = tbl.addColumn(Access.Last);
        assertThat(c6.getIndex(), is(6));
        c6.setDerivation("scale(col 2, 10, 20)");
        
        Column c7 = tbl.addColumn(Access.Next);
        assertThat(c7.getIndex(), is(7));
        
        Cell cR1C7 = tbl.getCell(r1,  c7);
        assertThat(cR1C7, notNullValue());
        cR1C7.setDerivation("min(col 6)");
        assertThat(cR1C7.getCellValue(), is (10.0));
        
        Row r2 = tbl.getRow(Access.ByIndex, 2);
        assertThat(r2, notNullValue());
        
        Cell cR2C7 = tbl.getCell(r2,  c7);
        assertThat(cR2C7, notNullValue());
        cR2C7.setDerivation("max(col 6)");
        assertThat(cR2C7.getCellValue(), is (20.0));
        
        List<Derivable> c1Affects = c1.getAffects();
        assertThat(c1Affects, notNullValue());
        
        c1.fill(100);
        assertThat(cR1C7.isPendings(), is(false));             
        assertThat(cR1C7, notNullValue());
        assertThat(cR1C7.getCellValue(), is (10.0));
    }
}
