package org.tms.api.utils;

import org.tms.api.Cell;
import org.tms.api.exceptions.TableCellValidationException;

public class NumericRangeRequired extends NumericRange
{
    public NumericRangeRequired(double minValue, double maxValue)
    {
        super(minValue, maxValue);
    }

    @Override
    public void validate(Cell cell, Object newValue) throws TableCellValidationException
    {
        if (newValue == null)
            throw new TableCellValidationException("Required");
            
        super.validate(cell, newValue);
    }
}
