package org.tms.teq;

import java.math.BigDecimal;

import org.tms.api.Cell;
import org.tms.api.TableCellsElement;

public class MathUtil
{
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

    static final public double acosD(double arg)
    {
        return Math.toDegrees(Math.acos(arg));
    }   

    static final public double atanD(double arg)
    {
        // handle special cases first
        if (Double.POSITIVE_INFINITY == arg)
            return 90.0;
        else if (Double.NEGATIVE_INFINITY == arg)
            return -90.0;
        else
            return Math.toDegrees(Math.atan(arg));
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
    
    static final public double toNumber(Object arg)
    {
        if (arg instanceof Number)
            return (Double)arg;
        else if (arg instanceof Boolean) 
            return ((Boolean)arg).booleanValue() ? 1 : 0;
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
    
    static final public double numberOf(TableCellsElement te, Object q)
    {
        int count = 0;
        if (te != null && q != null) {
            BigDecimal bdQ = toBigDecimal(q);
            for (Cell c : te.cells()) {
                Object cellValue = c.getCellValue();
                if (q.equals(c.getCellValue()) ||
                    (bdQ != null && bdQ.compareTo(toBigDecimal(cellValue)) == 0))
                    count++;
            }
        }
        
        return (double)count;
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
}
