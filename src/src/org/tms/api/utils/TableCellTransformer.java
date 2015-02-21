package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;

@FunctionalInterface
public interface TableCellTransformer extends TableCellValidator
{
    /**
     * Validates the candidate cell value according to the rules coded in the implementing method.
     * @param newValue the candidate cell value to validate
     * @throws ConstraintViolationException if the candidate value doesn't conform to the constraints defined in {@code validate}.
     */
    default public void validate(Object newValue) throws ConstraintViolationException
    {
        
    }
    
    /**
     * Validate and transform the candidate cell value according to the rules coded in the implementing method. If the candidate
     * cell value passes the validation criteria, it can also be transformed by the implementing method to apply application-specific 
     * formating and data consistency rules.
     * <p/>
     * Note that as the {@code TableCellValidator} contract requires that implementing classes supply a {@code validate} method, classes
     * that override {@code transform} may choose to implement all validation logic in {@code validate} and simply invoke that method
     * within.
     * @param newValue the candidate cell value to validate and transform
     * @return the validated and transformed candidate cell value
     * @throws ConstraintViolationException ConstraintViolationException if the candidate value doesn't conform to the constraints.
     */
    public Object transform(Object newValue);
}
