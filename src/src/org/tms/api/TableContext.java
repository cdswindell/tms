package org.tms.api;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.events.EventProcessorThreadPool;

/**
 * A collection of {@link Table}, initializable {@link TableProperty}s, and optional thread pools. Tables that share the same {@code TableContext}
 * can reference one another in {@link Column}, {@link Row}, and {@link Cell} {@link org.tms.api.derivables.Derivation Derivation}s.
 * <p>
 * See {@link IndexableTableElements} and {@link InitializableTableProperties} for additional methods that affect {@code TableContext}s.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface TableContext extends BaseElement, InitializableTableProperties
{
    /**
     * Returns the {@link Table} referenced by the {@link Access} specification and associated parameters. 
     * {@code TableContext} instances maintain a directory of the {@link Table} instances created within a given
     * {@code TableContext}.
     * @param mode the Access mode specified to reference the table
     * @param mda the associated Access mode parameters
     * @return the referenced table
     * @throws org.tms.api.exceptions.InvalidAccessException If {@code Table}s cannot be referenced using {@link Access} {@code mode}.
     * @throws org.tms.api.exceptions.InvalidException If the {@link Access} additional parameters given in {@code mda} are not appropriate. 
     * See {@link Access}
     */
    public Table getTable(Access mode, Object... mda);

    /**
     * Returns the number of {@link Table} instances in this {@code TableContext}.
     * @return the number of Table instances in this TableContext
     */
    public int getNumTables();
    
    public TokenMapper getTokenMapper();
    
    /**
     * Returns {@code true} if this {@link TableContext} implements {@link DerivableThreadPool}.
     * @return true if this TableContext implements DerivableThreadPool
     */
    default public boolean isDerivableThreadPool()
    {
        return this instanceof DerivableThreadPool;
    }
    
    /**
     * Returns {@code true} if this {@link TableContext} implements {@link EventProcessorThreadPool}.
     * @return true if this TableContext implements EventProcessorThreadPool
     */
    default public boolean isEventProcessorThreadPool()
    {
        return this instanceof EventProcessorThreadPool;
    }   
}
