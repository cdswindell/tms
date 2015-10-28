package org.tms.api;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.events.EventProcessorThreadPool;
import org.tms.api.exceptions.UnsupportedImplementationException;

/**
 * A collection of {@link Table}s, initializable {@link TableProperty}s, and optional thread pools. Tables that share the same {@code TableContext}
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
    
    /**
     * Returns the {@link org.tms.api.derivables.TokenMapper TokenMapper} associated with this {@code TableContext}.
     * @return the TokenMapper associated with this TableContext.
     */
    public TokenMapper getTokenMapper();
    
    /**
     * Loads the JDBC database driver class named {@code driverClassName} and maintains an index of 
     * loaded class names. 
     * <p>
     * The default implementation throws {@link org.tms.api.exceptions.UnsupportedImplementationException UnsupportedImplementationException}. 
     * @param driverClassName the JDBC driver class name to load
     * @throws ClassNotFoundException if the named JDBC driver class cannot be loaded
     * @throws UnsupportedImplementationException if this method is called on a TableContext that uses the default implementation
     */
    default public void loadDatabaseDriver(String driverClassName) throws ClassNotFoundException
    {
        throw new UnsupportedImplementationException(ElementType.TableContext, "loadDatabaseDriver");
    }
    
    /**
     * Returns true if the JDBC driver class named {@code driverClassName} has been loaded and initialized.
     * <p>
     * The default implementation throws {@link org.tms.api.exceptions.UnsupportedImplementationException UnsupportedImplementationException}. 
     * @param driverClassName String name of the JDBC driver class
     * @return true if driverClassName has been loaded
     * @throws UnsupportedImplementationException if this method is called on a TableContext that uses the default implementation
     */
    default public boolean isDatabaseDriverLoaded(String driverClassName)
    {
        throw new UnsupportedImplementationException(ElementType.TableContext, "loadDatabaseDriver");
    }
    
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

    /**
     * Registers a new {@code BinaryOp} {@link org.tms.api.derivables.Operator Operator} using the supplied
     * {@link java.util.function.BiFunction BiFunction} to compute the return value. As {@link java.util.function.BiFunction BiFunction}
     * is a {@link java.lang.FunctionalInterface FunctionalInterface}, a lambda expression can be provided
     * to perform the calculation. 
     * @param <T> the class of the function's first argument
     * @param <S> the class of the function's second argument
     * @param <R> the class of the function's return type
     * @param label the function label, which is used to reference this Operator in a Derivable expression
     * @param argTypeX the class of the first argument
     * @param argTypeY the class of the second argument
     * @param resultType the class of the return type
     * @param biOp the BiFunction used to compute the return value of this Operator
     */
    public <T, S, R> void registerOperator(String label, Class<?> argTypeX, Class<?> argTypeY, 
                Class<?> resultType, BiFunction<T, S, R> biOp);

    /**
     * Registers a new {@code UnaryOp} {@link org.tms.api.derivables.Operator Operator} using the supplied
     * {@link java.util.function.Function Function} to compute the return value. As {@link java.util.function.Function Function}
     * is a {@link java.lang.FunctionalInterface FunctionalInterface}, a lambda expression can be provided
     * to perform the calculation. 
     * @param <T> the class of the function's argument
     * @param <R> the class of the function's return type
     * @param label the function label, which is used to reference this Operator in a Derivable expression
     * @param argType the class of the function's argument
     * @param resultType the class of the return type
     * @param uniOp the Function used to compute the return value of this Operator
     */
    public <T, R> void registerOperator(String label, Class<?> argType, Class<?> resultType, Function<T, R> uniOp);

    public void registerNumericOperator(String label, UnaryOperator<Double> uniOp);

    public void registerNumericOperator(String label, BinaryOperator<Double> biOp);

    /**
     * Registers the supplied {@link org.tms.api.derivables.Operator Operator} {@code oper} 
     * with this {@code TableContext}. {@link org.tms.api.derivables.Operator Operator}s are used in
     * {@link org.tms.api.derivables.Derivable Derivable} expressions assigned to {@link TableElement}s
     * to perform calculations within the TMS Framework.
     * @param oper the labeled Operator to register with this TableContext
     */
    public void registerOperator(Operator oper);

    /**
     * Deregisters the supplied {@link org.tms.api.derivables.Operator Operator} {@code oper} 
     * from this {@code TableContext}.
     * <p>
     * <b>Note:</b>Deregistering {@link org.tms.api.derivables.Operator Operator}s only prevents them from being used
     * as a new {@link org.tms.api.derivables.Derivable Derivable} expression, it does not invalidate them in
     * existing derivations.
     * @param oper the Operator to deregister from this TableContext
     * @return true if an Operator with the supplied label was found (and deregistered)
     */
    public boolean deregisterOperator(Operator oper);

    /**
     * Deregisters the {@link org.tms.api.derivables.Operator Operator} with the supplied {@code label} 
     * from this {@code TableContext}.
     * <p>
     * <b>Note:</b>Deregistering {@link org.tms.api.derivables.Operator Operator}s only prevents them from being used
     * as a new {@link org.tms.api.derivables.Derivable Derivable} expression, it does not invalidate them in
     * existing derivations.
     * @param label the label associated with the Operator to deregister
     * @return true if an Operator with the supplied label was found (and deregistered)
     */
    public boolean deregisterOperator(String label);

    /**
     * Deregisters all user-defined {@link org.tms.api.derivables.Operator Operator}s from this {@code TableContext}.
     * <p>
     * <b>Note:</b>Deregistering {@link org.tms.api.derivables.Operator Operator}s only prevents them from being used
     * as a new {@link org.tms.api.derivables.Derivable Derivable} expression, it does not invalidate them in
     * existing derivations.
     */
    public void deregisterAllOperators();   
}
