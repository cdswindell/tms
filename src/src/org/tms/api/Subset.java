package org.tms.api;

/**
 * A {@link Table} subset, containing a collection of rows, columns, cells and other subsets. 
 * <p>
 * Subsets are equivalent to cell ranges in other spreadsheets. Subsets can be used as the target of a statistical
 * calculation in a {@link org.tms.api.derivables.Derivation Derivation}, can be labeled, and are dynamic, meaning that
 * as new cells are added to elements within a {@code Subset}, they are automatically added to the containing subset as well.
 * <p>
 * Subsets are created by calling {@link Table#addSubset Table.addSubset(...)}.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Subset extends TableElement
{
    /**
     * Add {@link TableElement}s to this {@code Subset}. Returns {@code true} if this {@code Subset} did not contain some or all
     * of the {@link TableElement}s specified in {@code tableElements}, or {@code false} if {@code tableElements} is {@code null} or
     * if all of the elements are already
     * present in this {@code Subset}.
     * <p>
     * @param tableElements {@code TableElement}s to add to this {@code Subset}
     * @return true if any of the {@code tableElement}s were not contained in this {@code Subset}
     * 
     * @throws org.tms.api.exceptions.InvalidParentException if an element in {@code tableElements} isn't associated with 
     * this {@code Subset}'s {@link Table}
     * @throws org.tms.api.exceptions.IllegalTableStateException if {@code tableElements} contains this {@code Subset}
     * @throws org.tms.api.exceptions.DeletedElementException if this subset or any of the elements in {@code tableElements} have been deleted
     */
    public boolean add(TableElement... tableElements);
    
    /**
     * Removes the {@link TableElement}s specified in {@code tableELements} from this {@code Subset}.
     * @param tableElements TableElements to be removed from this Subset, if present
     * @return true if any TableElements were removed as a result of this call
     */
    public boolean remove(TableElement... tableElements);
    
    /**
     * Returns {@code true} if this {@code Subset} contains {@code tableElement}.
     * @param tableElement TableElement whose presence in this Subset is to be tested
     * @return {@code true} if this {@code Subset} contains {@code tableElement}
     */
    public boolean contains(TableElement tableElement);
}