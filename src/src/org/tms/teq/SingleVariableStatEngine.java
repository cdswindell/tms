package org.tms.teq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tms.api.exceptions.UnimplementedException;

public class SingleVariableStatEngine
{
    private int m_n;
    private double m_sumX;
    private double m_sumX2;
    private double m_min;
    private double m_max;
    private boolean m_retainDataset;
    private List<Double> m_values;
    
    public SingleVariableStatEngine()
    {
        reset();
        m_retainDataset = false;
    }
    
    public SingleVariableStatEngine(boolean retainDataset)
    {
        reset();
        m_retainDataset = retainDataset;
        
        if (m_retainDataset)
            m_values = new ArrayList<Double>();
    }
    
    public void reset() 
    {
        m_n = 0;
        m_sumX = m_sumX2 = 0;
        m_min = Double.POSITIVE_INFINITY;
        m_max = Double.NEGATIVE_INFINITY;
    }

    public int enter(Double... vals) 
    {
        for (Double d : vals) {
            if (d != null)
                enter(d.doubleValue());
        }
        
        return m_n;
    }
    
    public int enter(Number x) 
    {
        if (x != null)
            enter(x.doubleValue());
        
        return m_n;
    }
    
    public int enter(double x) 
    {
        m_sumX += x;
        m_sumX2 += x*x;
        if (x > m_max)
            m_max = x;
        if (x < m_min)
            m_min = x;
        
        if (m_retainDataset)
            m_values.add(x);
        
        return ++m_n;
    }
    
    public double calcStatistic(BuiltinOperator stat)
    {        
        if (stat == BuiltinOperator.CountOper)
            return m_n;
        
        if (m_n <= 0)
            return Double.NaN;
        
        double tmpX = 0.0;
        switch (stat) {
            case SumOper:
                return m_sumX;
                
            case Sum2Oper:
                return m_sumX2;
                
            case MeanOper:
                return m_sumX / m_n;
                
            case MinOper:   
                return m_min;
                
            case MaxOper:
                return m_max;
                
            case StDevOper:
                tmpX = calcStatistic(BuiltinOperator.MeanOper);
                return Math.sqrt((m_sumX2 - m_n * tmpX * tmpX)/m_n);
            
            case StDevSampleOper:
                if (m_n == 1)
                    return Double.NaN;
                tmpX = calcStatistic(BuiltinOperator.MeanOper);
                return Math.sqrt((m_sumX2 - m_n * tmpX * tmpX)/(m_n-1));
                
            case VarOper:
                tmpX = calcStatistic(BuiltinOperator.StDevOper);
                return tmpX * tmpX;
            
            case VarSampleOper:
                tmpX = calcStatistic(BuiltinOperator.StDevSampleOper);
                return tmpX * tmpX;
                
            case RangeOper:
                return Math.abs(m_max - m_min);
                
            case MedianOper:
                return calcMedian();
            
            default:
                throw new UnimplementedException("Unsupported statistic: " + stat);            
        }
    }

    private double calcMedian()
    {
        // special cases
        if (m_n == 0 || !m_retainDataset)
            return Double.NaN;
        
        if (m_n == 1)
            return m_values.get(0);
        
        // sort m_values to order it
        Collections.sort(m_values);
        
        // if the number of items is odd, return the middle of the list
        // if even, return the average of the middle two items
        
        int half = m_n/2;
        if (half * 2 == m_n) // even number
            return (m_values.get(half - 1) + m_values.get(half))/2.0;
        else
            return m_values.get(half);
    }
}
