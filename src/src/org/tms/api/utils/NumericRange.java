package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

/**
 * {@code NumericRange} demonstrates the use of the {@link TableCellValidator} interface used
 * to provide data validation for data stored in {@link org.tms.api.Table Table} {@link org.tms.api.Cell Cell}s.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class NumericRange implements TableCellValidator
{   
    private static final long serialVersionUID = -8715729726175554010L;
    
    private double m_minValue;
    private double m_maxValue;
    
    /**
     * Creates a new {@link NumericRange} instance with the specified minimum and maximum
     * cell value limits.
     * @param minValue the minimum value allowed in a cell
     * @param maxValue the maximum value allowed in a cell
     */
    public NumericRange(double minValue, double maxValue)
    {
        if (maxValue < minValue)
            throw new IllegalArgumentException("Minimum value must be less than or equal to maximum value.");
        
        m_minValue = minValue;
        m_maxValue = maxValue;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void validate(Object newValue) throws ConstraintViolationException
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
