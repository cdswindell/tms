package org.tms.teq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.inference.TTest;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.UnimplementedException;

public class SingleVariableStatEngine
{
    private int m_n;
    private double m_sumX;
    private double m_sumX2;
    private double m_min;
    private double m_max;
    private double m_mean = Double.MIN_VALUE;
    private double m_stDevS = Double.MIN_VALUE;
    private double m_stDevP = Double.MIN_VALUE;
    private double m_median = Double.MIN_VALUE;
    private double m_1stQ = Double.MIN_VALUE;
    private double m_3rdQ = Double.MIN_VALUE;
    boolean m_retainDataset;
    private List<Double> m_values;
    private Set<Cell> m_excludedCells;
    private Set<Row> m_excludedRows;
    private Set<Column> m_excludedColumns;
    private SummaryStatistics m_sStats;
    private Skewness m_skewness;
    private Kurtosis m_kurtosis;
    
    public SingleVariableStatEngine()
    {
        m_retainDataset = false;
        m_sStats = new SummaryStatistics();
        m_skewness = new Skewness();
        m_kurtosis = new Kurtosis();
        
        reset();
    }
    
    public SingleVariableStatEngine(boolean retainDataset)
    {
        this();
        m_retainDataset = retainDataset;
        
        if (m_retainDataset) 
            m_values = new ArrayList<Double>();
    }
    
    public SingleVariableStatEngine(Double[] values)
    {
        this();
        enter(values);
    }

	public boolean isRetainDataset() 
	{
		return m_retainDataset;
	}
	
    public void reset() 
    {
        m_n = 0;
        m_sumX = m_sumX2 = 0;
        m_min = Double.POSITIVE_INFINITY;
        m_max = Double.NEGATIVE_INFINITY;
        
        resetCalcCache();
        
        m_excludedCells = new HashSet<Cell>();
        m_excludedRows = new HashSet<Row>();
        m_excludedColumns = new HashSet<Column>();
        
        m_sStats.clear();
        m_skewness.clear();
        m_kurtosis.clear();
        if (m_retainDataset) 
            m_values.clear();
    }

    private void resetCalcCache()
    {
        m_mean = Double.MIN_VALUE;
        m_stDevS = Double.MIN_VALUE;
        m_stDevP = Double.MIN_VALUE;
        m_median = Double.MIN_VALUE;
        m_1stQ = Double.MIN_VALUE;
        m_3rdQ = Double.MIN_VALUE;
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
        resetCalcCache();
        
        if (x == Double.MIN_VALUE) // skip null values
            return m_n; 
        
        m_sumX += x;
        m_sumX2 += x*x;
        if (x > m_max)
            m_max = x;
        if (x < m_min)
            m_min = x;
        
        m_sStats.addValue(x);
        m_skewness.increment(x);
        m_kurtosis.increment(x);
        
        if (m_retainDataset) 
            m_values.add(x);
        
        return ++m_n;
    }
    
    public void exclude(Cell cell)
    {
        if (cell != null) {
            m_excludedCells.add(cell);
            
            Row row = cell.getRow();
            if (row != null)
                m_excludedRows.add(row);
            
            Column col = cell.getColumn();
            if (col != null)
                m_excludedColumns.add(col);
        }
    }
    
    public boolean isExcluded(Cell cell) 
    {
        if (cell == null)
            return false;
        else
            return m_excludedCells.contains(cell);
    }
    
    public boolean isExcluded(Row row) 
    {
        if (row == null)
            return false;
        else
            return m_excludedRows.contains(row);
    }
    
    public boolean isExcluded(Column col) 
    {
        if (col == null)
            return false;
        else
            return m_excludedColumns.contains(col);
    }
    
    public boolean isExcluded(TableRowColumnElement rce) 
    {
        if (rce != null) {
            if (rce instanceof Row)
                return isExcluded((Row) rce);
            if (rce instanceof Column)
                return isExcluded((Column) rce);
            if (rce instanceof Cell)
                return isExcluded((Cell) rce);
        }
        
        return false;
    }
    
    protected SummaryStatistics getSummaryStatistics()
    {
        return m_sStats;
    }
    
