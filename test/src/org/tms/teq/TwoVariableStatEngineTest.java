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
    }

}
