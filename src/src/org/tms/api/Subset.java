package org.tms.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.tms.api.io.BaseIOOptions;

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
     * Return the number of {@link Column}s in this {@code Subset}.
     * @return the number of {@code Column}s in this {@code Subset}
     */
    public int getNumColumns();
    
    /**
     * Return the number of {@link Row}s in this {@code Subset}.
     * @return the number of {@code Row}s in this {@code Subset}
     */
    public int getNumRows();
    
    /**
     * Returns the table {@link Column}s in this {@code Subset}.
     * @return the {@code Column}s in this {@code Subset}
     */    
    public List<Column> getColumns();
 
    /**
     * Returns the table {@link Row}s in this {@code Subset}.
     * @return the {@code Row}s in this {@code Subset}
     */
    public List<Row> getRows();
    
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
    
    /**
     * Exports this {@link Subset} to the specified file format using the export options given in {@code options}.
     * If no {@link Row}s are present in the subset, all {@code row}s in the underlying {@link Table} are exported.
     * If no {@link Column}s are present in the subset, all {@code columns}s in the underlying {@link Table} are exported.
     * @param fileName the file name where the subset is written to
     * @param options the {@link BaseIOOptions} that specifies the output file format as well as any specific export options
     * @throws IllegalArgumentException if {@code fileName} or {@code options} are {@code} null
     * @throws IOException if {@code fileName} cannot be created
     * @see org.tms.api.io.CSVOptions#Default
     * @see org.tms.api.io.DocOptions#Default
     * @see org.tms.api.io.HTMLOptions#Default
     * @see org.tms.api.io.PDFOptions#Default
     * @see org.tms.api.io.RTFOptions#Default
     * @see org.tms.api.io.XlsOptions#Default
     * @see org.tms.api.io.XMLOptions#Default
     */
    public void export(String fileName, BaseIOOptions<?> options) 
    throws IOException;
    
    /**
     * Exports this {@link Subset} in the specified file format to the specified {@link OutputStream}
     * using the export options given in {@code options}.
     * @param out the {@code OutputStream} where the {@code Subset} data is exported to
     * @param options the {@link BaseIOOptions} that specifies the output file format as well as any specific export options
     * @throws IllegalArgumentException if {@code out} or {@code options} are {@code} null
     * @throws IOException if {@code out} cannot be written to
     * @see org.tms.api.io.CSVOptions#Default
     * @see org.tms.api.io.DocOptions#Default
     * @see org.tms.api.io.HTMLOptions#Default
     * @see org.tms.api.io.PDFOptions#Default
     * @see org.tms.api.io.RTFOptions#Default
     * @see org.tms.api.io.XlsOptions#Default
     * @see org.tms.api.io.XMLOptions#Default
     */
    public void export(OutputStream out, BaseIOOptions<?> options) 
    throws IOException;    
}
