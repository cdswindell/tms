package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

/**
 * {@code NumericRangeRequired} demonstrates the use of the {@link TableCellValidator} interface used
 * to provide data validation for data stored in {@link org.tms.api.Table Table} {@link org.tms.api.Cell Cell}s.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class NumericRangeRequired extends NumericRange
{
    /**
     * Creates a new {@link NumericRangeRequired} instance with the specified minimum and maximum
     * cell value limits. In addition to disallowing cell values that do not fall within the range,
     * {@code null} values are also disallowed.
     * @param minValue the minimum value allowed in a cell
     * @param maxValue the maximum value allowed in a cell
     */
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
