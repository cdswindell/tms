package org.tms.api;

import org.tms.api.derivables.Derivable;

/**
 * A {@link Table} row, containing a horizontal slice of cells. 
 * <p>
 * Rows are created by calling the various flavors of {@code addRow} in the {@link Table} interface.
 * See {@link TableRowColumnElement} and
 * {@link TableElement} for a list of methods that all {@code Row} instances support.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Row extends TableElement, Derivable, TableRowColumnElement
{
	Cell getCell(Column column);
}
