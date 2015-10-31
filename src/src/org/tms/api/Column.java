package org.tms.api;

import org.tms.api.derivables.Derivable;

/**
 * A {@link Table} column, containing a vertical slice of cells. 
 * <p>
 * Columns are created by calling the various flavors of {@code addColumn} in the {@link Table} interface.
 * See {@link TableRowColumnElement} and
 * {@link TableElement} for a list of methods that all {@code Column} instances support.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Column extends TableElement, Derivable, TableRowColumnElement
{
    /**
     * Return the data type associated with this {@code column}. Returns {@code null} if no data type
     * has been assigned.
     * @return the data type associated with this column or none if no data type has been assigned
     */
    public Class<?> getDataType();

    /**
     * Assign a primary data type ({@link Class}) to this {@code Column}. Assigning a data type to a {@code Column}
     * allows column cells to be constrained to that data type.
     * @param dataType the Java {@code Class} to assign to this column
     */
    public void setDataType(Class<? extends Object> dataType);
}
