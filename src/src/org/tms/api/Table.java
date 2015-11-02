package org.tms.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.io.BaseIOOption;
import org.tms.io.options.BaseIOOptions;
import org.tms.tds.events.EventProcessorThreadPool;

/**
 * A standard two dimensional table consisting of {@link Row}s, {@link Column}s, and {@link Cell}s, 
 * and, optionally, {@link Subset}s.
 * Tables are the key element in the TMS subsystem and only through table instance objects 
 * is it possible to create rows, columns, cells, and subsets.
 * <p>
 * The {@code Table} interface defines the methods available to operate on tables, including adding and 
 * retrieving {@link Row}s, {@link Column}s, {@link Cell}s, and {@link Subset}s, to get and set cell values, 
 * and to perform table-wide operations, such as sorting, recalculating derived elements, and manipulating
 * the <em>current cell</em>. It also defines methods to get and set table-wide properties such as row and 
 * column allocation increments, 
 * and to manage thread pool characteristics 
 * for the thread pools available to execute asynchronous derivations and to service event listeners. Thread
 * pool management is an optional feature that not all table implementations may support. 
 * See {@link TableContext} for more information on these capabilities.
 * <p>
 * TMS {@code Table}s are similar to Microsoft Excel spreadsheets in that both:
 * <ul>
 * <li>are cell-oriented,</li>
 * <li>have rows, columns, and multiple sheets,</li>
 * <li>allow table cells to contain data of different types (e.g., A1 is a number, B1 is a String), as well as 
 * contain the result of a calculation (known as a {@link org.tms.api.derivables.Derivation} in TMS),</li>
 * <li>automatically recalculate derivations when the data (table cells) on which they depend change, </li>
 * <li>allow rows, columns, and cell ranges to be grouped into {@link Subset}s, </li>
 * <li>support formatting,</li>
 * <li> are sortable. </li>
 * </ul>
 * <p>
 * TMS {@code Table}s differ from standard spreadsheets and provide the following advantages:
 * <ul>
 * <li>cells can contain any valid Java class instance, including those defined by the JDK (String, Integer, etc.), 
 * as well as custom classes your application provides,</li>
 * <li>derivations (formulas) can be assigned to columns and rows, as well as to specific cells; 
 * whenever cells are modified that affect the column (row) derivation, the appropriate cells 
 * in the column (row) are recalculated as well,</li>
 * <li>new functions can be registered (see {@link org.tms.api.derivables.Operator}) that can be used in 
 * {@link Column}, {@link Row},
 * and {@link Cell} {@link org.tms.api.derivables.Derivation}s to operate on standard Java data types as well as custom data types,</li>
 * <li>functions can execute asynchronously and post results to the table when complete; cells
 * dependent on asynchronous calculations are put into a <em>pending</em> state,</li>
 * <li>derivations referring to pending cells will suspend until they are able to proceed 
 * (all pending cells are resolved),</li>
 * <li>Rows and columns can be named, in addition to cells and subsets (cell ranges),</li>
 * <li>the numeric precision (number of significant digits) used to express derivation results 
 * can be specified,</li>
 * <li>custom Java {@link java.util.Comparator}s can be supplied to sort rows and columns,</li>
 * <li>cells can be constrained to specific data-types, </li>
 * <li>cell validators (and transformers) can be defined to help enforce data integrity,</li>
 * <li>event listeners are supported and can be registered on tables, rows, columns, and cells.</li>
 * </ul>
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Table extends TableElement, InitializableTableProperties
{  
    /**
     * Add a new {@link Row} to this {@link Table} directly following the current row. If the current row is last,
     * the new {@code Row} is added to the end of the table. Otherwise, the new {@code Row} is inserted directly following the current 
     * row, and all other rows are moved down by one.
     * @return the new {@code Row}
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */    
    public Row addRow(Access mode, Object... mda);  
    
    /**
     * Returns the current table {@link Row}, or {@code null} if no current row is defined.
     * @return the current table {@code Row}
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Row getRow();    
   
    /**
     * Returns the table {@link Row} at the 1-based index specified by {@code idx}, or {@code null} if no row 
     * exists at that location.
     * @param idx the 1-based index of the table row to retrieve
     * @return the {@code Row} with the 1-based index of {@code idx}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Row getRow(int idx);
    
    /**
     * Returns the table {@link Row} with the label {@code label}, or {@code null} if no row 
     * has that label. If multiple rows are labeled with the same value, the first {@code Row} with the
     * specified {@code label} is returned.
     * @param label the label of the {@code Row} to retrieve
     * @return the {@code Row} with the specified label
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code label} is {@code null} or not provided.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Row getRow(Access mode, Object... mda);
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList} of {@link Row}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table rows
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public List<Row> getRows();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList} of {@link Row}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table rows
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */    
    public Column addColumn(Access mode, Object... mda);  
    
    /**
     * Returns the current table {@link Column}, or {@code null} if no current column is defined.
     * @return the current table {@code Column}
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Column getColumn();    
   
    /**
     * Returns the table {@link Column} at the 1-based index specified by {@code idx}, or {@code null} if no column 
     * exists at that location.
     * @param idx the 1-based index of the table column to retrieve
     * @return the {@code Column} with the 1-based index of {@code idx}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code idx} is &lt;= 0
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Column getColumn(int idx);
    
    /**
     * Returns the table {@link Column} with the label {@code label}, or {@code null} if no column 
     * has that label. If multiple columns are labeled with the same value, the first {@code Column} with the
     * specified {@code label} is returned.
     * @param label the label of the {@code Column} to retrieve
     * @return the {@code Column} with the specified label
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code label} is {@code null} or not provided.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Column getColumn(Access mode, Object... mda);

    /**
     * Returns the number of columns in the table.
     * @return the number of columns in the table
     */
    public int getNumColumns();
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList} of {@link Column}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table columns
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public List<Column> getColumns();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList} of {@link Column}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table columns
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Iterable<Column> columns();
    
    /**
     * Add a new {@link Subset} to this {@link Table} using the {@link Access} specified by {@code mode} and the optional argument(s)
     * specified in {@code mda}. See the Javadoc on {@link Access} to learn more about the various {@code Access} modes and
     * their required parameters (if any). 
     * @param mode the {@code Access} mode to use to add the subset
     * @param mda the additional parameters required by the specified {@code Access} {@code mode}
     * @return the new {@code Subset}
     * @throws org.tms.api.exceptions.InvalidAccessException if an invalid {@code mode} and/or {@code mda} is specified
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */    
    public Subset addSubset(Access mode, Object... mda);    
    
    /**
     * Retrieves the table {@link Subset} specified by the given {@link Access} {@code mode} and its associated parameters {@code mda},
     * or {@code null} if the subset does not exist.  
     * @param mode the {@code Access} mode to use to specify a table subset
     * @param mda the associated {@code Access} parameters appropriate to the specified {@code mode}
     * @return the subset specified by {@code Access} {@code mode} and {@code mda}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code mode} is not appropriate for subset retrieval, or if the associated parameters 
     * specified in {@code mda} are not valid or are not specified.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Subset getSubset(Access mode, Object... mda);
    
    /**
     * Returns the {@link Table} {@link Cell} at the intersection of {@link Row} {@code row} and {@link Column} {@code col} and sets this
     * table's current row and column to these values. If {@code row} and/or
     * {@code col} are {@code null}, {@code null} is returned.
     * @param row the table row containing the desired cell
     * @param col the table column containing the desired cell
     * @return the table cell referenced by {@code row} and {@code col}, or {@code null} if row and/or col are null
     * @throws org.tms.api.exceptions.DeletedElementException if this table or the specified row or column has been deleted
     * @throws org.tms.api.exceptions.InvalidParentException if the specified row or column are not a part of this table
     */
    public Cell getCell(Row row, Column col);  
    
    /**
     * Retrieves the {@link Table} {@link Cell} specified by the given {@link Access} {@code mode} and its associated parameters {@code mda},
     * or {@code null} if the cell is not found.  
     * @param mode the {@code Access} mode to use to specify a table cell
     * @param mda the associated {@code Access} parameters appropriate to the specified {@code mode}
     * @return the cell specified by {@code Access} {@code mode} and {@code mda}
     * @throws org.tms.api.exceptions.InvalidAccessException if {@code mode} is not appropriate for table cell retrieval, or 
     * if the associated parameters specified in {@code mda} are not valid or are not specified.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public Cell getCell(Access mode, Object... mda);
    
    /**
     * Returns the value in the {@link Table} {@link Cell} at the intersection of {@link Row} {@code row} and {@link Column} {@code col} 
     * and sets this
     * table's current row and column to these values. If {@code row} and/or
     * {@code col} are {@code null}, {@code null} is returned.
     * @param row the table row containing the desired cell
     * @param col the table column containing the desired cell
     * @return the value in the table cell referenced by {@code row} and {@code col}, or {@code null} if row and/or col are null
     * @throws org.tms.api.exceptions.DeletedElementException if this table or the specified row or column has been deleted
     * @throws org.tms.api.exceptions.InvalidParentException if the specified row or column are not a part of this table
     */
    public Object getCellValue(Row row, Column col);
    
    /**
     * 
     * @param row the table row containing the desired cell
     * @param col the table column containing the desired cell
     * @return the formatted cell value
     * @throws org.tms.api.exceptions.DeletedElementException if this table or the specified row or column has been deleted
     * @throws org.tms.api.exceptions.InvalidParentException if the specified row or column are not a part of this table
     */
    public String getFormattedCellValue(Row row, Column col);
    
    /**
     * Sets the value of the {@link Table} {@link Cell} at the intersection of {@link Row} {@code row} and {@link Column} {@code col} 
     * to {@code newValue} and returns {@code true} if the cell value was changed.
     * @param row the table row containing the desired cell to set the value of
     * @param col the table column containing the desired cell to set the value of
     * @param newValue the new value to assign to the table cell at the specified row/column position
     * @return {@code true} if the cell value was not equal to {@code newValue}
     * @throws org.tms.api.exceptions.DeletedElementException if this table or the specified row or column has been deleted
     * @throws org.tms.api.exceptions.InvalidParentException if the specified row or column are not a part of this table
     */
    public boolean setCellValue(Row row, Column col, Object newValue);
    
    /**
     * Saves this {@link Table}'s current {@link Row} and current {@link Column} to a {@code stack} where they can be recalled 
     * and reapplied, as required.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void pushCurrent();
    
    /**
     * Retrieves a current cell reference from this {@link Table}'s current row/column {@code stack} and sets the table's
     * current {@link Row} and {@link Column}.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
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
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void sort(ElementType et, TableProperty tp, TableRowColumnElement... others);
    
    /**
     * Deletes the table elements in this {@link Table} specified in {@code elements}. Deleted elements are removed
     * from any containing {@link Subset}s, and any {@link org.tms.api.derivables.Derivation}s that reference a deleted element are also removed.
     * @param elements the table elements to delete
     */
    public void delete(TableElement...elements);
    
    /**
     * Recalculate all derived elements (rows, columns and cells) in this {@link Table}.
     * @throws org.tms.api.exceptions.DeletedElementException if this table has been deleted
     */
    public void recalculate();
    
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
    
    /**
     * Exports this {@link Table} in the specified file format to the specified file name
     * using the export options given in {@code options}.
     * @param fileName the file name where the table is written to
     * @param options the {@link BaseIOOptions} that specifies the output file format as well as any specific export options
     * @throws IllegalArgumentException if {@code fileName} or {@code options} are {@code} null
     * @throws IOException if {@code fileName} cannot be created
     * @see org.tms.api.io.CSVOptions#Default
     * @see org.tms.api.io.DOCOptions#Default
     * @see org.tms.api.io.HTMLOptions#Default
     * @see org.tms.api.io.PDFOptions#Default
     * @see org.tms.api.io.RTFOptions#Default
     * @see org.tms.api.io.XLSOptions#Default
     * @see org.tms.api.io.XMLOptions#Default
     */
    public void export(String fileName, BaseIOOption<?> options) 
    throws IOException;
    
    /**
     * Exports this {@link Table} in the specified file format to the specified {@link OutputStream}
     * using the export options given in {@code options}.
     * @param out the {@code OutputStream} where the {@code Table} data is exported to
     * @param options the {@link BaseIOOptions} that specifies the output file format as well as any specific export options
     * @throws IllegalArgumentException if {@code out} or {@code options} are {@code} null
     * @throws IOException if {@code out} cannot be written to
     * @see org.tms.api.io.CSVOptions#Default
     * @see org.tms.api.io.DOCOptions#Default
     * @see org.tms.api.io.HTMLOptions#Default
     * @see org.tms.api.io.PDFOptions#Default
     * @see org.tms.api.io.RTFOptions#Default
     * @see org.tms.api.io.XLSOptions#Default
     * @see org.tms.api.io.XMLOptions#Default
     */
    public void export(OutputStream out, BaseIOOption<?> options) 
    throws IOException;
    
    /**
     * Exports this {@link Table} to the specified file format using default {@link BaseIOOptions} determined by the file name extension.
     * @param fileName the file name where the table is written to
     * @throws IllegalArgumentException if {@code fileName} is {@code} null
     * @throws IOException if {@code fileName} cannot be created
     */
    default public void export(String fileName) 
    throws IOException
    {
        export(fileName, null);
    }

    /**
     * Returns {@code true} if the specified {@link Cell} has been created. In sparse tables, cells
     * are only created when they are referenced.
     * @param row the table row containing the desired cell
     * @param col the table column containing the desired cell
     * @return true if the specified cell has been created
     */
    public boolean isCellDefined(Row row, Column col);

    /**
     * Returns {@code true} if this table has been marked as persistent and will survive 
     * subsequent garbage collections
     * @return true if this table is marked as persistent
     */
    boolean isPersistant();

    /**
     * 
     * @param persistent set to true to make this table survive garbage collections
     */
    void setPersistant(boolean persistent);
}
