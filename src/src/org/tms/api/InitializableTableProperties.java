package org.tms.api;

import java.util.List;

/**
 * This interface the methods that are common to {@link TableContext} and {@link Table} instances, each of which have several
 * {@link TableProperty}s that can be initialized automatically from a template object when creating new {@link TableContext}s and {@link Table}s.
 * When these methods are invoked on {@link TableContext}s, they get and set the values for all initializable {@link TableProperty}s, and
 * establish the initial values that new {@link Table}s will have when created with that {@link TableContext} as a parent.
 * <p>
 * When invoked on a specific {@link Table}, they get and set the values for that specific {@code Table}.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface InitializableTableProperties extends IndexableTableElements
{
    /**                                                           
     * Returns {@code true} if derived table elements (see {@link org.tms.api.derivables.Derivation}) 
     * are automatically recalculated
     * in response to changes to dependent table cells.
     * @return true if derived elements are automatically recalculated when dependent cells are modified
     */
    public boolean isAutoRecalculate();
    
    /**
     * Set to {@code true} to allow derived elements to automatically recalculate when dependent cells are modified.
     * Set to {@code false} to inhibit this behavior, which can be useful if you are about to make several
     * changes to table elements, all of which would trigger derivations to recalculate. Call the 
     * {@link Table#recalculate()} method to manually recalculate all derived elements in a table.
     * @param autoRecalculate true if derived elements should automatically recalculate when dependent cells are modified
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void setAutoRecalculate(boolean autoRecalculate);
    
    /**
     * Returns the row capacity increment for this table. When new {@link Row}s are added to a
     * table, room for the new row plus the row capacity increment are allocated at the same time
     * to make subsequent row additions more efficient. The default row capacity increment is
     * {@value org.tms.tds.ContextImpl#sf_ROW_CAPACITY_INCR_DEFAULT}.
     * @return the row capacity increment 
     */
    public int getRowCapacityIncr();
    
    /**
     * Sets the row capacity increment for this table. This value is used to minimize
     * memory fragmentation by {@link Table} implementations that use {@link List} data structures.
     * <p>
     * The default row capacity increment is
     * {@value org.tms.tds.ContextImpl#sf_ROW_CAPACITY_INCR_DEFAULT}. 
     * @param increment the new row capacity increment  
     */
    public void setRowCapacityIncr(int increment);
    
    /**
     * Returns the column capacity increment for this table. When new {@link Column}s are added to a
     * table, room for the new column plus the column capacity increment are allocated at the same time
     * to make subsequent column additions more efficient. The default column capacity increment is
     * {@value org.tms.tds.ContextImpl#sf_COLUMN_CAPACITY_INCR_DEFAULT}.
     * @return the column capacity increment 
     */
    public int getColumnCapacityIncr();
    
    /**
     * Sets the column capacity increment for this table. This value is used to minimize
     * memory fragmentation by {@link Table} implementations that use {@link List} data structures.
     * <p>
     * The default column capacity increment is
     * {@value org.tms.tds.ContextImpl#sf_COLUMN_CAPACITY_INCR_DEFAULT}. 
     * @param increment the new column capacity increment  
     */
    public void setColumnCapacityIncr(int increment);
    

}
