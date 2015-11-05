package org.tms.api;

import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.utils.Validatable;

/**
 * A {@link Table} cell. Cells contain
 * user data, as set by the variety of TMS {@link Table}, {@link Row}, {@link Column}, {@link Subset}, and {@code Cell} methods.
 * Cells can also be set through derivations.
 * <p>
 * Cells only exist in the context of {@link Row}s and {@link Column}s, and as such, there are no methods available to create and/or
 * fetch them directly. That said, specific cells can be assigned labels and can be added to {@link Subset}s.
 * Cells are only created when there is non-null data to assign to them. 
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Cell extends TableElement, Derivable, Validatable
{
    /**
     * Sets this cell to the specified value.
     * @param value the value to assign to this cell
     * @return true if the supplied value differs from the previous cell value
     * @throws org.tms.api.exceptions.ReadOnlyException if this cell is marked as Read Only
     * @throws org.tms.api.exceptions.NullValueException if this cell does not allow null values and value is null
     */
    public boolean setCellValue(Object value);
    
    /**
     * Return this {@code Cell}'s value, as assigned by {@link Cell#setCellValue setCellValue()}.
     * @return this cell's value
     */
    public Object getCellValue();
    
    /**
     * Returns {@code true} if this {@code Cell}'s value is numeric. 
     * @return true if this cell's value is numeric
     */
    public boolean isNumericValue();
    
    /**
     * Returns {@code true} if this {@code Cell}'s value is a {@link java.lang.String String}. 
     * @return true if this cell's value is a String
     */
    public boolean isStringValue();
        
    /**
     * Returns {@code true} if this {@code Cell}'s value is a {@link Boolean} 
     * ({@code true} or {@code false}). 
     * @return {@code true} if this cell's value is a String
     */
    public boolean isBooleanValue();
    
    /**
     * Returns {@code true} if this cell value represents an error condition.
     * @return {@code true} if this cell value represents an error condition
     */
    public boolean isErrorValue();
    
    /**
     * Return the {@link ErrorCode} associated to this {@code Cell}, if its cell value
     * represents an error condition.
     * @return the {@code ErrorCode} associated to this Cell
     */
    public ErrorCode getErrorCode();
    
    /**
     * Returns {@code true} if this {@code Cell} or the containing {@link Row}, {@link Column}, or 
     * {@link Table} is marked as Read-Only
     * @return {@code true} if this cell or the containing row, column, or table is marked as Read-Only
     */
    public boolean isWriteProtected();
    
    /**
     * Returns {@code true} if this {@code Cell} is marked as Read-Only.
     * @return {@code true} if this {@code Cell} is marked as Read-Only
     */
    public boolean isReadOnly();
    
    /**
     * Returns the {@link Class} of this {@code Cell} value.
     * @return the class of this {@code Cell} value
     */
    public Class<? extends Object> getDataType();
    
    /**
     * Returns the table {@link Row} containing this {@code Cell}.
     * @return the table {@code Row} containing this {@code Cell}
     */
    public Row getRow();
    
    /**
     * Returns the table {@link Column} containing this {@code Cell}.
     * @return the table {@code Column} containing this {@code Cell}
     */
    public Column getColumn();
    
    /**
     * Returns the Units label assigned to this {@code Cell}.
     * @return the Units label assigned to this {@code Cell}
     */
    public String getUnits();
    
    /**
     * Set the Units label to assign to this {@code Cell}. The Units label
     * is for annotation purposes only and is not used for any calculative purpose.
     * @param units the Units label to assign to this {@code Cell}
     */
    public void setUnits(String units);

    public String getFormattedCellValue();
    public String getDisplayFormat();
    public void setDisplayFormat(String format);
}
