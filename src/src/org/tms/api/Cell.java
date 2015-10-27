package org.tms.api;

import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;

/*
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
public interface Cell extends TableElement, Derivable
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
     * Returns {@code true} if this {@code Cell}'s value is a {@link boolean} 
     * ({@code true} or {@code false}. 
     * @return true if this cell's value is a String
     */
    public boolean isBooleanValue();
    
    public boolean isErrorValue();
    public ErrorCode getErrorCode();
    
    public boolean isWriteProtected();
    public boolean isReadOnly();
    
    public Class<? extends Object> getDataType();
    
    public Row getRow();
    public Column getColumn();
    
    public String getUnits();
    public void setUnits(String units);

    public String getDisplayFormat();
    public void setDisplayFormat(String format);
    public boolean isFormatted();
    public String getFormattedCellValue();

    public TableCellValidator getValidator();
    public void setValidator(TableCellValidator validator);
    public void setTransformer(TableCellTransformer transformer);
}
