package org.tms.api;

import java.util.List;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.events.EventProcessorThreadPool;

public interface Table extends TableElement
{  
    /**
     * Add a new {@link Row} to this {@link Table} directly following the current row. If the current row is last,
     * the new {@code Row} is added to the end of the table. Otherwise, the new {@code Row} is inserted directly following the current 
     * row, and all other rows are moved down by one.
     * @return the new {@code Row}
     */
    public Row addRow();   

    /**
     * Add a new {@link Row} to this {@link Table} at the 1-based index specified by {@code idx}. If the {@code idx} value 
     * is greater than the number of rows currently in the table, additional new rows are added, and the table will contain
     * {@code idx} rows when the operation completes. Otherwise, the new {@code Row} is inserted at the position specified by
     * {@code idx} and all existing rows are moved down by one.
     * @param idx the 1-based index where the new row will be inserted or added
     * @return the new {@code Row}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     */    
    public Row addRow(int idx);  
    
    /**
     * Add a new {@link Row} to this {@link Table} using the {@link Access} specified by {@code mode} and the optional argument(s)
     * specified in {@code mda}. See the Javadoc on {@link Access} to learn more about the various {@code Access} modes and
     * their required parameters (if any). 
     * <p>If the new {@code Row} is added at a position greater than the current number of rows in the table, additional new rows are added, 
     * as needed. Otherwise, the new {@code Row} is inserted at the position specified by
     * {@code mda} and all existing rows are moved down by one.
     * @param mode the {@code Access} mode to use to add the row
     * @param mda the additional parameters required by the specified {@code Access} {@code mode}
     * @return the new {@code Row}
     * @throws org.tms.api.exceptions.InvalidAccessException if an invalid {@code mode} and/or {@code mda} is specified
     */    
    public Row addRow(Access mode, Object... mda);  
    
    /**
     * Returns the current table {@link Row}, or {@code null} if no current row is defined.
     * @return the current table {@code Row}
     */
    public Row getRow();    
   
    /**
     * Returns the table {@link Row} at the 1-based index specified by {@code idx}, or {@code null} if no row 
     * exists at that location.
     * @param idx the 1-based index of the table row to retrieve
     * @return the {@code Row} with the 1-based index of {@code idx}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     */
    public Row getRow(int idx);
    
    /**
     * Returns the table {@link Row} with the label {@code label}, or {@code null} if no row 
     * has that label. If multiple rows are labeled with the same value, the first {@code Row} with the
     * specified {@code label} is returned.
     * @param label the label of the {@code Row} to retrieve
     * @return the {@code Row} with the specified label
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code label} is {@code null} or not provided.
     */
    public Row getRow(String label);
    
