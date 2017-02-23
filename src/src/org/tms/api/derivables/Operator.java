package org.tms.api.derivables;

/**
 * A class implements the {@code Operator} interface to allow it to function as an operator in a calculation
 * expression assigned to a {@link org.tms.api.derivables.Derivable Derivable} object. 
 * <p>
 * Operator precedence in the TMS system is expressed using a numeric integer priority. 
 * The values used are as follows:
 * <p/>
 * <table border="1">
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
 * <p/>
 * By default, user-defined operators are assigned a priority of 8. This can be overridden, as needed.
 * <p/>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Operator extends Labeled
{
	static final int DEFAULT_PRIORITY = 8;
	
    public TokenType getTokenType();
    
    public Class<?> [] getArgTypes();

    public Token evaluate(Token... args);
    
    
    default public Class<?> getResultType()
    {
        return Object.class;
    }
    
    default public int numArgs()
    {
        Class<?> [] args = getArgTypes();
        
        return args != null ? args.length : 0;
    }
    
    default public int getPriority()
    {
        return 8;
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
