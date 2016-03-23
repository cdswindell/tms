package org.tms.teq;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.tms.api.derivables.TimeSeriesable;
import org.tms.api.events.TableElementEventType;
import org.tms.tds.CellImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.TableSliceElementImpl;

public abstract class AbstractTimeSeriesWorker implements Runnable
{
	protected TableImpl m_parentTable;
	private TableSliceElementImpl m_timeSampElem;
	private Set<TimeSeriesable> m_timeSeriesedElems;
		
	abstract protected TableSliceElementImpl addNextSlice();
	abstract protected CellImpl getCell(TableSliceElementImpl nextSliceRef, TableSliceElementImpl x);
	
	protected AbstractTimeSeriesWorker(TableImpl parentTable, TableSliceElementImpl timeStampElem, Set<TimeSeriesable> tsEs)
	{
		m_parentTable = parentTable;
		m_timeSampElem = timeStampElem;
		m_timeSeriesedElems = tsEs;
	}
	
	@Override
	public void run() 
	{
		// if there are no time seriesed elements, do nothing
		if (m_timeSeriesedElems == null || m_timeSeriesedElems.isEmpty())
			return;
		
		m_parentTable.pushCurrent();
		try {
			TableSliceElementImpl nextSliceRef = addNextSlice();
			
			if (m_timeSampElem != null) {
				CellImpl tsCell = getCell(nextSliceRef, m_timeSampElem);
				if (tsCell != null)
					tsCell.setCellValue(new Date());
			}
			
			// order the derivations
			List<TimeSeriesable> ordered = DerivationImpl.calculateTimeSeriesDependencies(m_timeSeriesedElems);
			
			DerivationImpl.DerivationContext dc = new DerivationImpl.DerivationContext();
			for (TimeSeriesable ts : ordered) {
				DerivationImpl di = (DerivationImpl)ts.getTimeSeries();
				if (di != null) {
					CellImpl tsCell = getCell(nextSliceRef, (TableSliceElementImpl)ts);
					di.recalculateTargetCell(tsCell, dc);
					
					// also recalculate all dependent elements
					DerivationImpl.recalculateAffected(tsCell, dc);
				}
			}
			
            // start background calculation threads, if any
			dc.processPendings();
			
			nextSliceRef.fireEvents(nextSliceRef, TableElementEventType.OnRecalculate);
		}
		finally {
			m_parentTable.popCurrent();
		}
	}
}
