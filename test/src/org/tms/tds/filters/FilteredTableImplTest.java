package org.tms.tds.filters;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ContextImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;

public class FilteredTableImplTest extends BaseTest 
{

	@Test
	public void basicFilteredTableTest() 
	{
		int nRows = 100;
		
		// create the source table
		ContextImpl tc = ContextImpl.createContext();
		assertNotNull(tc);
		
		TableImpl t = TableImpl.createTable(tc);
		assertNotNull(t);
		
		// add some rows
		RowImpl r100 = t.addRow(nRows);
		assertThat(t.getNumRows(), is(nRows));
		
		// and columns
		ColumnImpl c1 = t.addColumn(Access.ByLabel,"C1");
		c1.setDerivation("rIdx");
		assertThat(t.getCellValue(r100,  c1), is((double)nRows));
		
		ColumnImpl c2 = t.addColumn(Access.ByLabel,"C2");
		assertNotNull(c2);
		
		ColumnImpl c3 = t.addColumn(Access.ByLabel,"C3");
		c3.setDerivation("rIdx * 3");
		assertThat(t.getCellValue(r100,  c3), is(3.0 * nRows));
		
		// create scope
		SubsetImpl scope = t.addSubset();
		assertNotNull(scope);
		
		scope.add(c1, c3);
		assertThat(scope.getNumColumns(), is(2));
		
		// create filtered table
		FilteredTableImpl ft = FilteredTableImpl.createTable(t, scope, tc);
		assertNotNull(ft);
		
		// check row/column count
		assertThat(ft.getNumColumns(), is(2));
		assertThat(ft.getNumRows(), is(nRows));
		
		// get cells
		RowImpl fr10 = ft.getRow(10);
		assertNotNull(fr10);
		
		FilteredColumnImpl fc1 = (FilteredColumnImpl)ft.getColumn(1);
		assertNotNull(fc1);
		assertThat(fc1.getIndex(), is(1));
		assertThat(fc1.getParent().getIndex(), is(1));
		
		Cell fr10c1 = ft.getCell(fr10,  fc1);
		assertNotNull(fr10c1);
		assertThat(fr10c1.getCellValue(), is(10.0));
		
		// try a derivation
		RowImpl frMeans = ft.addRow();
		assertNotNull(frMeans);
		assertThat(frMeans instanceof FilteredRowImpl, is(false));
		
		frMeans.setDerivation("mean(ColRef(cIdx))");	
		assertThat(ft.getCellValue(frMeans, fc1), is((1.0 + nRows)/2.0));
		
		FilteredColumnImpl fc2 = (FilteredColumnImpl)ft.getColumn("C3");
		assertNotNull(fc2);
		assertThat(fc2.getIndex(), is(2));
		assertThat(fc2.getLabel(), is("C3"));
		assertThat(fc2.getParent().getIndex(), is(3));
		assertThat(ft.getCellValue(frMeans, fc2), is((3.0 + nRows*3)/2.0));
		
		// delete c1, verify it goes away in filter
		c1.delete();
		assertThat(scope.getNumColumns(), is(1));
		assertThat(ft.getNumColumns(), is(1));
		
		// delete t, it should invalidate ft
		t.delete();
		assertThat(t.isInvalid(), is(true));
		assertThat(ft.isInvalid(), is(true));		
	}
}
