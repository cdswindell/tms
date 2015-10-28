package org.tms.api.derivables;

/**
 * A class implements the {@code Operator} interface to allow it to function as an operator in a calculation
 * expression assigned to a {@link org.tms.api.derivables.Derivable Derivable} object. 
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Operator extends Labeled
{
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
        return 5;
    }

    default boolean isRightAssociative() 
    {
        return false;
    }

    default boolean isVariableArgs()
    {
        return false;
    }
}