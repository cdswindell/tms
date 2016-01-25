package org.tms.io;

abstract class BaseIO
{   
    protected String trimString(String arg)
    {
        if (arg == null || (arg = arg.trim()).length() <= 0)
            return null;
        else
            return arg;
    }
    
    protected String removeString(String target, String toExpunge)
    {
        if (target != null && toExpunge != null) {
            if (!toExpunge.endsWith(":"))
                toExpunge += ":";
            
            int teLen = toExpunge.length();
            int idx;
            while ((idx = target.lastIndexOf(toExpunge)) > -1) {
                target = target.substring(0, idx) + target.substring(idx + teLen);
            }
        }
        
        return trimString(target);
    }
}
