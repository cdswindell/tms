package org.tms.teq;

import java.util.Set;

import org.tms.api.derivables.TimeSeriesable;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.TableSliceElementImpl;

public class TimeSeriedRowsWorker extends AbstractTimeSeriesWorker 
{
	public TimeSeriedRowsWorker(TableImpl parentTable, ColumnImpl tse, Set<TimeSeriesable> rowsTimeSeries) 
	{
		super(parentTable, tse, rowsTimeSeries);
	}

	@Override
	protected TableSliceElementImpl addNextSlice() 
	{
		return super.m_parentTable.addRow();
	}

	@Override
	protected CellImpl getCell(TableSliceElementImpl nextSliceRef, TableSliceElementImpl ref) 
	{
		return super.m_parentTable.getCell((RowImpl)nextSliceRef, (ColumnImpl)ref);
	}
}
