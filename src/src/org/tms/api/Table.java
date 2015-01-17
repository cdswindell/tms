package org.tms.api;

public interface Table extends TableElement, TableCellsElement
{
    
    public Iterable<Row> rows();
    public Iterable<Column> columns();
    
    /**
     * Recalculate all derived elements (rows, columns and cells)
     */
    public void recalculate();

    public Row addRow(Access mode, Object... mda);   
    public Column addColumn(Access mode, Object... mda);   
    
    public Column getColumn(Access mode, Object...mda);
    public Row getRow(Access mode, Object...mda);
    
    public Cell getCell(Row row, Column col);
       
    public Object getCellValue(Row row, Column col);
    public void setCellValue(Row row, Column col, Object o);

    public int getNumColumns();
    public int getNumRows();
    
    public void pushCurrent();
    public void popCurrent();
    
}
