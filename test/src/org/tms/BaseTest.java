package org.tms;

public class BaseTest
{

    public boolean closeTo(double x, double y, double withIn) 
    {
        return withIn > Math.abs(x - y);
    }

}