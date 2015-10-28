/**
 * The TMS event subsystem. {@link org.tms.api.TableElement TableElement}s that implement the {@link Listenable} interface can 
 * register to be notified when {@link org.tms.api.TableElement TableElement}s are created, deleted, or modified, 
 * before or after the triggering action occurs, and when
 * {@link org.tms.api.derivables.Derivable Derivable} formulas are recalculated.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
package org.tms.api.events;

