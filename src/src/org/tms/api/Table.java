package org.tms.api;

import java.util.List;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.events.EventProcessorThreadPool;

public interface Table extends TableElement
{  
    /**
     * Add a new {@link Row} to this {@link Table} directly following the current row. If the current row is last,
     * the new {@code Row} is added to the end of the table. Otherwise, the new {@code Row} is inserted directly following the current 
     * row, and all other rows are moved down by one position.
     * @return the new {@code Row}
     */
    public Row addRow();   

    /**
     * Add a new {@link Row} to this {@link Table} at the 1-based index specified by {@code idx}. If the {@code idx} value 
     * is greater than the number of rows currently in the table, additional new rows are added, and the table will contain
     * {@code idx} rows when the operation completes. Otherwise, the new {@code Row} is inserted at the position specified by
     * {@code idx} and all existing rows are moved down by one position.
     * @param idx
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
     * {@code mda} and all existing rows are moved down by one position.
     * @param mode
     * @param mda
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
     * @param idx
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
     * 
     * @return
     */
    public List<Row> getRows();
    
    /**
     * 
     * @return
     */
    public Iterable<Row> rows();
    
    /**
     * Returns the number of rows in the table.
     * @return the number of rows in the table
     */
    public int getNumRows();
    
    public Column addColumn();  
    public Column addColumn(int idx);  
    public Column addColumn(Access mode, Object... mda);  
    public Column getColumn(Access mode, Object... mda);
    public Column getColumn();
    public Column getColumn(int idx);
    public Column getColumn(String label);
    public List<Column> getColumns();
    public Iterable<Column> columns();
    public int getNumColumns();
    
    public Subset addSubset(Access mode, Object... mda);    
    public Subset getSubset(Access mode, Object... mda);
    public List<Subset> getSubsets();
    public Iterable<Subset> subsets();
       
    public Cell getCell(Row row, Column col);      
    public Cell getCell(Access mode, Object... mda);
    
    public Object getCellValue(Row row, Column col);
    public boolean setCellValue(Row row, Column col, Object o);
  
    public void pushCurrent();
    public void popCurrent();
    
    public void sort(ElementType et, TableProperty tp, TableRowColumnElement... others);
    
    /**
     * 
     * @param elements
     */
    public void delete(TableElement...elements);
    
    /**
     * Recalculate all derived elements (rows, columns and cells)
     */
    public void recalculate();
    
    public boolean isRowLabelsIndexed();
    public void setRowLabelsIndexed(boolean isIndexed);
    
    public boolean isColumnLabelsIndexed();
    public void setColumnLabelsIndexed(boolean isIndexed);
    
    public boolean isCellLabelsIndexed();
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
