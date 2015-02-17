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
        c = tbl.getCell(r9,  c1);
        assertThat(c, notNullValue());
        
        c7.setDerivation("12 + normalize(col 8)");
        
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
    
    @Test
    public void testFiveNumberSummaryStats()
    {
        Table t = TableFactory.createTable(12, 10);        
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = t.addRow();
        Row r2 = t.addRow();
        Row r3 = t.addRow();
        Row r4 = t.addRow();
        Row r5 = t.addRow();
        
        assertThat(r1.getIndex(), is(1));
        assertThat(r5.getIndex(), is(5));        
        
        Column c1 = t.addColumn();
        
        t.setCellValue(r1, c1, 12);
        t.setCellValue(r2, c1, 2);
        t.setCellValue(r3, c1, 9);
        t.setCellValue(r4, c1, 6);
        t.setCellValue(r5, c1, 5);
        
        Column c2 = t.addColumn();
        
        t.getCell(r1, c2).setDerivation("min(col 1)");
        t.getCell(r2, c2).setDerivation("firstQ(col 1)");
        t.getCell(r3, c2).setDerivation("median(col 1)");
        t.getCell(r4, c2).setDerivation("thirdQ(col 1)");
        t.getCell(r5, c2).setDerivation("max(col 1)");
        
        assertThat(t.getCellValue(r1, c2), is(2.0));
        assertThat(t.getCellValue(r2, c2), is(3.5));
        assertThat(t.getCellValue(r3, c2), is(6.0));
        assertThat(t.getCellValue(r4, c2), is(10.5));
        assertThat(t.getCellValue(r5, c2), is(12.0));
    }

    @Test
    public void testSkewnessKurtosisStats()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        Row r1 = t.addRow();
        Row r2 = t.addRow();
        Row r3 = t.addRow();
        Row r4 = t.addRow();
        Row r5 = t.addRow();
        Row r100 = t.addRow(Access.ByIndex, 100);
        
        assertThat(r1.getIndex(), is(1));
        assertThat(r5.getIndex(), is(5));
        assertThat(r100.getIndex(), is(100));      
        
        Column c1 = t.addColumn();
        c1.fill(61, 5, Access.First);
        c1.fill(64, 18, Access.Next);
        c1.fill(67, 42, Access.Next);
        c1.fill(70, 27, Access.Next);
        c1.fill(73, 8, Access.Next);
        
        // set statistics derivations
        Column c2 = t.addColumn();
        Cell c = t.getCell(r1, c2);
        c.setDerivation("count(col 1)");
        assertThat(c.getCellValue(), is(100.0));
        
        c = t.getCell(r2, c2);
        c.setDerivation("mean(col 1)");
        assertThat(c.getCellValue(), is(67.45));
        
        c = t.getCell(r3, c2);
        c.setDerivation("skew(col 1)");
        assertThat(closeTo(c.getCellValue(), -0.1098, 0.00001), is(true));
        
        c = t.getCell(r4, c2);
        c.setDerivation("kurt(col 1)");
        assertThat(closeTo(c.getCellValue(), -0.2091, 0.0001), is(true));
    }
    
    @Test
    public void testNormalDistributionStats()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        t.addRow(Access.ByIndex, 25000);
        
        Column c1 = t.addColumn();
        c1.setDerivation("normS(0, 1)");
        
        Column c2 = t.addColumn();
        
        Cell c = t.getCell(t.getRow(Access.First), c2);
        c.setDerivation("mean(col 1)");
        assertThat(closeTo(c.getCellValue(), 0.0, 0.05), is(true));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("stDev(col 1)");
        assertThat(closeTo(c.getCellValue(), 1.0, 0.01), is(true));
    }
    
    @Test
    public void testNDProbability()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        Row r1 = t.addRow(Access.First);
        
        Column c1 = t.addColumn();
        c1.setLabel("Mean");
        c1.fill(1000);
        
        Column c2 = t.addColumn();
        c2.setLabel("StDev");
        c2.fill(100);
        
        Column c3 = t.addColumn();
        c3.setLabel("NRV");
        c3.fill(1200);
        
        Column c4 = t.addColumn();
        c4.setLabel("Random CDF");
        c4.fill(.97725);
        
        Column c5 = t.addColumn();
        c5.setLabel("CDF");
        c5.setDerivation("normCDF(col 1, col 2, col 3)");
        Cell c = t.getCell(r1, c5);
        assertThat(closeTo(c.getCellValue(), 0.977, 0.001), is(true));
        
        Column c6 = t.addColumn();
        c6.setLabel("NV");
        c6.setDerivation("normInvCDF(col 1, col 2, col 4)");
        c = t.getCell(r1, c6);
        assertThat(closeTo(c.getCellValue(), 1200, 0.1), is(true));
        
        Row r2 = t.addRow(Access.Next);
        t.setCellValue(r2, c1, 50);
        t.setCellValue(r2, c2, 10);
        t.setCellValue(r2, c3, 63);
        t.setCellValue(r2, c4, 0.9030);
        assertThat(closeTo(t.getCellValue(r2, c5), 0.903, 0.001), is(true));
        assertThat(closeTo(t.getCellValue(r2, c6), 63, 0.1), is(true));
        
        Row r3 = t.addRow(Access.Next);
        t.setCellValue(r3, c1, 0);
        t.setCellValue(r3, c2, 1);
        t.setCellValue(r3, c3, 1.96);
        t.setCellValue(r3, c4, 0.975);
        assertThat(closeTo(t.getCellValue(r3, c5), 0.975, 0.001), is(true));
        assertThat(closeTo(t.getCellValue(r3, c6), 1.96, 0.01), is(true));
    }
    
    @Test
    public void testNDProbabilityInRange()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        Column c1 = t.addColumn();
        c1.setLabel("Mean");
        
        Column c2 = t.addColumn();
        c2.setLabel("StDev");
        
        Column c3 = t.addColumn();
        c3.setLabel("X0");
        
        Column c4 = t.addColumn();
        c4.setLabel("X1");
        
        Column c5 = t.addColumn();
        c5.setLabel("PIR");
        c5.setDerivation("normPIR(col 1, col 2, col 3, col 4)");
        
        t.setCellValue(t.addRow(), c1, 0);
        t.setCellValue(t.getRow(Access.Current), c2, 1);
        t.setCellValue(t.getRow(Access.Current), c3, -0.25);
        t.setCellValue(t.getRow(Access.Current), c4, 0.25);
        assertThat(closeTo(t.getCellValue(t.getRow(Access.Current), c5), 0.1974, 0.0001), is(true));
        
        t.setCellValue(t.addRow(), c1, 0);
        t.setCellValue(t.getRow(Access.Current), c2, 1);
        t.setCellValue(t.getRow(Access.Current), c3, 0.25);
        t.setCellValue(t.getRow(Access.Current), c4, 0.50);
        assertThat(closeTo(t.getCellValue(t.getRow(Access.Current), c5), 0.0928, 0.0001), is(true));
        
        t.setCellValue(t.addRow(), c1, 0);
        t.setCellValue(t.getRow(Access.Current), c2, 1);
        t.setCellValue(t.getRow(Access.Current), c3, 1.0);
        t.setCellValue(t.getRow(Access.Current), c4, 1.1);
        assertThat(closeTo(t.getCellValue(t.getRow(Access.Current), c5), 0.023, 0.0001), is(true));
        
        t.setCellValue(t.addRow(), c1, 20);
        t.setCellValue(t.getRow(Access.Current), c2, 4);
        t.setCellValue(t.getRow(Access.Current), c3, 15);
        t.setCellValue(t.getRow(Access.Current), c4, 22);
        assertThat(closeTo(t.getCellValue(t.getRow(Access.Current), c5), 0.5859, 0.0001), is(true));
        
        t.setCellValue(t.addRow(), c1, 50);
        t.setCellValue(t.getRow(Access.Current), c2, 0.05);
        t.setCellValue(t.getRow(Access.Current), c3, 49.9);
        t.setCellValue(t.getRow(Access.Current), c4, 50.1);
        assertThat(closeTo(t.getCellValue(t.getRow(Access.Current), c5), 0.9544, 0.0001), is(true));
    }
    
    @Test
    public void testTDistributionStats()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        t.addRow(Access.ByIndex, 25000);
        
        Column c1 = t.addColumn();
        c1.setDerivation("tS(500)");
        
        Column c2 = t.addColumn();
        
        Cell c = t.getCell(t.getRow(Access.First), c2);
        c.setDerivation("mean(col 1)");
        assertThat(closeTo(c.getCellValue(), 0.0, 0.01), is(true));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("stDev(col 1)");
        assertThat(closeTo(c.getCellValue(), 1.0, 0.02), is(true));
    }
    
    @Test
    public void testTProbability()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        Row r1 = t.addRow(Access.First);
        
        Column c1 = t.addColumn();
        c1.setLabel("DOF");
        c1.fill(13);
        
        Column c2 = t.addColumn();
        c2.setLabel("NRV");
        c2.fill(-0.4276);
        
        Column c3 = t.addColumn();
        c3.setLabel("Random CDF");
        c3.fill(0.338);
        
        Column c4 = t.addColumn();
        c4.setLabel("CDF");
        c4.setDerivation("tCDF(col 1, col 2)");
        Cell c = t.getCell(r1, c4);
        assertThat(closeTo(c.getCellValue(), 0.338, 0.001), is(true));
        
        Column c5 = t.addColumn();
        c5.setLabel("NV");
        c5.setDerivation("tInvCDF(col 1, col 3)");
        c = t.getCell(r1, c5);
        assertThat(closeTo(c.getCellValue(), -0.4276, 0.0001), is(true));
        
        Row r2 = t.addRow(Access.Next);
        t.setCellValue(r2, c1, 13);
        c = (Cell) t.getCell(r2, c2).setDerivation("tScore(20000, 19800, 1750, 14)");
        t.setCellValue(r2, c3, 0.338);
        assertThat(closeTo(t.getCellValue(r2, c4), 0.338, 0.001), is(true));
        assertThat(closeTo(t.getCellValue(r2, c5), -0.4276, 0.0001), is(true));
        
        Row r3 = t.addRow(Access.Next);
        t.setCellValue(r3, c1, 24);
        c = (Cell) t.getCell(r3, c2).setDerivation("tScore(112.1, 115, 11, 25)");
        t.setCellValue(r3, c3, 0.90);
        assertThat(closeTo(t.getCellValue(r3, c4), 0.90, 0.001), is(true));
        assertThat(closeTo(t.getCellValue(r3, c5), 1.3182, 0.001), is(true));
    }
    
    @Test
    public void testSingleTTest()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        
        Column c1 = t.addColumn();
        c1.setLabel("Data");        
        
        t.setCellValue(t.addRow(), c1, 7);
        t.setCellValue(t.addRow(), c1, 11);
        t.setCellValue(t.addRow(), c1, 2.3);
        t.setCellValue(t.addRow(), c1, 5);
        t.setCellValue(t.addRow(), c1, 9);
        t.setCellValue(t.addRow(), c1, 3);
        t.setCellValue(t.addRow(), c1, 11);
        t.setCellValue(t.addRow(), c1, 2);
        t.setCellValue(t.addRow(), c1, 5);
        t.setCellValue(t.addRow(), c1, 9);
        
        Column c2 = t.addColumn();    
        Cell c = t.getCell(t.getRow(Access.First), c2);
        c.setDerivation("count(col 1)");
        assertThat(c.getCellValue(), is(10.0));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("mean(col 1)");
        assertThat(c.getCellValue(), is(6.43));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("stDev(col 1)");
        assertThat(closeTo(c.getCellValue(), 3.4615, 0.0001), is(true));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("tValue(col 1, 10)");
        assertThat(closeTo(c.getCellValue(), -3.261351, 0.00001), is(true));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("pValue(col 1, 10)");
        assertThat(closeTo(c.getCellValue(), 0.009818, 0.00001), is(true));
        
        c = t.getCell(t.getRow(Access.Next), c2);
        c.setDerivation("tTest(col 1, 10, 0.01)");
        assertThat(c.getCellValue(), is(true));
    }
}
