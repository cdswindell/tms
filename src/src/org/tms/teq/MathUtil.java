package org.tms.teq;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937a;
import org.json.simple.JSONObject;
import org.tms.api.Cell;
import org.tms.api.TableElement;
import org.tms.api.derivables.InvalidOperandsException;

public class MathUtil
{
	static final public int indexOf(String source, String target)
	{
		return source.toLowerCase().indexOf(target.toLowerCase());
	}
	
	static final public boolean contains(String source, String target)
	{
		return indexOf(source, target) > -1;
	}
	
    static final public Object jsonGet(JSONObject json, String key)
    {
        if (key == null || (key = key.trim()).length() <= 0)
            return json;
        
        String [] tokens = key.split("/");
        
        JSONObject tree = json;
        Object leaf = null;
        int tokensProcessed = 0;
        for (String token : tokens) {
            leaf = tree.get(token);
            tokensProcessed++;
            if (leaf != null && leaf instanceof JSONObject)
                tree = (JSONObject)leaf;
            else
                break;
        }
        
        if (leaf == null && tokensProcessed < tokens.length)
            throw new InvalidOperandsException("Result not found: " + tokens[tokensProcessed]);
        
        return leaf;
    }
    
    static final public long quotient(double x, double y)
    {
    	// do some rounding
    	long xL = (long)(x + 0.5);
    	long yL = (long)(y + 0.5);
    	
    	return xL / yL;
    }
    
    static final public long toEven(double x)
    {
    	// save sign of final answer
    	int sgn = x >= 0 ? 1 : -1;
    	
    	// convert double to long
    	long xL = (long)Math.ceil(Math.abs(x));
    	
    	// now convert to even
    	xL += (xL % 2);
    	
    	// adjust sign
    	return xL * sgn;
    }
    
    static final public long toOdd(double x)
    {
    	// save sign of final answer
    	int sgn = x >= 0 ? 1 : -1;
    	
    	// convert double to long
    	long xL = (long)Math.ceil(Math.abs(x));
    	
    	// now convert to odd
    	xL += 1 - (xL % 2);
    	
    	// adjust sign
    	return xL * sgn;
    }
    
    static final public double pmt(final double ipt, final int nPer, final double pv, final double fv)
    {
        double tmp = Math.exp(nPer*Math.log1p(ipt));
        double pmt = ((pv*tmp + fv)*ipt)/(1.0 - tmp);      
        return pmt;
    }
    
    static final public double fv(final double ipt, final int nPer, final double pmt, final double pv)
    {
        double tmp = Math.exp(nPer*Math.log1p(ipt));
        double fv = -pv * tmp - pmt * ((tmp - 1.0)/ipt);      
        return fv;
    }
    
    static final public double pv(final double ipt, final int nPer, final double pmt, final double fv)
    {
        double tmp = Math.exp(nPer*Math.log1p(ipt));
        double pv = (-fv - pmt * ((tmp - 1.0)/ipt))/tmp;      
        return pv;
    }
    
    static final public double nper(double ipt, double pmt, double pv, double fv)
    {
        double tmp = pmt/ipt;
        double tmp2 = (-fv + tmp)/(pv + tmp);   
        
        double nPer = Math.log(tmp2)/Math.log(1+ipt);
        return nPer;
    }
    
    static final public double rate(int nPer, double pmt, double pv, double fv)
    {
        double ipt = 0.0;
        double in = 0;
        
        if (pv != 0.0)             
            ipt = (-fv - pv - pmt*nPer)/pv/nPer;
        else
            ipt = -fv /pmt/nPer/nPer;
        
        while (true) {
            double t1 = Math.exp(nPer*Math.log1p(ipt));
            double t2 = pmt/ipt*(t1 - 1);
            double t3 = t2 + fv + pv * t1;
            double t4 = (t2 + (fv * ipt - pmt)*nPer/(1 + ipt))/ipt;
            
            in = ipt + t3/t4;
            double diff = Math.abs((in-ipt)/in);
            if (diff < 1e-13) break;
            
            ipt = in;
        }
        
        return ipt;
    }
    
    /**
     * A formula for the number of possible combinations of r objects from a set of n objects where order is not important. 
     * From probability theory, this is the calculation of nCr or n Choose r, or n!/r!(n-r)!.
     * @param popSize
     * @param subSetSize
     * @return nCr
     */
    static final public double numCombinations(double popSize, double subSetSize)
    {
        long n = (long)popSize;
        long r = (long)subSetSize;
        
        if (n < 1 || r < 1 || n < r)
            return Double.NaN;
        
        // handle edge case
        long result = 1;
        if (n == r)
            return result;
                
        for (long i = n; i > (n-r); i--) {
            result *= i;
        }
                    
        result = result/(long)fact((double)r);
        
        return result;
    }
    
