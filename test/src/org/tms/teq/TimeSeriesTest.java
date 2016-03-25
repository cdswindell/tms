package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XMLOptions;
import org.tms.api.utils.StockTickerOp;

public class TimeSeriesTest extends BaseTest
{
	private static final int sf_EXPECTED_ROW_COUNT = 4;
	private static final int sf_EXPECTED_CELL_COUNT = 12;
	
    @Test
    public final void testTimeSeriesedRowsDataStructures()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));

        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));

        // create the Time Series
        c3.setTimeSeries("random + rIdx");
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(true, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));

        String expr = c3.getTimeSeries().getAsEnteredExpression();
        assertThat(expr, notNullValue());
        assertThat(expr, is("random + rIdx"));  
        
        expr = c3.getTimeSeries().getInfixExpression();
        assertThat(expr, notNullValue());
        assertThat(expr, is("random + rowIndex"));  
        
        // delete column, make sure nothing blows up
        c3.delete();
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // create a new time series column that depends on another column
        // make sure it is removed when operand column is deleted
        Column c2 = tbl.getColumn(Access.ByIndex, 2);
        assertNotNull(c2);
        assertThat(2, is(c2.getIndex()));
        
        c3 = tbl.addColumn(Access.ByIndex, 3);
        assertNotNull(c3);
        assertThat(3, is(c3.getIndex()));
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // create the time series expression
        c3.setTimeSeries("random + col 2");
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(true, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // delete column 2, dependent time series should be removed
        c2.delete();
        assertThat(2, is(c3.getIndex()));
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(false));
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // same thing, only delete the time series column, then the operand column
        c2 = tbl.addColumn(Access.ByIndex, 2);
        assertNotNull(c2);
        assertThat(2, is(c2.getIndex()));
        
        c3 = tbl.getColumn(Access.ByIndex, 3);
        assertNotNull(c3);
        assertThat(3, is(c3.getIndex()));
        
        // create the time series expression
        c3.setTimeSeries("random + col 2");
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        // delete the time series column,we need this to remove listeners
        c3.delete();
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // now delete operand column
        c2.delete();
    }
    
    @Test
    public final void testSimpleTimeSeriesedRows() throws InterruptedException
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));

        // Timestamp column
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertNotNull(c1);
        assertThat(1, is(c1.getIndex()));
        c1.setLabel("Time Stamp");
        
        // Time series column
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertNotNull(c3);
        assertThat(3, is(c3.getIndex()));
        
        // set time series definition
        c3.setTimeSeries("now()");
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        String expr = c3.getTimeSeries().getInfixExpression();
        assertThat(expr, notNullValue());
        assertThat(expr, is("now()"));  
        
        // enable (start) the calculations
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(tbl.getPropertyInt(TableProperty.numRows), is (0));
        
        tbl.enableTimeSeriesedRows(c1, 250);
        assertThat(true, is(tbl.isTimeSeriesedRows()));
        assertThat(true, is(tbl.isTimeSeriesedRowsActive()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        // sleep a second, give the code time to fill in some rows
        Thread.sleep(780);
        
        // clear the time series from c3, this should shut everything down
        c3.clearTimeSeries();
        
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (8));
        assertThat(tbl.getPropertyInt(TableProperty.numRows), is (4));

        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedRowsActive()));
    }
    
    @Test
    public final void testDependentSimpleTimeSeriesedRows() throws InterruptedException
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
        
        tbl.getTableContext().registerOperator(new StockTickerOp());

        // Timestamp column
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertNotNull(c1);
        assertThat(1, is(c1.getIndex()));
        c1.setLabel("Time Stamp");
        
        // Dependent Time series column
        Column c2 = tbl.addColumn(Access.ByIndex, 2);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertNotNull(c2);
        assertThat(2, is(c2.getIndex()));
        
        // Time series column
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertNotNull(c3);
        assertThat(3, is(c3.getIndex()));
        
        // set time series definition
        c2.setTimeSeries("2 * col 3");
        assertThat(c2.isDerived(), is(false));
        assertThat(c2.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        c3.setTimeSeries("ticker(\"jcom\")");
        assertThat(c3.isDerived(), is(false));
        assertThat(c3.isTimeSeries(), is(true));
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        // enable (start) the calculations
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        assertThat(tbl.getPropertyInt(TableProperty.numRows), is (0));
        
        tbl.enableTimeSeriesedRows(c1, 250);
        assertThat(true, is(tbl.isTimeSeriesedRows()));
        assertThat(true, is(tbl.isTimeSeriesedRowsActive()));
        assertThat(false, is(tbl.isTimeSeriesedColumns()));
              
        // sleep a second, give the code time to fill in some rows
        Thread.sleep(780);
        
        // clear the time series from c3, this should shut everything down
        c2.clearTimeSeries();
        c3.clearTimeSeries();
        
        while (tbl.isPendings()) {
            Thread.sleep(500);
        }
        
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (sf_EXPECTED_CELL_COUNT));
        assertThat(tbl.getPropertyInt(TableProperty.numRows), is (sf_EXPECTED_ROW_COUNT));

        assertThat(false, is(tbl.isTimeSeriesedRows()));
        assertThat(false, is(tbl.isTimeSeriesedRowsActive()));
    }
    
    @Test
    public final void testReadTimeSeriesFromXMLs() throws InterruptedException
    {
        TableContextFactory.fetchDefaultTableContext().registerOperator(new StockTickerOp());
        
    	Table t = TableFactory.importFile(qualifiedFileName("TimeSeries.xml"), XMLOptions.Default);
    	assertNotNull(t);
    	assertThat(true, is(t.isTimeSeriesedRows()));
    	assertThat(true, is(t.isTimeSeriesedRowsActive()));
    	
    	Thread.sleep(100);
    	
    	assertThat(true, is (t.getPropertyInt(TableProperty.numRows) > sf_EXPECTED_ROW_COUNT));
    	assertThat(true, is (t.getPropertyInt(TableProperty.numCells) > sf_EXPECTED_CELL_COUNT));
    	
    	t.suspendAllTimeSeries();
    	assertThat(true, is(t.isTimeSeriesedRows()));
    	assertThat(false, is(t.isTimeSeriesedRowsActive()));
    	
    	TableContextFactory.fetchDefaultTableContext().deregisterAllOperators();
    }
}
