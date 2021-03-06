package org.tms.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;

import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.TimeSeriesable;
import org.tms.api.io.IOOption;
import org.tms.api.utils.Validatable;

/**
 * Methods common to {@link Table} {@link Row}s and {@link Column}s, 
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface TableRowColumnElement extends TableElement, Derivable, TimeSeriesable, Validatable
{
    /**
     * Return the 1-based ordinal index of this {@link Row}/{@link Column}.
     * @return The 1-based ordinal index of this Row/Column
     */
    public int getIndex();
    public String getUuid();
    public long getIdent();   
    
    public void fill(Object o, int n, Access access, Object... mda);
    public void fill(Object[] o, Access access, Object... mda);
    
    public String getUnits();
    public void setUnits(String units);    

    public String getDisplayFormat();
    public void setDisplayFormat(String format);

    /**
     * Exports this {@link TableRowColumnElement} to the specified file format using the export options given in {@code options}.
     * @param fileName the file name where the row/column is written to
     * @param options the {@link IOOption} that specifies the output file format as well as any specific export options
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
    public void export(String fileName, IOOption<?> options) 
    throws IOException;
    
    /**
     * Exports this {@link TableRowColumnElement} in the specified file format to the specified {@link OutputStream}
     * using the export options given in {@code options}.
     * @param out the {@code OutputStream} where the {@code TableRowColumnElement} data is exported to
     * @param options the {@link IOOption} that specifies the output file format as well as any specific export options
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
    public void export(OutputStream out, IOOption<?> options) 
    throws IOException;
    
    /**
     * Sort the table by a row/column. Null elements are sorted to the end of the row/column.
     * Numeric cells are given sort order priority. All other cells are sorted by the natural
     * order of the cell value interpreted as a String.
     */
    public void sort();
    
    /**
     * Sort the table by a row/column. Null elements are sorted to the end of the row/column.
     * Numeric cells are given sort order priority. All other cells are sorted by the natural
     * order of the cell value interpreted as a String.
     * 
     * @param cellSorter An instance of {@link Comparator} that is used to sort non-null row/column cells.
     */
    public void sort(Comparator<Cell> cellSorter);

    /**
     * Return the {@link Cell} identified by {@link Access} {@code mode}/{@code mda}.
     * @param mode the {@code Access} mode used to identify the {@code Cell}
     * @param mda optional information determined by {@code mode}
     * @return the identified {@code Table} {@code Cell}
     */
    public Cell getCell(Access mode, Object... mda);
    
    /**
     * Set this {@link Row} or {@link Column} as current in the parent {@link Table}.
     * @return the previous current {@code Row}/{@code Column}
     */
    public TableRowColumnElement setCurrent();
    
    public Object[] toArray();
    public <T> T[] toArray(T[] template);
}
