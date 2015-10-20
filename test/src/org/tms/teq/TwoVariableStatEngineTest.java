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
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class TwoVariableStatEngineTest extends BaseTest
{

    @Test
    public final void testStats()
    {
        TwoVariableStatEngine te = new TwoVariableStatEngine();
        assertThat(te, notNullValue());
        
        te.enter(0, 0);
        int n = te.enter(5, 5);
        assertThat(n, is(2));
    
        assertThat(te.calcStatistic(BuiltinOperator.LinearInterceptOper), is(0.0));
        assertThat(te.calcStatistic(BuiltinOperator.LinearSlopeOper), is(1.0));
        
        te.reset();
        assertThat(te.calcStatistic(BuiltinOperator.CountOper), is(0));
        
        te.enter(-2, 0);
        n = te.enter(0, 1);
        assertThat(n, is(2));
        
        double m = (double)te.calcStatistic(BuiltinOperator.LinearSlopeOper);
        double b = (double)te.calcStatistic(BuiltinOperator.LinearInterceptOper);
        assertThat(b, is(1.0));
        assertThat(m, is(0.5));
        
        assertThat(MathUtil.lrComputeY(m, b, 0), is(1.0));
        assertThat(MathUtil.lrComputeY(m, b, 2), is(2.0));
        assertThat(MathUtil.lrComputeY(m, b, 4), is(3.0));
        assertThat(MathUtil.lrComputeY(m, b, 6), is(4.0));
        assertThat(MathUtil.lrComputeY(m, b, 100), is(51.0));
        
        te.reset();
        assertThat(te.calcStatistic(BuiltinOperator.CountOper), is(0));
        
        te.enter(0, 0);
        te.enter(2, 2);
        te.enter(3, 3);
        te.enter(4, 4);
        n = te.enter(5, 6);
        assertThat(n, is(5));
        
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearInterceptOper),-0.216216, 0.000001), is(true));
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearSlopeOper), 1.1486486, 0.000001), is(true));
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearROper), 0.9881049, 0.000001), is(true));      
    }
    
    @Test
    public void linearRegressionTest()
    {
        Table tData = TableFactory.createTable();
        tData.setLabel("Data");
        Column c1 = tData.addColumn();
        c1.setLabel("X");
        
        Column c2 = tData.addColumn();
        c2.setLabel("Y");
        
        tData.setCellValue(tData.addRow(), c1, 0);
        tData.setCellValue(tData.getRow(Access.Current), c2, 0);
        
        tData.setCellValue(tData.addRow(), c1, 2);
        tData.setCellValue(tData.getRow(Access.Current), c2, 2);
        
        tData.setCellValue(tData.addRow(), c1, 3);
        tData.setCellValue(tData.getRow(Access.Current), c2, 3);
        
        tData.setCellValue(tData.addRow(), c1, 4);
        tData.setCellValue(tData.getRow(Access.Current), c2, 4);
        
        tData.setCellValue(tData.addRow(), c1, 5);
        tData.setCellValue(tData.getRow(Access.Current), c2, 6);
        
        Table t2 = TableFactory.createTable();
        Cell cR1C1 = (Cell)t2.getCell(t2.addRow(), t2.addColumn()).setDerivation("slope(col Data::X, col Data::Y)");
        assertThat(closeTo(cR1C1.getCellValue(), 1.1486486, 0.000001), is(true));
        
        Cell cR2C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn()).setDerivation("intercept(col Data::X, col Data::Y)");
        assertThat(closeTo(cR2C1.getCellValue(), -0.216216, 0.000001), is(true));
        
        Cell cR3C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn()).setDerivation("ccr(col Data::X, col Data::Y)");
        assertThat(closeTo(cR3C1.getCellValue(), 0.9881049, 0.000001), is(true));
        
        Cell cR4C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn());
        cR4C1.setCellValue(0.0);
        cR4C1.setLabel("xVal");
        
        Cell cR5C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn());
        cR5C1.setDerivation("computeY(slope(col Data::X, col Data::Y), intercept(col Data::X, col Data::Y), cell xVal)");
        assertThat(closeTo(cR5C1.getCellValue(), (Double)cR2C1.getCellValue(), 0.000001), is(true));
        
        cR4C1.setCellValue(1.0);
        assertThat(closeTo(cR5C1.getCellValue(), (Double)cR1C1.getCellValue() + (Double)cR2C1.getCellValue(), 0.000001), is(true));
        
        tData.delete();
        assertThat(tData.isInvalid(), is(true));
        assertThat(c1.isInvalid(), is(true));
        assertThat(tData.getRow(), nullValue());
        assertThat(tData.getColumn(), nullValue());
        assertThat(cR1C1.isDerived(), is(false));
        assertThat(cR2C1.isDerived(), is(false));
        
        t2.delete();        
    }
    
    @Test
    public void testTwoSampleTTest()
    {
        Table t = TableFactory.createTable();
        Column c1 = t.addColumn();
        c1.setLabel("X");
        t.setCellValue(t.addRow(), c1, 26);
        t.setCellValue(t.addRow(), c1, 21);
        t.setCellValue(t.addRow(), c1, 22);
        t.setCellValue(t.addRow(), c1, 26);
        t.setCellValue(t.addRow(), c1, 19);
        t.setCellValue(t.addRow(), c1, 22);
        t.setCellValue(t.addRow(), c1, 26); 
        t.setCellValue(t.addRow(), c1, 25); 
        t.setCellValue(t.addRow(), c1, 24); 
        t.setCellValue(t.addRow(), c1, 21); 
        t.setCellValue(t.addRow(), c1, 23); 
        t.setCellValue(t.addRow(), c1, 23); 
        t.setCellValue(t.addRow(), c1, 18); 
        t.setCellValue(t.addRow(), c1, 29); 
        t.setCellValue(t.addRow(), c1, 22); 
        t.pushCurrent();
        t.getCell(t.addRow(), c1).setDerivation("mean(col 1)");
        t.getCell(t.addRow(), c1).setDerivation("stDev(col 1)");
        t.getCell(t.addRow(), c1).setDerivation("ss(col 1)");
        t.getCell(t.addRow(), c1).setDerivation("count(col 1)");
        
        Column c2 = t.addColumn();
        c2.setLabel("Y");
        t.setCellValue(t.getRow(Access.First), c2, 18);
        t.setCellValue(t.getRow(Access.Next), c2, 23);
        t.setCellValue(t.getRow(Access.Next), c2, 21);
        t.setCellValue(t.getRow(Access.Next), c2, 20);
        t.setCellValue(t.getRow(Access.Next), c2, 20);
        t.setCellValue(t.getRow(Access.Next), c2, 29);
        t.setCellValue(t.getRow(Access.Next), c2, 20);
        t.setCellValue(t.getRow(Access.Next), c2, 16);
        t.setCellValue(t.getRow(Access.Next), c2, 20);
        t.setCellValue(t.getRow(Access.Next), c2, 26);
        t.setCellValue(t.getRow(Access.Next), c2, 21);
        t.setCellValue(t.getRow(Access.Next), c2, 25);
        t.setCellValue(t.getRow(Access.Next), c2, 17);
        t.setCellValue(t.getRow(Access.Next), c2, 18);
        t.setCellValue(t.getRow(Access.Next), c2, 19);
        
        t.popCurrent();
        t.getCell(t.getRow(Access.Next), c2).setDerivation("mean(col 2)");
        t.getCell(t.getRow(Access.Next), c2).setDerivation("stDev(col 2)");
        t.getCell(t.getRow(Access.Next), c2).setDerivation("ss(col 2)");
        t.getCell(t.getRow(Access.Next), c2).setDerivation("count(col 2)");
        
        Column c3 = t.addColumn();
        Cell c = (Cell) t.getCell(t.getRow(Access.First), c3).setDerivation("tsPValue(col 1, col 2)");
        
        c = (Cell) t.getCell(t.getRow(Access.Next), c3).setDerivation("tsTValue(col 1, col 2)");
        assertThat(closeTo(c.getCellValue(), 1.91, 0.01), is(true));
        
        c = (Cell) t.getCell(t.getRow(Access.Next), c3).setDerivation("tsTTest(col 1, col 2, 0.10)");
        assertThat(c.getCellValue(), is(true));
    }      
}
