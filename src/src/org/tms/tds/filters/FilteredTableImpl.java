package org.tms.tds.filters;

import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ContextImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;

public class FilteredTableImpl extends TableImpl 
{
	public static FilteredTableImpl createTable(TableImpl parent, SubsetImpl scope, ContextImpl tc) 
	{
		return new FilteredTableImpl(parent, scope, tc);
	}	

	private TableImpl m_parent;

	public FilteredTableImpl(TableImpl parentTable, SubsetImpl scope)
	{
		this(parentTable, scope, ContextImpl.fetchDefaultContext());
	}

	public FilteredTableImpl(TableImpl parentTable, SubsetImpl scope, ContextImpl tc)
	{
		// initialize the default table object
		super((int)Math.ceil(parentTable.getNumRows() * 1.1), (int)Math.ceil(parentTable.getNumColumns() * 1.1), tc);

		m_parent = parentTable;
		m_parent.registerFilter(this);	

		// add the rows/columns defined in the scope subset
		Iterable<Column> cols = null;
		if (scope != null && scope.getNumColumns() > 0) 
			cols = scope.getColumns();
		else
			cols = m_parent.columns();
		
		for (Column pc : cols) {
			FilteredColumnImpl c = new FilteredColumnImpl(this, (ColumnImpl)pc);
			add(c, false, false, Access.Next);                 
		}
		
		Iterable<Row> rows = null;
		if (scope != null && scope.getNumRows() > 0) 
			rows = scope.getRows();
		else
			rows = m_parent.rows();
		
		for (Row pr : rows) {
			FilteredRowImpl r = new FilteredRowImpl(this, (RowImpl)pr);
			add(r, false, false, Access.Next);                 
		}
	}

	protected TableImpl getParent()
	{
		return m_parent;
	}

	@Override
	synchronized public void delete()
	{
		if (isInvalid())
			return;
		
		if (m_parent != null)
			m_parent.deregisterFilter(this);

		super.delete();
		m_parent = null;
	}

    
	protected CellImpl getCellInternal(TableImpl table, RowImpl row, ColumnImpl col, boolean createIfNull, boolean setCurrent) 
	{
		return super.getCellInternal(table, row, col, createIfNull, setCurrent);
	}
	
	protected CellImpl getCell(RowImpl row, ColumnImpl col, boolean createIfNull)
	{
		if (row instanceof FilteredRowImpl && col instanceof FilteredColumnImpl) {
			FilteredRowImpl fRow = (FilteredRowImpl) row;
			FilteredColumnImpl fCol = (FilteredColumnImpl) col;
			CellImpl c = super.getCellInternal(getParent(), fRow.getParent(), fCol.getParent(), createIfNull, false);
			
			return new FilteredCellImpl(fRow, fCol, c);
		}
		else
			return super.getCell(row, col, createIfNull);
	}
}
