package org.tms.api;

import org.tms.api.derivables.DerivableThreadPool;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.event.EventProcessorThreadPool;

public interface TableContext extends BaseElement
{
    /**
     * Returns the {@link Table} referenced by the {@link Access} specification and associated parameters. 
     * {@code TableContext} instances maintain a directory of the {@link Table} instances created within a given
     * {@code TableContext}.
     * @param mode the Access mode specified to reference the table
     * @param mda the associated Access mode parameters
     * @return the referenced table
     * @throws InvalidAccessException If {@code Table}s cannot be referenced using {@link Access} {@code mode}.
     * @throws InvalidException If the {@link Access} additional parameters given in {@code mda} are not appropriate. 
     * @See {@link Access}
     */
    public Table getTable(Access mode, Object... mda);

    public TokenMapper getTokenMapper();
    
    /**
     * Returns {@code true} if this {@link TableContext} implements {@link DerivableThreadPool}.
     * @return true if this TableContext implements DerivableThreadPool
     */
    default public boolean isDerivableThreadPool()
    {
        return this instanceof DerivableThreadPool;
    }
    
    /**
     * Returns {@code true} if this {@link TableContext} implements {@link EventProcessorThreadPool}.
     * @return true if this TableContext implements EventProcessorThreadPool
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
