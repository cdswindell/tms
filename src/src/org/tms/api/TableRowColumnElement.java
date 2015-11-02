package org.tms.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;

import org.tms.api.io.BaseIOOption;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;
import org.tms.io.options.BaseIOOptions;

public interface TableRowColumnElement extends TableElement
{
    /**
     * Return the 1-based ordinal index of this Row/Column.
     * @return The 1-based ordinal index of this Row/Column
     */
    public int getIndex();
    
    public void fill(Object o, int n, Access access, Object... mda);
    public void fill(Object[] o, Access access, Object... mda);
    
    public String getUnits();
    public void setUnits(String units);    

    public String getDisplayFormat();
    public void setDisplayFormat(String format);

    public TableCellValidator getValidator();
    public void setValidator(TableCellValidator validator);
    public void setTransformer(TableCellTransformer transformer);
       
    /**
     * Exports this {@link TableRowColumnElement} to the specified file format using the export options given in {@code options}.
     * @param fileName the file name where the row/column is written to
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
     * Exports this {@link TableRowColumnElement} in the specified file format to the specified {@link OutputStream}
     * using the export options given in {@code options}.
     * @param out the {@code OutputStream} where the {@code TableRowColumnElement} data is exported to
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

    public Cell getCell(Access mode, Object... mda);
    
    public TableRowColumnElement setCurrent();
}
