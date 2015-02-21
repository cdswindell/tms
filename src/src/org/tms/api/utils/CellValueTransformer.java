package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

public class CellValueTransformer implements TableCellValidator
{
    @Override
    public void validate(Object newValue) throws ConstraintViolationException
    {
        // Intentionally does nothing
    }
    
    @Override
    public Object transform(Object newValue)
    {
        return newValue;
    }
}
