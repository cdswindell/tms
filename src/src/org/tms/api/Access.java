package org.tms.api;

/**
 * This enum defines the methods available to access {@link Row}, {@link Column}, {@link Subset}, and {@link Cell} 
 * table elements from their parent containers. Some enums, such as {@code First} and {@code Last} 
 * are <em>stand-alone</em>,
 * in that no other information is required to retrieve the associated {@code TableElement} from the specified
 * parent container. Others, such as {@code ByIndex}, {@code ByLabel}, and {@code ByReference}, 
 * require one additional
 * parameter to retrieve the desired {@code TableElement}. 
 * {@code ByProperty} requires two additional parameters, a {@code String} or
 * {@link TableProperty} to specify the {@code TableProperty}, and another to specify the 
 * desired {@code TableProperty} value
 * to be used as the {@code TableElement} index.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public enum Access
{
    /** The first {@link TableElement} */
    First,
    /** The first {@link TableElement} */
    Last,
    /** The {@link TableElement} following the {@code Current} element */
    Next,
    /** The {@link TableElement} preceding the {@code Current} element */
    Previous,
    /** The current {@link TableElement} */
    Current,
    /** The {@link TableElement} with the specified 1-based index*/
    ByIndex,
    /** The {@link TableElement} */
    ByReference,
    /** The {@link TableElement} with the specified label*/
    ByLabel, 
    /** The {@link TableElement} with the specified description*/
    ByDescription,
    /** The {@code TableElement} with the specified {@link TableProperty} value*/
    ByProperty,
    /** The {@code TableElement} with the specified {@link TableProperty} value*/
    ByDataType, /* Column-only*/
}
