package org.tms.api.utils;

import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.tds.util.GroovyCellTransformer;

/**
 * Classes that implement {@code TableCellTransformer} can modify values before they are persisted in
 * {@link org.tms.api.Table Table} {@link org.tms.api.Cell Cell}s. Uses include trimming whitespace from character strings,
 * correcting character case, converting floating point numbers to integers, or truncating numbers to specific precisions, etc.
 * <p>
 * Because {@code TableCellTransformer} is a {@link FunctionalInterface}, a lambda expression (closure) can be
 * used in place of a class instance in calls to 
 * {@link org.tms.api.TableRowColumnElement#setTransformer(TableCellTransformer) TableRowColumnElement.setTransformer()} or
 * {@link org.tms.api.Cell#setTransformer(TableCellTransformer) Cell.setTransformer()}.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see org.tms.api.Cell#setTransformer(TableCellTransformer) Cell.setTransformer(TableCellTransformer)
 * @see org.tms.api.TableRowColumnElement#setTransformer(TableCellTransformer) TableRowColumnElement.setTransformer(TableCellTransformer)
 */
@FunctionalInterface
public interface TableCellTransformer extends TableCellValidator
{
    /**
     * Create and return a {@link TableCellTransformer} based on a Groovy implementation. 
     * The contained Groovy class is inspected for a method that matches the signature
     * of {@link TableCellTransformer#transform(Object) transform(Object)} and uses it
     * to perform cell value transformation.
     * @param fileName file name of the Groovy script
     * @return a {code TableCellTransformer} based on a Groovy implementation 
     */
    static public TableCellTransformer fromGroovy(String fileName)
    {
        return new GroovyCellTransformer(fileName, null, null);
    }
    
    /**
     * Create and return a {@link TableCellTransformer} based on a Groovy implementation. Methods
     * for both cell value validation as well as transformation can be specified.
     * @param fileName file name of the Groovy script
     * @param valName method name of the cell validator
     * @param transName method name of the cell transformer
     * @return a {code TableCellTransformer} based on a Groovy implementation 
     */
    static public TableCellTransformer fromGroovy(String fileName, String valName, String transName)
    {
        return new GroovyCellTransformer(fileName, valName, transName);
    }
    
    /**
     * Validates the candidate cell value according to the rules coded in the implementing method.
     * Implementing classes can override this method so that their class can be used as both
     * {@link TableCellValidator}s as well as {@link TableCellTransformer}s.
     * @param newValue the candidate cell value to validate
     * @throws ConstraintViolationException if the candidate value doesn't conform to the constraints defined in {@code validate}.
     */
    default public void validate(Object newValue) throws ConstraintViolationException
    {
        // noop
    }
    
    /**
     * Validate and transform the candidate cell value according to the rules coded in the implementing method. If the candidate
     * cell value passes the validation criteria, it can also be transformed by the implementing method to apply application-specific 
     * formating and data consistency rules.
     * <p>
     * Note that as the {@code TableCellValidator} contract requires that implementing classes supply a {@code validate} method, classes
     * that override {@code transform} may choose to implement all validation logic in {@code validate} and simply invoke that method
     * within.
     * @param newValue the candidate cell value to validate and transform
     * @return the validated and transformed candidate cell value
     * @throws ConstraintViolationException ConstraintViolationException if the candidate value doesn't conform to the constraints.
     * @see org.tms.api.Cell#setTransformer(TableCellTransformer) Cell.setTransformer(TableCellTransformer)
     * @see org.tms.api.TableRowColumnElement#setTransformer(TableCellTransformer) TableRowColumnElement.setTransformer(TableCellTransformer)
     */
    public Object transform(Object newValue);
}
