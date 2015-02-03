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
        assertThat(te.calcStatistic(BuiltinOperator.CountOper), is(0.0));
        
        te.enter(-2, 0);
        n = te.enter(0, 1);
        assertThat(n, is(2));
        
        assertThat(te.calcStatistic(BuiltinOperator.LinearInterceptOper), is(1.0));
        assertThat(te.calcStatistic(BuiltinOperator.LinearSlopeOper), is(0.5));
        
        assertThat(te.calculateY(0), is(1.0));
        assertThat(te.calculateY(2), is(2.0));
        assertThat(te.calculateY(4), is(3.0));
        assertThat(te.calculateY(6), is(4.0));
        assertThat(te.calculateY(100), is(51.0));
        
        te.reset();
        assertThat(te.calcStatistic(BuiltinOperator.CountOper), is(0.0));
        
        te.enter(0, 0);
        te.enter(2, 2);
        te.enter(3, 3);
        te.enter(4, 4);
        n = te.enter(5, 6);
        assertThat(n, is(5));
        
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearInterceptOper),-0.216216, 0.000001), is(true));
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearSlopeOper), 1.1486486, 0.000001), is(true));
        assertThat(closeTo(te.calcStatistic(BuiltinOperator.LinearCorrelationOper), 0.9881049, 0.000001), is(true));      
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
        
        Cell cR3C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn()).setDerivation("r2(col Data::X, col Data::Y)");
        assertThat(closeTo(cR3C1.getCellValue(), 0.9881049, 0.000001), is(true));
        
        Cell cR4C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn());
        cR4C1.setCellValue(0.0);
        cR4C1.setLabel("xVal");
        
        Cell cR5C1 = (Cell)t2.getCell(t2.addRow(), t2.getColumn()).setDerivation("computeY(col Data::X, col Data::Y, cell xVal)");
        assertThat(closeTo(cR5C1.getCellValue(), (Double)cR2C1.getCellValue(), 0.000001), is(true));
        
        cR4C1.setCellValue(1.0);
        assertThat(closeTo(cR5C1.getCellValue(), (Double)cR1C1.getCellValue() + (Double)cR2C1.getCellValue(), 0.000001), is(true));
        
        tData.delete();
        assertThat(tData.isInvalid(), is(true));
        assertThat(c1.isInvalid(), is(true));
        assertThat(tData.getCurrentRow(), nullValue());
        assertThat(tData.getCurrentColumn(), nullValue());
        assertThat(cR1C1.isDerived(), is(false));
        assertThat(cR2C1.isDerived(), is(false));
        
        t2.delete();        
    }
}
