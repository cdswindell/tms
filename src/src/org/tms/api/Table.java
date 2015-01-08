package org.tms.api;

public interface Table extends TableElement
{
    public Row addRow(Access mode, Object... mda);   
    public Column addColumn(Access mode, Object... mda);   
    
}
