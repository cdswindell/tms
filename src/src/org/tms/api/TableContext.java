package org.tms.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.Operator;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.api.io.IOOption;
import org.tms.tds.events.EventProcessorThreadPool;

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
	 * Clears all {@link Table}s contained in this {@code TableContext}.
	 */
	public void clear();
	
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
     * @param <U> the class of the function's second argument
     * @param <R> the class of the function's return type
     * @param label the function label, which is used to reference this Operator in a Derivable expression
     * @param argTypeX the class of the first argument
     * @param argTypeY the class of the second argument
     * @param resultType the class of the return type
     * @param biOp the BiFunction used to compute the return value of this Operator
     */
    public <T, U, R> void registerOperator(String label, Class<?> argTypeX, Class<?> argTypeY, 
                Class<?> resultType, BiFunction<T, U, R> biOp);

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

    /**
     * Simplified call to define a new {@code UnaryFunc} {@link org.tms.api.derivables.Operator Operator} 
     * using the supplied {@link java.util.function.Function Function} that operates on a {@code double} 
     * and returns a {@code double}.
     * @param label the function label, which is used to reference this Operator in a Derivable expression
     * @param uniOp UnaryOperator closure containing function logic
     */
    public void registerNumericOperator(String label, UnaryOperator<Double> uniOp);

    /**
     * Simplified call to define a new {@code BinaryFunc} {@link org.tms.api.derivables.Operator Operator} 
     * using the supplied {@link java.util.function.BinaryOperator BinaryOperator} that takes 2 {@code double} 
     * arguments and returns a {@code double}.
     * @param label the function label, which is used to reference this Operator in a Derivable expression
     * @param biOp BinaryOperator closure containing function logic
     */
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
     * Registers class methods and constructors for use as operators
     * in Derivations.
     * @param clazz The class to evaluate
     */
    public void registerOperators(Class<?> clazz);
    
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
     * Registers a new {@link org.tms.api.derivables.Operator Operator} that uses the Groovy code
     * specified in {@code fileName} to perform the calculation.
     * @param label the function label, which is used to reference this {@code Operator} in a {@code Derivable} expression
     * @param pTypes array specifying the classes of the input arguments
     * @param resultType the class of the return type
     * @param fileName fully-qualified file name containing the Groovy class containing the function logic
     */
	public void registerGroovyOperator(String label, Class<?>[] pTypes, Class<String> resultType, String fileName);
	
    /**
     * Registers a new {@link org.tms.api.derivables.Operator Operator} that uses the Groovy code
     * specified in {@code fileName} to perform the calculation.
     * @param label the function label, which is used to reference this {@code Operator} in a {@code Derivable} expression
     * @param pTypes array specifying the classes of the input arguments
     * @param resultType the class of the return type
     * @param fileName fully-qualified file name containing the Groovy class containing the function logic
     * @param methodName the method name containing the Groovy code containing the function logic
     */
    public void registerGroovyOperator(String label, Class<?>[] pTypes, Class<?> resultType, String fileName, String methodName);    

    /**
     * Loads and compiles the Groovy class contained in {@code fileName}. {@link org.tms.api.derivables.Operator Operator}s
     * are created from each {@code public} method that return a value found in the compiled class. The class itself must be concrete
     * and instantiatable.
     * 
     * @param fileName fully-qualified file name containing the Groovy class to compile and parse
     */
    public void registerGroovyOperators(String fileName);
    
    /**
     * Registers a new {@code BinaryOp} {@link org.tms.api.derivables.Operator Operator} as an overloaded operator that uses the Groovy code
     * specified in {@code fileName} to perform the calculation. Any valid {@link org.tms.api.derivables.Derivation Derivation} operator, such as 
     * {@code +}, {@code -}, {@code *}, and {@code /} can be overloaded. Overloads take into account the class types of the participating
     * operands to select the correct {@link org.tms.api.derivables.Operator Operator} to execute.
     * @param label the overloaded operator name, which is used to reference this {@code Operator} in a {@code Derivable} expression
     * @param pType1 the class of the first operand
     * @param pType2 the class of the second operand
     * @param resultType the class of the return type
     * @param fileName fully-qualified file name containing the Groovy class containing the function logic
     * @param methodName the method name containing the Groovy code containing the function logic
     */
    public void registerGroovyOverload(String label, Class<?> pType1, Class<?> pType2, Class<?> resultType, String fileName, String methodName);
    
    /**
     * Deregisters all user-defined {@link org.tms.api.derivables.Operator Operator}s from this {@code TableContext}.
     * <p>
     * <b>Note:</b>Deregistering {@link org.tms.api.derivables.Operator Operator}s only prevents them from being used
     * as a new {@link org.tms.api.derivables.Derivable Derivable} expression, it does not invalidate them in
     * existing derivations.
     */
    public void deregisterAllOperators();   
    
	public void overloadOperator(String opStr, Operator op);
	public void unOverloadOperator(String opStr, Operator op);
	
    public void export(String fileName) throws IOException;
    public void export(String fileName, IOOption<?>format) throws IOException;
    public void export(OutputStream out, IOOption<?>format) throws IOException;
    
    public Table importTable(String fileName);
    public Table importTable(String fileName, IOOption<?>format);

    public void importTables(String fileName);
    public void importTables(String fileName, IOOption<?>format);
    
    public void registerJythonOperators(String fileName);
    public void registerJythonOperators(String fileName, String className);

    public Table getConstantsTable();

    public void setConstantsTable(Table constants);

	public List<String> getOperatorCategories();

	public List<Operator> getOperatorsForCategory(String string);
}
