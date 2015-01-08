package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;

public class SingleVariableStatEngineTest extends BaseTest
{

    @Test
    public final void testStats()
    {
        SingleVariableStatEngine se = new SingleVariableStatEngine();
        assertThat(se, notNullValue());
        
        int n = se.enter(3096951.0, 1123560.0,5725983.0,918959.0,945761.0, 511297.0);
        assertThat(n, is(6));
    
        assertThat(se.calcStatistic(BuiltinOperator.CountOper), is(6.0));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.MeanOper), 2053751.8, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.StDevOper), 1840895.1, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.StDevSampleOper), 2016599.5, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.VarOper), 3.3888947439634e12, 1), is(true));
        assertThat(se.calcStatistic(BuiltinOperator.MinOper), is(511297.0));
        assertThat(se.calcStatistic(BuiltinOperator.MaxOper), is(5725983.0));
        assertThat(se.calcStatistic(BuiltinOperator.RangeOper), is(5725983.0 - 511297.0));
    }

}
