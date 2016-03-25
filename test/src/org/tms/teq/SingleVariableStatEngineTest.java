package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;

public class SingleVariableStatEngineTest extends BaseTest
{

    @Test
    public final void testSingleVariableStats()
    {
        SingleVariableStatEngine se = new SingleVariableStatEngine(true, false, null);
        assertThat(se, notNullValue());
        
        int n = se.enter(3096951.0, 1123560.0,5725983.0,918959.0,945761.0, 511297.0);
        assertThat(n, is(6));
    
        assertThat(se.calcStatistic(BuiltinOperator.CountOper), is(6));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.MeanOper), 2053751.8, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.StDevPopulationOper), 1840895.1, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.StDevSampleOper), 2016599.5, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.VarPopulationOper), 3.3888947439634e12, 1), is(true));
        assertThat(se.calcStatistic(BuiltinOperator.MinOper), is(511297.0));
        assertThat(se.calcStatistic(BuiltinOperator.MaxOper), is(5725983.0));
        assertThat(se.calcStatistic(BuiltinOperator.RangeOper), is(5725983.0 - 511297.0));
        assertThat(se.calcStatistic(BuiltinOperator.MedianOper), is(1034660.5));
        assertThat(se.calcStatistic(BuiltinOperator.FirstQuartileOper), is(918959.0));
        assertThat(se.calcStatistic(BuiltinOperator.ThirdQuartileOper), is(3096951.0));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.ModeOper), 2053751.8, 0.1), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.SkewOper), 1.586183, 0.000001), is(true));
        assertThat(closeTo(se.calcStatistic(BuiltinOperator.KurtosisOper), 1.90271, 0.00001), is(true));
    }
}
