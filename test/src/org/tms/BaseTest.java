package org.tms;

import java.io.File;


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
    
    protected final String qualifiedFileName(String fn)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(System.getProperty("user.dir"));
        sb.append(File.separator);
        sb.append("src");
        sb.append(File.separator);
        
        String packagePath = this.getClass().getPackage().getName();
        packagePath = packagePath.replaceAll("\\.", File.separator);
        
        sb.append(packagePath);
        sb.append(File.separator);
        sb.append(fn);
        
        return sb.toString();
    }
}
