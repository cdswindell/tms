package org.tms.api;

import java.util.List;

public interface Table extends TableElement
{    
    public Row addRow();   
    public Row addRow(Access mode, Object... mda);   
    public Row getRow(Access mode, Object...mda);
    public List<Row> getRows();
    public Iterable<Row> rows();
    public int getNumRows();
    
    public Column addColumn();  
    public Column addColumn(Access mode, Object... mda);  
    public Column getColumn(Access mode, Object...mda);
    public List<Column> getColumns();
    public Iterable<Column> columns();
    public int getNumColumns();
    
    public Range addRange(Access mode, Object... mda);    
    public Range getRange(Access mode, Object... mda);
    public List<Range> getRanges();
    public Iterable<Range> ranges();
    public int getNumRanges();
    
    
    public Cell getCell(Row row, Column col);      
    public Cell getCell(Access mode, Object... mda);
    public Object getCellValue(Row row, Column col);
    public boolean setCellValue(Row row, Column col, Object o);
  
    public void pushCurrent();
    public void popCurrent();
    
    public void delete(TableElement...elements);
    
    /**
     * Recalculate all derived elements (rows, columns and cells)
     */
    public void recalculate();
}
