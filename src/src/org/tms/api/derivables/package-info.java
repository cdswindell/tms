/**
 * {@link Derivable}s are {@link org.tms.api.TableElement TableElement}s and can be assigned formulas. The formulas can include
 * user-defined {@link Operator}s as well as use the over 250 built-in operators defined within TMS. 
 * the evaluated results of the formulas are assigned to the {@link Derivable}s and are automatically recalculated
 * when other {@link org.tms.api.TableElement TableElement}s on which the formula is based are modified.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
package org.tms.api.derivables;

