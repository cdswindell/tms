package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

public class NumericRangeRequired extends NumericRange
{
    public NumericRangeRequired(double minValue, double maxValue)
    {
        super(minValue, maxValue);
    }

    @Override
    public void validate(Object newValue) throws ConstraintViolationException
    {
        if (newValue == null)
            throw new ConstraintViolationException("Required");
            
        super.validate(newValue);
    }
}
