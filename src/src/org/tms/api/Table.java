package org.tms.api;

import java.util.List;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.events.EventProcessorThreadPool;

public interface Table extends TableElement
{    
    public Row addRow();   
    public Row addRow(Access mode, Object... mda);   
    public Row getRow(Access mode, Object... mda);
    public Row getRow();
    public List<Row> getRows();
    public Iterable<Row> rows();
    public int getNumRows();
    
    public Column addColumn();  
    public Column addColumn(Access mode, Object... mda);  
    public Column getColumn(Access mode, Object... mda);
    public Column getColumn();
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
    
    public void delete(TableElement...elements);
    
    /**
     * Recalculate all derived elements (rows, columns and cells)
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
}
