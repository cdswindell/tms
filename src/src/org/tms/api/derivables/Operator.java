package org.tms.api.derivables;

/**
 * A class implements the {@code Operator} interface to act as an operator in a calculation
 * expression assigned to a {@link org.tms.api.derivables.Derivable Derivable} object. Operators can act as
 * <i>functions</i> that take zero or more arguments and return a value, or as a unary or binary <i>operator</i>
 * that acts on one (unary) or two (binary) values and returns a value. Arguments and returned values can be
 * any valid Java data type, including other objects of your own design.
 * <p>
 * Operator precedence in the TMS system is expressed using a numeric integer priority. 
 * The values used are as follows:
 * <br>
 * <table border="1" summary="">
 * <tr><th>Operators</th><th>Priority</th><th>Examples</th></tr>
 * <tr><td>Parens</td><td>10</td><td>(, )</td></tr>
 * <tr><td>Functions</td><td>8</td><td>built-in and user-defined functions</td></tr>
 * <tr><td>Unary</td><td>7</td><td>!, %, -, neg</td></tr>
 * <tr><td>Multiplicative</td><td>6</td><td>*, /</td></tr>
 * <tr><td>Additive</td><td>5</td><td>+, -</td></tr>
 * <tr><td>Relational</td><td>4</td><td>&gt;, &lt;, &gt;=, &lt;=</td></tr>
 * <tr><td>Equality</td><td>3</td><td>=, ==, !=, &lt;&gt;</td></tr>
 * <tr><td>Logical</td><td>2</td><td>&amp;&amp;, ||</td></tr>
 * </table>
 * <br>
 * By default, user-defined operators are assigned a priority of 8. This can be overridden, as needed.
 * <br>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Operator extends Labeled
{
	static final int DEFAULT_PRIORITY = 8;
	
	/**
	 * Return a {@link TokenType} that describes this operator's behavior. Common
	 * {@link TokenType}s include:
	 * <br>
	 * <table border="1" summary="">
	 * <tr><th>TokenType</th><th>Description</th><th>Example</th></tr>
	 * <tr><td>UnaryFunc</td><td>Unary Function, taking one argument</td><td>isEven(7), abs(-30)</td></tr>
	 * <tr><td>BinaryFunc</td><td>Binary Function, taking two arguments</td><td>randBetween(10, 50)</td></tr>
	 * <tr><td>GenericFunc</td><td>Generic Function, taking one or more arguments</td><td>rectVolume(3, 4, 5)</td></tr>
	 * <tr><td>BinaryOp</td><td>Binary Operation</td><td>3 + 4, 12 * 3</td></tr>
	 * <tr><td>Operand</td><td>TokenType for return value Tokens</td><td></td></tr>
	 * </table>
	 * 
	 * @return the TokenType for this Operator
	 */
    public TokenType getTokenType();
    
    /**
     * Returns an array of the argument types required by this operator. Use this method to describe
     * the data type(s) of the arguments used by this operator.
     * @return an array of the argument types required by this operator
     */
    public Class<?> [] getArgTypes();

    /**
     * Compute the operator return value from the input arguments, in {@link Token} form.
     * 
     * @param args operator arguments, as Tokens
     * @return Token containing computed value and TokenType of Operand 
     */
    public Token evaluate(Token... args);
      
    default public Class<?> getResultType()
    {
        return Object.class;
    }
    
    /**
     * Return the number of arguments required by this operator
     * @return the number of arguments required by this operator
     */
    default public int numArgs()
    {
        Class<?> [] args = getArgTypes();
        
        return args != null ? args.length : 0;
    }
    
    default public int getPriority()
    {
        return DEFAULT_PRIORITY;
    }

    default boolean isRightAssociative() 
    {
        return false;
    }

    default boolean isVariableArgs()
    {
        return false;
    }
    
    default String [] getCategories()
    {
    	return new String[] {};
    }
}
