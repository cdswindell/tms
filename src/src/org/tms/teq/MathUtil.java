package org.tms.teq;

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
}