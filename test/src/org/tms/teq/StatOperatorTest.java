package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
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
import org.tms.tds.TableImpl;

public class StatOperatorTest extends BaseTest
{
    @Test
    public void testSingleVariableStats()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);
        Row r3 = tbl.addRow(Access.ByIndex, 3);
        Row r4 = tbl.addRow(Access.ByIndex, 4);
        Row r5 = tbl.addRow(Access.ByIndex, 5);
        Row r6 = tbl.addRow(Access.ByIndex, 6);
        Row r7 = tbl.addRow(Access.ByIndex, 7);
        Row r8 = tbl.addRow(Access.ByIndex, 8);
        Row r9 = tbl.addRow(Access.ByIndex, 9);
        Row r10 = tbl.addRow(Access.ByIndex, 10);
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r15 = tbl.addRow(Access.ByIndex, 15);
        assertThat(r15, notNullValue());
        
        Column c8 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c7 = tbl.getColumn(Access.ByIndex, 7);
        c7.setDerivation("meanCenter(col 8)");
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows() * 2));
        
        Column c6 = tbl.getColumn(Access.ByIndex, 6);
        c6.setDerivation("normalize(col 8)");
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows() * 3));
        
        c8.fill(42);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows() * 3));
        
        // mean oper
        Cell c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // max oper
        c = tbl.getCell(r2,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("max(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // min oper
        c = tbl.getCell(r3,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("min(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // stdev oper
        c = tbl.getCell(r4,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("StDevSample(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(0.0));
        
        // count oper
        c = tbl.getCell(r5,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("count(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(0.0d + tbl.getNumRows()));
        
        // median oper
        c = tbl.getCell(r7,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("median(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(42.0));
        
        // spread oper
        c = tbl.getCell(r6,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("spread(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(0.0));
        
        // normalize oper
        c = tbl.getCell(r8,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(col 6)");
        assertThat(c.isNumericValue(), is(false));
        assertThat(c.isErrorValue(), is(true));
        
        // meanCenter oper
        c = tbl.getCell(r9,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(col 7)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(0.0));
        
        // Set less random variables
        c8.clear();
        
        // recalculate statistic, should cause cell to 
        // be set to error
        c.recalculate();
        assertThat(c.isErrorValue(), is(true));
        assertThat(c.getErrorCode(), is(ErrorCode.NaN));
        
        // set some data to known values to test stat calculation
        ((TableImpl)tbl).deactivateAutoRecalculate();
        
        tbl.setCellValue(r1, c8, 3.68);
        tbl.setCellValue(r2, c8, 1.28);
        tbl.setCellValue(r3, c8, 1.84);
        tbl.setCellValue(r4, c8, 3.68);
        tbl.setCellValue(r5, c8, 1.83);
        tbl.setCellValue(r6, c8, 6.0);
        ((TableImpl)tbl).activateAutoRecalculate();
        
        tbl.recalculate();
        
        // mean oper
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(closeTo(c.getCellValue(), 3.0517, 0.0001), is(true));
        
        // max oper
        c = tbl.getCell(r2,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(6.0));
        
        // min oper
        c = tbl.getCell(r3,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(1.28));     
        
        // stDev oper
        c = tbl.getCell(r4,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(closeTo(c.getCellValue(), 1.7653, 0.0001), is(true));
        
        // do some more positive testing
        c7.setDerivation("12 + normalize(col 8)");
        c = tbl.getCell(r9,  c1);
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(12.0));
        
        // do some more positive testing
        c7.setDerivation("normalize(col 8) + hypot(3,4) * 2");
        c = tbl.getCell(r9,  c1);
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(10.0));
        
        // do some more positive testing
        c7.setDerivation("normalize(col 8) + hypot(3,4) ");
        c = tbl.getCell(r9,  c1);
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(5.0));
        
        // disable precision, value should be slightly different than 5.0
        ((TableImpl)tbl).setPrecision(-1);
        c7.setDerivation("normalize(col 8) + hypot(3,4) ");
        c = tbl.getCell(r9,  c1);
        c.setDerivation("mean(col 7)");
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), not(5.0));        
        
        // reset precision, value should now be 5.0
        ((TableImpl)tbl).setPrecision(Integer.MAX_VALUE);
        c7.setDerivation("normalize(col 8) + hypot(3,4) ");
        c = tbl.getCell(r9,  c1);
        c.setDerivation("mean(col 7)");
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(5.0)); 
        
        // test with very small numbers, make sure all is well
        ((TableImpl)tbl).deactivateAutoRecalculate();
        
        tbl.setCellValue(r1, c8, 3.68e-30);
        tbl.setCellValue(r2, c8, 1.28e-30);
        tbl.setCellValue(r3, c8, 1.84e-30);
        tbl.setCellValue(r4, c8, 3.68e-30);
        tbl.setCellValue(r5, c8, 1.83e-30);
        tbl.setCellValue(r6, c8, 6.0e-30);
        
        ((TableImpl)tbl).activateAutoRecalculate();
        
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(closeTo(c.getCellValue(), 3.0517e-30, 0.0001e-30), is(true));
        
        c7.setDerivation("normalize(col 8) + hypot(3,4) ");
        c = tbl.getCell(r9,  c1);
        c.setDerivation("mean(col 7)");
        assertThat(c, notNullValue());
        
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(5.0)); 
        
        c = tbl.getCell(r10,  c1);
        assertThat(c, notNullValue());
        
        c.setDerivation("stDev(col 7)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(1.0));        
    }    
}