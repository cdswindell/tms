package org.tms.api.utils;

import org.tms.api.Cell;
import org.tms.api.TableCellValidator;
import org.tms.api.exceptions.ConstraintViolationException;

public class NumericRange implements TableCellValidator
{   
    private double m_minValue;
    private double m_maxValue;
    
    public NumericRange(double minValue, double maxValue)
    {
        if (maxValue < minValue)
            throw new IllegalArgumentException("Minimum value must be less than or equal to maximum.");
        
        m_minValue = minValue;
        m_maxValue = maxValue;
    }

    @Override
    public void validate(Cell cell, Object newValue) throws ConstraintViolationException
    {
        if (newValue == null)
            return;
        
        if (!(newValue instanceof Number))
            throw new ConstraintViolationException("Numeric Required");
            
        double n = ((Number)newValue).doubleValue();
        if (m_minValue != Double.MIN_VALUE && n < m_minValue)
            throw new ConstraintViolationException("Too small");
        
        if (m_maxValue != Double.MAX_VALUE && n > m_maxValue)
            throw new ConstraintViolationException("Too large");
    }
}
