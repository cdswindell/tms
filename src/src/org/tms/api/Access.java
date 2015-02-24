package org.tms.api;

/**
 * This enum defines the methods available to access {@link Row}, {@link Column}, {@link Subset}, and {@link Cell} 
 * table elements from their parent containers. Some enums, such as {@code First} and {@code Last} are <em>stand-alone</em>,
 * in that no other information is required to retrieve the associated {@code TableElement} from the specified
 * parent container. Others, such as {@code ByIndex}, {@code ByLabel}, and {@code ByReference}, require one additional
 * parameter to retrieve the desired {@code TableElement}. {@code ByProperty} requires two additional parameters, a {@code String} or
 * {@link TableProperty} to specify the {@code TableProperty}, and another to specify the desired {@code TableProperty} value
 * to be used as the {@code TableElement} index.
 */
public enum Access
{
    /** The first {@code TableElement} */
    First,
    /** The first {@code TableElement} */
    Last,
    /** The {@code TableElement} following the {@code Current} element */
    Next,
    /** The {@code TableElement} preceding the {@code Current} element */
    Previous,
    /** The current {@code TableElement} */
    Current,
    /** The {@code TableElement} with the specified 1-based index*/
    ByIndex,
    ByReference,
    /** The {@code TableElement} with the specified label*/
    ByLabel, 
    /** The {@code TableElement} with the specified description*/
    ByDescription,
    ByProperty,
}