    static final public double numPermutations(double popSize, double subSetSize)
    {
        long n = (long)popSize;
        long k = (long)subSetSize;
        
        if (n < 1 || k < 1 || n < k)
            return Double.NaN;
        
        // handle edge case
        long result = 1;
        if (n == 1)
            return result;
                
        for (long i = n; i > (n - k); i--) {
            result *= i;
        }
                    
        return result;
    }
    
    static final public double mod(double x, double y)
    {
        return x % y;
    }
    
    static final public double neg(double arg)
    {
        return -arg;
    }
    
    static final public double frac(double arg)
    {
        return arg - Math.floor(arg);
    }
    
    static final public double fact(double arg)
    {
        double rVal = 1;
        if (arg > 1.0) {
            long dl = Math.round(arg);
            for (long i = dl; i > 1; i--) {
                rVal *= i;
            }
        }
        
        return rVal;
    }
    
    static final public double randomInt(double arg)
    {
        double d = Math.ceil(Math.abs(arg));
        return 1 + Math.floor(d * Math.random());
    }
    
    static final public int randomBetween(double arg1, double arg2)
    {
        int i1 = (int)Math.ceil(arg1 < arg2 ? arg1 : arg2);
        int i2 = (int)Math.ceil(arg2 > arg1 ? arg2 : arg1);
        return i1 + (int)((i2 - i1) * Math.random() + 0.5);
    }
    
    static final public double e()
    {
        return Math.E;
    }
    
    static final public double pi()
    {
        return Math.PI;
    }
    
    static final public double sinD(double arg)
    {
        return Math.sin(Math.toRadians(arg));
    }
    
    static final public double cosD(double arg)
    {
        return Math.cos(Math.toRadians(arg));
    }
    
    static final public double tanD(double arg)
    {
        arg = Math.IEEEremainder(arg, 360.0);
        
        // check for infinity cases
        if (arg/90.0 == 1.0)
            return Double.POSITIVE_INFINITY;
        else if (arg/90.0 == -1.0)
            return Double.NEGATIVE_INFINITY;
        else    
            return Math.tan(Math.toRadians(arg));
    }
    
    static final public double asinD(double arg)
    {
        return Math.toDegrees(Math.asin(arg));
    }

    static final public double acosD(final double arg)
    {
        return Math.toDegrees(Math.acos(arg));
    }   

    static final public double atanD(final double arg)
    {
        // handle special cases first
        if (Double.POSITIVE_INFINITY == arg)
            return 90.0;
        else if (Double.NEGATIVE_INFINITY == arg)
            return -90.0;
        else
            return Math.toDegrees(Math.atan(arg));
    } 
    
    static final public double percent(final double arg)
    {
        return arg / 100.0;
    }
    
    static final public String toLower(String arg)
    {
        return arg != null ? arg.toLowerCase() : null;
    }   
    
    static final public String toUpper(String arg)
    {
        return arg != null ? arg.toUpperCase() : null;
    }   
    
    static final public String trim(String arg)
    {
        return arg != null ? arg.trim() : null;
    }   
    
    static final public String reverse(String arg)
    {
        if (arg != null) {
            int len = arg.length();
            StringBuffer sb = new StringBuffer();
            
            if (len > 0) {
                char [] chars = arg.toCharArray();
                for (int i = len - 1; i >= 0; i--) {
                    sb.append(chars[i]);
                }
            }
            
            return sb.toString();
        }
        else 
            return null;
    }   
    
    static final public double length(String arg)
    {
        return arg != null ? arg.length() : 0;
    }   
    
    static final public String instrMid(String arg, int firstChar, int numChars)
    {
        if (arg != null && firstChar > 0 && numChars >= 0) {
            int argLen = arg.length();            
            if (firstChar > argLen)
                return null;
            else
                return arg.substring(firstChar - 1, Math.min(firstChar - 1 + numChars, argLen));
        }
        else
            return null;
    }
    
    static final public String instrLeft(String arg, int numChars)
    {
        if (arg != null && numChars >= 0) {
            if (numChars > arg.length())
                return arg;
            else
                return arg.substring(0, numChars);
        }
        else
            return null;
    }
    
    static final public String instrRight(String arg, int numChars)
    {
        if (arg != null && numChars >= 0) {
            if (numChars > arg.length())
                return arg;
            else
                return arg.substring(arg.length() - numChars);
        }
        else
            return null;
    }
    
    static final public double toNumber(Object arg)
    {
        if (arg instanceof Number)
            return (Double)arg;
        else if (arg instanceof Boolean) 
            return ((Boolean)arg).booleanValue() ? 1 : 0;
        else if (arg instanceof BigDecimal) 
            return ((BigDecimal)arg).doubleValue();
        else if (arg instanceof Date) 
            return ((Date)arg).getTime();
        else if (arg instanceof String) {
            try {
                double d = Double.valueOf((String)arg);
                return d;
            }
            catch (NumberFormatException e) 
            {
                return Double.NaN;
            }
        }
        else
            return Double.NaN;
    }   
    
