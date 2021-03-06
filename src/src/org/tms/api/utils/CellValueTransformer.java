package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

/**
 * An {@code Identity} {@link TableCellValidator} that returns the candidate cell value as the
 * result of the transform operation.
 */
public class CellValueTransformer implements TableCellValidator
{
    private static final long serialVersionUID = -8442377707257020619L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object newValue) throws ConstraintViolationException
    {
        // Intentionally does nothing
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object transform(Object newValue)
    {
        return newValue;
    }
}
