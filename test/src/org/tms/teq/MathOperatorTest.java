package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
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
        c1.fill(100);
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill(500);
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill(10);
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill(22);
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill("abc");
        cR1C1.setDerivation("numberOf(col 1, 'abc')");
        assertThat(cR1C1.getCellValue(), is (499.0));
        
        cR1C1.setDerivation("numberOf(col 1, 50)");
        assertThat(cR1C1.getCellValue(), is (0.0));
        
        c1.fill(50);
        assertThat(cR1C1.getCellValue(), is (499.0));
        
        Cell cR1C2 = tbl.getCell(r1,  c2);
        assertThat(cR1C2, notNullValue());
        cR1C2.setDerivation("count(col 2)");
        
        Cell cR1C3 = tbl.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        cR1C3.setDerivation("mean(col 3)");
        
        c2.setDerivation("randomInt(col 1)");
        c3.setDerivation("normalize(col 2)");
        
        assertThat(cR1C1.isNumericValue(), is(true));
    }
}