    /**
     * Retrieves the table {@link Row} specified by the given {@link Access} {@code mode} and its associated parameters {@code mda},
     * or {@code null} if the row does not exist. If the row does exist, it is marked as the {@link Table}'s current row. 
     * @param mode the {@code Access} mode to use to specify a table row
     * @param mda the associated {@code Access} parameters appropriate to the specified {@code mode}
     * @return the row specified by {@code Access} {@code mode} and {@code mda}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code mode} is not appropriate for row retrieval, or if the associated parameters 
     * specified in {@code mda} are not valid or are not specified.
     */
    public Row getRow(Access mode, Object... mda);
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList} of {@link Row}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table rows
     */
    public List<Row> getRows();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList} of {@link Row}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table rows
     */
    public Iterable<Row> rows();
    
    /**
     * Returns the number of rows in the table.
     * @return the number of rows in the table
     */
    public int getNumRows();
    
    /**
     * Add a new {@link Column} to this {@link Table} directly following the current column. If the current column is last,
     * the new {@code Column} is added to the end of the table. Otherwise, the new {@code Column} is inserted directly following the current 
     * column, and all other columns are moved down by one.
     * @return the new {@code Column}
     */
    public Column addColumn();  
    
    /**
     * Add a new {@link Column} to this {@link Table} at the 1-based index specified by {@code idx}. If the {@code idx} value 
     * is greater than the number of columns currently in the table, additional new columns are added, and the table will contain
     * {@code idx} columns when the operation completes. Otherwise, the new {@code Column} is inserted at the position specified by
     * {@code idx} and all existing columns are moved down by one.
     * @param idx the 1-based index where the new column will be inserted or added
     * @return the new {@code Column}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     */    
    public Column addColumn(int idx);  
    
    /**
     * Add a new {@link Column} to this {@link Table} using the {@link Access} specified by {@code mode} and the optional argument(s)
     * specified in {@code mda}. See the Javadoc on {@link Access} to learn more about the various {@code Access} modes and
     * their required parameters (if any). 
     * <p>If the new {@code Column} is added at a position greater than the current number of columns in the table, additional new columns are added, 
     * as needed. Otherwise, the new {@code Column} is inserted at the position specified by
     * {@code mda} and all existing columns are moved down by one.
     * @param mode the {@code Access} mode to use to add the column
     * @param mda the additional parameters required by the specified {@code Access} {@code mode}
     * @return the new {@code Column}
     * @throws org.tms.api.exceptions.InvalidAccessException if an invalid {@code mode} and/or {@code mda} is specified
     */    
    public Column addColumn(Access mode, Object... mda);  
    
    /**
     * Returns the current table {@link Column}, or {@code null} if no current column is defined.
     * @return the current table {@code Column}
     */
    public Column getColumn();    
   
    /**
     * Returns the table {@link Column} at the 1-based index specified by {@code idx}, or {@code null} if no column 
     * exists at that location.
     * @param idx the 1-based index of the table column to retrieve
     * @return the {@code Column} with the 1-based index of {@code idx}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     */
    public Column getColumn(int idx);
    
    /**
     * Returns the table {@link Column} with the label {@code label}, or {@code null} if no column 
     * has that label. If multiple columns are labeled with the same value, the first {@code Column} with the
     * specified {@code label} is returned.
     * @param label the label of the {@code Column} to retrieve
     * @return the {@code Column} with the specified label
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code label} is {@code null} or not provided.
     */
    public Column getColumn(String label);
    
    /**
     * Retrieves the table {@link Column} specified by the given {@link Access} {@code mode} and its associated parameters {@code mda},
     * or {@code null} if the column does not exist. If the column does exist, it is marked as the {@link Table}'s current column. 
     * @param mode the {@code Access} mode to use to specify a table column
     * @param mda the associated {@code Access} parameters appropriate to the specified {@code mode}
     * @return the column specified by {@code Access} {@code mode} and {@code mda}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code mode} is not appropriate for column retrieval, or if the associated parameters 
     * specified in {@code mda} are not valid or are not specified.
     */
    public Column getColumn(Access mode, Object... mda);

    /**
     * Returns an {@link java.util.Collections#unmodifiableList} of {@link Column}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table columns
     */
    public List<Column> getColumns();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList} of {@link Column}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table columns
     */
    public Iterable<Column> columns();
    
    /**
     * Returns the number of columns in the table.
     * @return the number of columns in the table
     */
    public int getNumColumns();
    
    /**
     * Saves this {@link Table}'s current {@link Row} and current {@link Column} to a {@code stack} where they can be recalled 
     * and reapplied, as required.
     */
    public void pushCurrent();
    
    /**
     * Retrieves a current cell reference from this {@link Table}'s current row/column {@code stack} and sets the table's
     * current {@link Row} and {@link Column}.
     */
    public void popCurrent();
    
    /**
     * Sorts this {@link Table} by a {@link TableProperty}, such as {@link TableProperty#Label} and then by the table
     * elements specified by {@code others}. If {@code et} is {@link ElementType#Row}, then the {@link TableRowColumnElement}
     * elements specified in {@code others} must all be {@link Column}s. Similarly, if {@code et} is {@link ElementType#Column}, then
     * the {@link TableRowColumnElement} must all be {@link Row}s.
     * @param et the {@link ElementType} to sort in this table ({@code Row} or {@code Column})
     * @param tp the optional {@link TableProperty} to use as the initial sort criterium
     * @param others the additional {@code Row}s or {@code Column}s to use as sort criteria
     * @throws IllegalArgumentException if {@code et} is {@code null} or if {@code tp} and {@code others} are {@code} null
     * @throws NullPointerException if an element in {@code others} is null
     * @throws org.tms.api.exceptions.InvalidParentException if an element in {@code others} isn't associated with this {@link Table}
     * @throws org.tms.api.exceptions.InvalidException if {@code et} is not {@link ElementType#Row} or {@code ElementType#Column}
     */
    public void sort(ElementType et, TableProperty tp, TableRowColumnElement... others);
    
    public Subset addSubset(Access mode, Object... mda);    
    public Subset getSubset(Access mode, Object... mda);
    public List<Subset> getSubsets();
    public Iterable<Subset> subsets();
       
    public Cell getCell(Row row, Column col);      
    public Cell getCell(Access mode, Object... mda);
    
    public Object getCellValue(Row row, Column col);
    public boolean setCellValue(Row row, Column col, Object o);
    
    /**
     * Deletes the table elements in this {@link Table} specified in {@code elements}. Deleted elements are removed
     * from any containing {@link Subset}s, and any {@link org.tms.api.derivables.Derivation}s that reference a deleted element are also removed.
     * @param elements the table elements to delete
     */
    public void delete(TableElement...elements);
    
    /**
     * Recalculate all derived elements (rows, columns and cells) in this {@link Table}.
     */
    public void recalculate();
    
    /**
     * Returns {@code true} if the {@link Row} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Row} labels in this {@code Table} are indexed
     */
    public boolean isRowLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Row} labels in this {@link Table}. Indexed rows are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique row labels
     */
    public void setRowLabelsIndexed(boolean isIndexed);
    
    /**
     * Returns {@code true} if the {@link Column} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Column} labels in this {@code Table} are indexed
     */
    public boolean isColumnLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Column} labels in this {@link Table}. Indexed columns are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique column labels
     */
    public void setColumnLabelsIndexed(boolean isIndexed);
    
    /**
     * Returns {@code true} if the {@link Cell} labels in this {@link Table} are indexed.
     * @return {@code true} if the {@code Cell} labels in this {@code Table} are indexed
     */
    public boolean isCellLabelsIndexed();
    
    /**
     * Set to {@code true} to index the {@link Cell} labels in this {@link Table}. Indexed cells are faster to retrieve,
     * which makes parsing {@link org.tms.api.derivables.Derivation}s more performant, and/but the labels must all be unique.
     * @param isIndexed {@code true} or {@code false}
     * @throws org.tms.api.exceptions.NotUniqueException if {@code isIndexed} is {@code true} and this table contains non-unique cell labels
     */
    public void setCellLabelsIndexed(boolean isIndexed);
    
    public boolean isSubsetLabelsIndexed();
    public void setSubsetLabelsIndexed(boolean isIndexed);
    
    public int getRowCapacityIncr();
    public void setRowCapacityIncr(int increment);
    
    public int getColumnCapacityIncr();
    public void setColumnCapacityIncr(int increment);
    
    public double getFreeSpaceThreshold();
    public void setFreeSpaceThreshold(double threshold);
    
    public int getPrecision();
    public void setPrecision(int digits);
    
    public boolean isAutoRecalculate();
    public void setAutoRecalculate(boolean autoRecalculate);
    
    /**
     * Returns {@code true} if this {@link Table} implements {@link DerivableThreadPool}.
     * @return true if this Table implements DerivableThreadPool
     */
    default public boolean isDerivableThreadPool()
    {
        return this instanceof DerivableThreadPool;
    }
    
    /**
     * Returns {@code true} if this {@link Table} implements {@link EventProcessorThreadPool}.
     * @return true if this Table implements EventProcessorThreadPool
     */
    default public boolean isEventProcessorThreadPool()
    {
        return this instanceof EventProcessorThreadPool;
    }   
}
