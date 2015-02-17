package org.tms.teq;

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
        
        m_slope = m_intercept = m_r2 = Double.MIN_VALUE;
    }
    
    public int enter(double x, double y)
    {
        m_n++;
        int nX = m_statX.enter(x);
        int nY = m_statY.enter(y);        
        assert nX == m_n && nY == m_n;
        
        m_sumXY += x * y;
        
        m_slope = m_intercept = m_r2 = Double.MIN_VALUE;
        return m_n;
    }


    public void enter(Number x, Number y)
    {
        enter(x.doubleValue(), y.doubleValue());
    }
    
    public double calcStatistic(BuiltinOperator stat, Token... params)
    {        
        if (stat == BuiltinOperator.CountOper)
            return m_n;
        
        if (m_n <= 0)
            return Double.NaN;
        
        switch (stat) {
            case LinearSlopeOper:
                return m_slope = calculateSlope();
                
            case LinearInterceptOper:
                return calculateIntercept();
                
            case LinearCorrelationOper:
                return calculateCorrelation();
                
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
