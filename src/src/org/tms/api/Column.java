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

    public Class<?> getDataType();

    public void setDataType(Class<? extends Object> dataType);
}
