package org.tms.teq;

import org.tms.api.exceptions.UnimplementedException;

public class TwoVariableStatEngine
{
    private SingleVariableStatEngine m_statX;
    private SingleVariableStatEngine m_statY;
    private double m_sumXY;
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
    }
    
    public int enter(double x, double y)
    {
        m_n++;
        int nX = m_statX.enter(x);
        int nY = m_statY.enter(y);        
        assert nX == m_n && nY == m_n;
        
        m_sumXY += x * y;
        
        return m_n;
    }


    public void enter(Number x, Number y)
    {
        enter(x.doubleValue(), y.doubleValue());
    }
    
    public double calcStatistic(BuiltinOperator stat)
    {        
        if (stat == BuiltinOperator.CountOper)
            return m_n;
        
        if (m_n <= 0)
            return Double.NaN;
        
        switch (stat) {
            case LinearSlopeOper:
                return calculateSlope();
                
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
        double sumX = m_statX.calcStatistic(BuiltinOperator.SumOper);
        double sumY = m_statY.calcStatistic(BuiltinOperator.SumOper);
        double sumX2 = m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
        double sumY2 = m_statY.calcStatistic(BuiltinOperator.Sum2Oper);
        return (m_n * m_sumXY - sumX*sumY)/
                Math.sqrt((m_n*sumX2 - sumX*sumX)*(m_n*sumY2 - sumY*sumY));
    }

    private double calculateIntercept()
    {
        double meanX = m_statX.calcStatistic(BuiltinOperator.MeanOper);
        double meanY = m_statY.calcStatistic(BuiltinOperator.MeanOper);
        
        return meanY - calculateSlope() * meanX;
    }

    protected double calculateSlope()
    {
        double sumX = m_statX.calcStatistic(BuiltinOperator.SumOper);
        double sumY = m_statY.calcStatistic(BuiltinOperator.SumOper);
        double sumX2 = m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
         
        return ((sumX*sumY/m_n) - m_sumXY)/((sumX*sumX/m_n) - sumX2);
    }
    
    protected double calculateSlope2()
    {
        double meanX = m_statX.calcStatistic(BuiltinOperator.MeanOper);
        double meanY = m_statY.calcStatistic(BuiltinOperator.MeanOper);
        double sumX2 = m_statX.calcStatistic(BuiltinOperator.Sum2Oper);
        
        return (m_sumXY - m_n*meanX*meanY)/(sumX2 - m_n*meanX*meanX);
    }

    public double calculateY(double x) 
    {
        return calculateSlope() * x + calculateIntercept();
    }
    
    public double calculateX(double y) 
    {
        return (y - calculateIntercept())/calculateSlope();
    }
}