    public Object calcStatistic(BuiltinOperator stat, Token... params)
    {        
        if (stat == BuiltinOperator.CountOper)
            return m_n;
        
        if (m_n <= 0)
            return Double.NaN;
        
        double tmpX = 0.0;
        Object value = 0.0;
        TTest tTest = null;
        switch (stat) {
            case SumOper:
                value = m_sumX;
                break;
                
            case Sum2Oper:
                value = m_sumX2;
                break;
                
            case SumSqD2Oper:
                value = m_sumX2 - m_sumX*m_sumX/m_n;
                break;
                
            case MeanOper:
            	if (m_mean != Double.MIN_VALUE)
            		value = m_mean;
            	else 
            	    value = m_mean = m_sumX / m_n;
                break;
                
            case MinOper:   
                value = m_min;
                break;
                
            case MaxOper:
                value = m_max;
                break;
                
            case StDevPopulationOper:
            	if (m_stDevP != Double.MIN_VALUE)
            		value = m_stDevP;
            	else {
	                tmpX = (double)calcStatistic(BuiltinOperator.MeanOper);
	                value = m_stDevP = Math.sqrt((m_sumX2 - m_n * tmpX * tmpX)/m_n);
            	}
                break;
            
            case StDevSampleOper:
                if (m_n == 1)
                    return Double.NaN;
                
            	if (m_stDevS != Double.MIN_VALUE)
            		value = m_stDevS;
            	else {
	                tmpX = (double)calcStatistic(BuiltinOperator.MeanOper);
	                value = m_stDevS = Math.sqrt((m_sumX2 - m_n * tmpX * tmpX)/(m_n-1));
            	}
                break;
                
            case VarPopulationOper:
                tmpX = (double)calcStatistic(BuiltinOperator.StDevPopulationOper);
                value = tmpX * tmpX;
                break;
            
            case VarSampleOper:
                tmpX = (double)calcStatistic(BuiltinOperator.StDevSampleOper);
                value = tmpX * tmpX;
                break;
                
            case RangeOper:
                value = Math.abs(m_max - m_min);
                break;
                
            case ModeOper:
                value = calcMode();
                break;
            
            case MedianOper:
                value = calcMedian();
                break;
            
            case FirstQuartileOper:
                value = calcFirstQuartile();
                break;
                
            case ThirdQuartileOper:
                value = calcThirdQuartile();
                break;
                
            case SkewOper:
                value = m_skewness.getResult();
                break;
            
            case KurtosisOper:
                value = m_kurtosis.getResult();
                break;
            
            case PValueOper:
                if (m_n < 2 || params == null || params.length < 1 || params[0].isNull() || !params[0].isNumeric())
                    value = Double.NaN;
                else {
                    double mu = params[0].getNumericValue();
                    tTest = new TTest();
                    value = tTest.tTest(mu, m_sStats);
                }
                break;
                
            case TValueOper:
                if (m_n < 2 || params == null || params.length < 1 || params[0].isNull() || !params[0].isNumeric())
                    value = Double.NaN;
                else {
                    double mu = params[0].getNumericValue();
                    tTest = new TTest();
                    value = tTest.t(mu, m_sStats);
                }
                break;
                
            case TTestOper:
                if (m_n < 2 || params == null || params.length < 2 || !params[0].isNumeric() || !params[1].isNumeric())
                    value = Double.NaN;
                else {
                    double mu = params[0].getNumericValue();
                    double alpha = params[1].getNumericValue();
                    tTest = new TTest();
                    value = tTest.tTest(mu, m_sStats, alpha);
                }
                break;
                
            default:
                throw new UnimplementedException("Unsupported statistic: " + stat);            
        }
        
        return value;
    }

    private double calcMode()
    {
        // special cases
        if (m_n == 0 || !m_retainDataset)
            return Double.NaN;
        
        if (m_n == 1)
            return m_values.get(0);
        
        // iterate through the set of values to calculate the frequencies
        Map<Double, Integer> frequencies = new HashMap<Double, Integer>(m_n);
        for (double d : m_values) {
            Integer count = frequencies.get(d);
            if (count == null) 
                frequencies.put(d, 1);
            else
                count++;
        }
        
        // now invert the map and tabulate the the frequencies        
        int maxFrequency = Integer.MIN_VALUE;
        Map<Integer, Set<Double>> freqMap = new HashMap<Integer, Set<Double>>(m_n);
        for (Entry<Double, Integer> e : frequencies.entrySet()) {
            int freq = e.getValue();
            double value = e.getKey();
            
            Set<Double> valSet = freqMap.get(freq);
            if (valSet == null) {
                valSet = new HashSet<Double>();
                freqMap.put(freq,  valSet);
            }
            
            valSet.add(value);
            
            if (freq > maxFrequency)
                maxFrequency = freq;
        }
        
        // at this point, we have the key, maxFrequency, into the freqMap, 
        // who's value is the set of most frequent values, e.g., the mode
        Set<Double> modeValues = freqMap.get(maxFrequency);
        
        Double [] modeValuesArray = modeValues.toArray(new Double[] {});
        if (modeValues.size() > 1) {
            // return the mean value
            SingleVariableStatEngine svse = new SingleVariableStatEngine(modeValuesArray);
            return (double)svse.calcStatistic(BuiltinOperator.MeanOper);
        }
        else
            return modeValuesArray[0];
    }
    
    private double calcMedian()
    {
        if (m_median != Double.MIN_VALUE)
            return m_median;
        
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
            return (m_median = (m_values.get(half - 1) + m_values.get(half))/2.0);
        else
            return (m_median = m_values.get(half));
    }
    
    private double calcFirstQuartile()
    {
        if (m_1stQ != Double.MIN_VALUE)
            return m_1stQ;
        
        // calculate median of all values < median
        double median = calcMedian();
        if (median == Double.NaN)
            return median;
        
        SingleVariableStatEngine svse = new SingleVariableStatEngine(true);
        for (double x : m_values) {
            if (x < median)
                svse.enter(x);
            else
                break; // we know m_values must be sorted at this point, since calc of median does this
        }
        
        return (m_1stQ = svse.calcMedian());
    }
    
    private double calcThirdQuartile()
    {
        if (m_3rdQ != Double.MIN_VALUE)
            return m_1stQ;
        
        // calculate median of all values > median
        double median = calcMedian();
        if (median == Double.NaN)
            return median;
        
        SingleVariableStatEngine svse = new SingleVariableStatEngine(true);
        for (double x : m_values) {
            if (x > median)
                svse.enter(x);
        }
        
        return (m_3rdQ = svse.calcMedian());
    }    
}
