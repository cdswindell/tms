package org.tms.teq;

import org.apache.commons.math3.stat.inference.TTest;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.UnimplementedException;

public class TwoVariableStatEngine
{
    private SingleVariableStatEngine m_statX;
    private SingleVariableStatEngine m_statY;
    private double m_sumXY;
    private double m_slope = Double.MIN_VALUE; 
    private double m_intercept = Double.MIN_VALUE; 
    private double m_r2 = Double.MIN_VALUE;
    private int m_n;
    private boolean m_nonUniform;
    
    public TwoVariableStatEngine()
    {
        reset();
    }
    
    public void reset() 
    {
        m_statX = new SingleVariableStatEngine();
        m_statY = new SingleVariableStatEngine();
        
        m_sumXY = 0; 
        m_n = 0;
        m_nonUniform = false;
        
        m_slope = m_intercept = m_r2 = Double.MIN_VALUE;
    }
    
    public int enter(double x, double y)
    {
        int nX = m_statX.enter(x);
        int nY = m_statY.enter(y);   
        if (nX != nY)
            m_nonUniform = true;
        
        if (x == Double.MIN_VALUE || y == Double.MIN_VALUE)
            return m_n;
        m_n++;
        
        m_sumXY += x * y;
        
        m_slope = m_intercept = m_r2 = Double.MIN_VALUE;
        return m_n;
    }


    public void enter(Number x, Number y)
    {
        enter(x.doubleValue(), y.doubleValue());
    }
    
    protected boolean isNonUniform()
    {
        return this.m_nonUniform;
    }
    
    public Object calcStatistic(BuiltinOperator stat, Token... params)
    {        
        if (stat == BuiltinOperator.CountOper)
            return m_n;
        
        if (m_n <= 0)
            return Double.NaN;
        
        TTest tTest = null;
        switch (stat) {
            case LinearSlopeOper:
                return m_slope = calculateSlope();
                
            case LinearInterceptOper:
                return calculateIntercept();
                
            case LinearCorrelationOper:
                return calculateCorrelation();
                
            case TwoSamplePValueOper:
                if (m_n < 2 )
                    return Double.NaN;
                else {
                    tTest = new TTest();
                    return tTest.tTest(m_statX.getSummaryStatistics(), m_statY.getSummaryStatistics());
                }
                
            case TwoSampleTValueOper:
                if (m_n < 2)
                    return Double.NaN;
                else {
                    tTest = new TTest();
                    return tTest.t(m_statX.getSummaryStatistics(), m_statY.getSummaryStatistics());
                }
                
            case TwoSampleTTestOper:
                if (m_n < 2 || params == null || params.length < 1 || !params[0].isNumeric())
                    return Double.NaN;
                else {
                    double alpha = params[0].getNumericValue();
                    tTest = new TTest();
                    return tTest.tTest(m_statX.getSummaryStatistics(), m_statY.getSummaryStatistics(), alpha);
                }
                
            default:
                throw new UnimplementedException("Unsupported binary statistic: " + stat);            
        }
    }

    private double calculateCorrelation()
    {
        if (m_r2 == Double.MIN_VALUE) {
            double sumX = (double)m_statX.calcStatistic(BuiltinOperator.SumOper);
            double sumY = (double)m_statY.calcStatistic(BuiltinOperator.SumOper);
            double sumX2 = (double)m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
            double sumY2 = (double)m_statY.calcStatistic(BuiltinOperator.Sum2Oper);
            return m_r2 = (m_sumXY - sumX*sumY/m_n)/
                    Math.sqrt((sumX2 - sumX*sumX/m_n)*(sumY2 - sumY*sumY/m_n));
        }
        else 
            return m_r2;
    }

    private double calculateIntercept()
    {
        if (m_intercept == Double.MIN_VALUE) {
            double meanX = (double)m_statX.calcStatistic(BuiltinOperator.MeanOper);
            double meanY = (double)m_statY.calcStatistic(BuiltinOperator.MeanOper);
            
            return m_intercept = meanY - calculateSlope() * meanX;
        }
        else
            return m_intercept;
    }

    protected double calculateSlope()
    {
        if (m_slope == Double.MIN_VALUE) {
            double sumX = (double)m_statX.calcStatistic(BuiltinOperator.SumOper);
            double sumY = (double)m_statY.calcStatistic(BuiltinOperator.SumOper);
            double sumX2 = (double)m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
             
            return m_slope = ((sumX*sumY/m_n) - m_sumXY)/((sumX*sumX/m_n) - sumX2);
        }
        else
            return m_slope;
    }
    
    protected double calculateSlope2()
    {
        double meanX = (double)m_statX.calcStatistic(BuiltinOperator.MeanOper);
        double meanY = (double)m_statY.calcStatistic(BuiltinOperator.MeanOper);
        double sumX2 = (double)m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
        
        return (m_sumXY - m_n*meanX*meanY)/(sumX2 - m_n*meanX*meanX);
    }
}
