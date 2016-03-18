package org.tms.api.derivables;

public interface Labeled 
{
    public String getLabel();
    
    default public boolean isLabeled()
    {
        return getLabel() != null ? true : false;
    }
    
    default public int getLabelLength()
    {
        if (getLabel() != null)
            return getLabel().length();
        else
            return 0;
    }    
}