    static final public int numberOf(TableElement te, Object q)
    {
        int count = 0;
        if (te != null && q != null) {
        	List<TableElement> affected = null;
            BigDecimal bdQ = toBigDecimal(q);
            Iterable<Cell> cellIter = te.cells();
            if (cellIter != null) {
                for (Cell c : cellIter) {
                    if (c == null)
                        continue;
                    
                	if (c.isDerived()) {
                		affected = c.getAffectedBy();
                		if (affected != null && affected.contains(te))
                			continue;
                	}
                	
                    Object cellValue = c.getCellValue();
                    if (q.equals(cellValue) ||
                        (bdQ != null && cellValue != null && 
                            (cellValue instanceof Number) && 
                            bdQ.compareTo(toBigDecimal(cellValue)) == 0))
                        count++;
                }
            }
        }
        
        return count;
    }   
    
    static final private BigDecimal toBigDecimal(Object n)
    {
        if (n != null && (n instanceof Number)) {
            if (n instanceof Double)
                return BigDecimal.valueOf((Double)n);
            if (n instanceof Integer)
                return BigDecimal.valueOf((Integer)n);
            if (n instanceof Long)
                return BigDecimal.valueOf((Long)n);
            if (n instanceof Short)
                return BigDecimal.valueOf((Short)n);
        }
        
        return null;
    }
    
    static final public String toString(Object arg)
    {
        if (arg == null)
            return null;
        else
            return arg.toString().trim();
    }  
    
    static private RandomGenerator sf_DISTRIBUTION_RANDOM_GENERATOR = null;
    static private final Object sf_LOCK = new Object();
    
    static final public double normalSample(double mean, double stDev) 
    {
        synchronized(sf_LOCK) {
            if (sf_DISTRIBUTION_RANDOM_GENERATOR == null)
                sf_DISTRIBUTION_RANDOM_GENERATOR = new Well19937a(System.currentTimeMillis());
        }
        
        NormalDistribution nd = new NormalDistribution(sf_DISTRIBUTION_RANDOM_GENERATOR, mean, stDev);
        
        return nd.sample();
    }
    
    static final public double normalDensity(double mean, double stDev, double x) 
    {
        NormalDistribution nd = new NormalDistribution(null, mean, stDev);
        
        return nd.density(x);
    }
    
    static final public double normalCumProb(double mean, double stDev, double x) 
    {
        NormalDistribution nd = new NormalDistribution(null, mean, stDev);
        
        return nd.cumulativeProbability(x);
    }
    
    static final public double normalInvCumProb(double mean, double stDev, double x) 
    {
        NormalDistribution nd = new NormalDistribution(null, mean, stDev);
        
        return nd.inverseCumulativeProbability(x);
    }
    
    static final public double normalProbability(double mean, double stDev, double x) 
    {
        NormalDistribution nd = new NormalDistribution(null, mean, stDev);
        return nd.probability(x);
    }
    
    static final public double normalProbInRange(double mean, double stDev, double x0, double x1) 
    {
        NormalDistribution nd = new NormalDistribution(null, mean, stDev);
        
        return nd.probability(x0, x1);
    }
    
    /*
     * Chi Squared Distribution
     */
    static final public double chiSqSample(double dof) 
    {
        synchronized(sf_LOCK) {
            if (sf_DISTRIBUTION_RANDOM_GENERATOR == null)
                sf_DISTRIBUTION_RANDOM_GENERATOR = new Well19937a(System.currentTimeMillis());
        }
        
        ChiSquaredDistribution nd = new ChiSquaredDistribution(sf_DISTRIBUTION_RANDOM_GENERATOR, dof);        
        return nd.sample();
    }
    
    static final public double chiSqDensity(double dof, double x) 
    {
        ChiSquaredDistribution nd = new ChiSquaredDistribution(null, dof);        
        return nd.density(x);
    }
    
    static final public double chiSqCumProb(double dof, double x) 
    {
        ChiSquaredDistribution nd = new ChiSquaredDistribution(null, dof);        
        return nd.cumulativeProbability(x);
    }
    
    static final public double chiSqInvCumProb(double dof, double x) 
    {
        ChiSquaredDistribution nd = new ChiSquaredDistribution(null, dof);        
        return nd.inverseCumulativeProbability(x);
    }
    
    static final public double chiSqProbability(double dof, double x) 
    {
        ChiSquaredDistribution nd = new ChiSquaredDistribution(null, dof);
        return nd.probability(x);
    }
    
    static final public double chiSqProbInRange(double dof, double x0, double x1) 
    {
        ChiSquaredDistribution nd = new ChiSquaredDistribution(null, dof);        
        return nd.probability(x0, x1);
    }
    
