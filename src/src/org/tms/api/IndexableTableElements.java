package org.tms.api;

/**
 * This interface defines the methods that are common to {@link TableContext} and {@link Table} instances that allow 
 * {@link Row}, {@link Column}, {@link Cell}, and {@link Subset}, labels to be indexed.
 * When called on a {@link TableContext}, these methods establish the defaults that are used to construct new tables in that
 * {@code TableContext}. When invoked on a specific {@link Table}, they affect that table immediately.
 * <p>
 * When invoked on a specific {@link Table}, they get and set the values for that specific {@code Table}.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface IndexableTableElements
{
    /**
     * Returns {@code true} if the {@link Row} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Row} labels in this {@code Table} are indexed
     */
    public boolean isRowLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Row} labels in this {@link Table}. Indexed rows are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique row labels
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void setRowLabelsIndexed(boolean isIndexed);
    
    /**
     * Returns {@code true} if the {@link Column} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Column} labels in this {@code Table} are indexed
     */
    public boolean isColumnLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Column} labels in this {@link Table}. Indexed columns are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique column labels
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void setColumnLabelsIndexed(boolean isIndexed);
    
    /**
     * Returns {@code true} if the {@link Cell} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Cell} labels in this {@code Table} are indexed
     */
    public boolean isCellLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Cell} labels in this {@link Table}. Indexed cells are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique cell labels
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void setCellLabelsIndexed(boolean isIndexed);
    
    /**
     * Returns {@code true} if the {@link Subset} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Subset} labels in this {@code Table} are indexed
     */
    public boolean isSubsetLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Subset} labels in this {@link Table}. Indexed subsets are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique subset labels
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void setSubsetLabelsIndexed(boolean isIndexed);
}
