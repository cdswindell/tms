package org.tms.teq;

import java.util.Set;

import org.tms.api.derivables.TimeSeriesable;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.TableSliceElementImpl;

public class TimeSeriedColumnsWorker extends AbstractTimeSeriesWorker 
{
	public TimeSeriedColumnsWorker(TableImpl parentTable, RowImpl tse, Set<TimeSeriesable> colsTimeSeries) 
	{
		super(parentTable, tse, colsTimeSeries);
	}

	@Override
	protected TableSliceElementImpl addNextSlice() 
	{
		return super.m_parentTable.addColumn();
	}

	@Override
	protected CellImpl getCell(TableSliceElementImpl nextSliceRef, TableSliceElementImpl ref) 
	{
		return super.m_parentTable.getCell((RowImpl)ref, (ColumnImpl)nextSliceRef);
	}
}
