package org.tms.api;

import org.tms.teq.Token;

public interface Table extends TableElement
{
    
    public Iterable<Row> rowIterable();
    public Iterable<Column> columnIterable();

    public Row addRow(Access mode, Object... mda);   
    public Column addColumn(Access mode, Object... mda);   
    
    public Cell getCell(Row row, Column col);
    public void setCellValue(Row row, Column col, Token t);

    public void pushCurrent();
    public void popCurrent();
    
}
