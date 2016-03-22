package org.tms.api.derivables;

import org.tms.api.TableElement;

public interface TimeSeriesable extends TableElement
{    
    /**
     * Returns {@code true} if this element has a time series set
     * @return {@code true} if this element has a time series set
     */
    public boolean isTimeSeries();
    
    /**
     * Return the element's time series formula, as a {@link TimeSeries}, if one is defined.
     * @return the element's time series
     */
    public TimeSeries getTimeSeries();
    public TimeSeries setTimeSeries(String expression);
    
    /**
     * Clear the time series assigned to this element, if one is defined.
     */
    public void clearTimeSeries();
}
