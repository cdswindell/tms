package org.tms.api;

public interface Table extends TableCellsElement
{    
    public int getNumRows();
    public Row addRow(Access mode, Object... mda);   
    public Row getRow(Access mode, Object...mda);
    public Iterable<Row> rows();
    
    public int getNumColumns();
    public Column addColumn(Access mode, Object... mda);  
    public Column getColumn(Access mode, Object...mda);
    public Iterable<Column> columns();
    
    public int getNumRanges();
    public Range addRange(Access mode, Object... mda);    
    public Range getRange(Access mode, Object... mda);
    public Iterable<Range> ranges();
    
    public Cell getCell(Row row, Column col);      
    public Object getCellValue(Row row, Column col);
    public boolean setCellValue(Row row, Column col, Object o);
  
    public void pushCurrent();
    public void popCurrent();
    
    /**
     * Recalculate all derived elements (rows, columns and cells)
     */
    public void recalculate();
}
