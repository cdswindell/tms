package org.tms;


public class BaseTest
{
    public boolean closeTo(double x, double y, double withIn) 
    {
        return withIn > Math.abs(x - y);
    }

    public boolean closeTo(Object x, double y, double withIn) 
    {
        if (x == null)
            return false;
        
        return withIn > Math.abs((Double)x - y);
    }

}