    static final public double chiSqScore(double stDevPop, double stDevSamp, double nSamples) 
    {
        if (nSamples < 1 || stDevPop == 0.0)
            return Double.NaN;
        
        return (nSamples - 1)*stDevSamp*stDevSamp/(stDevPop*stDevPop);
    }
    
    /*
     * T Distribution
     */
    static final public double tSample(double dof) 
    {
        synchronized(sf_LOCK) {
            if (sf_DISTRIBUTION_RANDOM_GENERATOR == null)
                sf_DISTRIBUTION_RANDOM_GENERATOR = new Well19937a(System.currentTimeMillis());
        }
        
        TDistribution nd = new TDistribution(sf_DISTRIBUTION_RANDOM_GENERATOR, dof);        
        return nd.sample();
    }
    
    static final public double tDensity(double dof, double x) 
    {
        TDistribution nd = new TDistribution(null, dof);        
        return nd.density(x);
    }
    
    static final public double tCumProb(double dof, double x) 
    {
        TDistribution nd = new TDistribution(null, dof);        
        return nd.cumulativeProbability(x);
    }
    
    static final public double tInvCumProb(double dof, double x) 
    {
        TDistribution nd = new TDistribution(null, dof);        
        return nd.inverseCumulativeProbability(x);
    }
    
    static final public double tProbability(double dof, double x) 
    {
        TDistribution nd = new TDistribution(null, dof);
        return nd.probability(x);
    }
    
    static final public double tProbInRange(double dof, double x0, double x1) 
    {
        TDistribution nd = new TDistribution(null, dof);        
        return nd.probability(x0, x1);
    }
    
    /**
     * 
     * @param meanPop
     * @param meanSamp
     * @param stDevSamp
     * @param nSamples
     * @return
     */
    static final public double tScore(double meanPop, double meanSamp, double stDevSamp, double nSamples) 
    {
        if (nSamples < 1 || stDevSamp <= 0)
            return Double.NaN;
        
        return (meanSamp - meanPop)/(stDevSamp/Math.sqrt(nSamples));
    }
    
    /**
     * 
     * @param cumProb
     * @param meanSamp
     * @param stDevSamp
     * @param nSamples
     * @return
     */
    static final public double popMean(double cumProb, double meanSamp, double stDevSamp, double nSamples) 
    {
        if (nSamples < 2)
            return Double.NaN;
        
        return meanSamp - tInvCumProb(nSamples-1, cumProb)*(stDevSamp/Math.sqrt(nSamples));
    }
    
    /*
     * Exponential Distribution
     */
    
    static final public double exponentialSample(double mean) 
    {
        synchronized(sf_LOCK) {
            if (sf_DISTRIBUTION_RANDOM_GENERATOR == null)
                sf_DISTRIBUTION_RANDOM_GENERATOR = new Well19937a(System.currentTimeMillis());
        }
        
        ExponentialDistribution nd = new ExponentialDistribution(sf_DISTRIBUTION_RANDOM_GENERATOR, mean);        
        return nd.sample();
    }
    
    static final public double exponentialDensity(double mean, double x) 
    {
        ExponentialDistribution nd = new ExponentialDistribution(null, mean);        
        return nd.density(x);
    }
    
    static final public double exponentialCumProb(double mean, double x) 
    {
        ExponentialDistribution nd = new ExponentialDistribution(null, mean);        
        return nd.cumulativeProbability(x);
    }
    
    static final public double exponentialInvCumProb(double mean, double x) 
    {
        ExponentialDistribution nd = new ExponentialDistribution(null, mean);        
        return nd.inverseCumulativeProbability(x);
    }
    
    static final public double exponentialProbability(double mean, double x) 
    {
        ExponentialDistribution nd = new ExponentialDistribution(null, mean);
        return nd.probability(x);
    }
    
    static final public double exponentialProbInRange(double mean, double x0, double x1) 
    {
        ExponentialDistribution nd = new ExponentialDistribution(null, mean);        
        return nd.probability(x0, x1);
    }
    
    /*
     * Linear Regression
     */
    static final public double lrComputeX(double m, double b, double y) 
    {
        return (y - b)/m ;
    }
    
    static final public double lrComputeY(double m, double b, double x) 
    {
        return m*x + b;
    }
    
    static final public Object parseCellValue(String s, boolean ignoreSpaces)
    {
        try {
            char c = 0;
            if (Boolean.valueOf(s))
                return true;
            else if ("false".equalsIgnoreCase(s))
                return false;
            else if ((c = s.charAt(0)) > 0 && (!Character.isDigit(c) && c != '+' && c != '-'))
                return ignoreSpaces ? s.trim() : s;
            
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            try {
                return Double.parseDouble(s);
            }
            catch (Exception e2) {
                return s;
            }
        }
    }
}
