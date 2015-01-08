package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;

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

}
