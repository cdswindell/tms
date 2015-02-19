package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.exceptions.InvalidExpressionException;
import org.tms.api.factories.TableFactory;
import org.tms.tds.TableImpl;

public class SubsetRefTest extends BaseTest
{
    @Test
    public void testSingleVariableSubsetStats()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);
        Row r3 = tbl.addRow(Access.ByIndex, 3);
        Row r4 = tbl.addRow(Access.ByIndex, 4);
        Row r5 = tbl.addRow(Access.ByIndex, 5);
        Row r6 = tbl.addRow(Access.ByIndex, 6);
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r15 = tbl.addRow(Access.ByIndex, 15);
        assertThat(r15, notNullValue());
        
        Column c8 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c7 = tbl.getColumn(Access.ByIndex, 7);
        c7.setDerivation("col 8");
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows() * 2));
        
        c8.fill(42);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows() * 2));
        
        // create subset
        Subset rng = tbl.addSubset(Access.ByLabel, "rng1");
        rng.add(c8);
        
        // mean oper
        Cell c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(set 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // max oper
        c = tbl.getCell(r2,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("max(group 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // min oper
        c = tbl.getCell(r3,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("min(set 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // stdev oper
        c = tbl.getCell(r4,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("StDevSample(set 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(0.0));
        
        // count oper
        c = tbl.getCell(r5,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("count(set 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(0.0d + tbl.getNumRows()));
        
        // spread oper
        c = tbl.getCell(r6,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("spread(set 'rng1')");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.isErrorValue(), is(false));
        assertThat(c.getCellValue(), is(0.0));
        
        // Set less random variables
        c8.clear();
        
        // recalculate statistic, should cause cell to 
        // be set to error
        c.recalculate();
        assertThat(c.isErrorValue(), is(true));
        assertThat(c.getErrorCode(), is(ErrorCode.NaN));
        
        // set some data to known values to test stat calculation
        ((TableImpl)tbl).deactivateAutoRecalculate();
        
        tbl.setCellValue(r1, c8, 3.68);
        tbl.setCellValue(r2, c8, 1.28);
        tbl.setCellValue(r3, c8, 1.84);
        tbl.setCellValue(r4, c8, 3.68);
        tbl.setCellValue(r5, c8, 1.83);
        tbl.setCellValue(r6, c8, 6.0);
        ((TableImpl)tbl).activateAutoRecalculate();
        
        tbl.recalculate();
        
        // mean oper
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(closeTo(c.getCellValue(), 3.0517, 0.0001), is(true));
        
        // max oper
        c = tbl.getCell(r2,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(6.0));
        
        // min oper
        c = tbl.getCell(r3,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(1.28));     
        
        // stDev oper
        c = tbl.getCell(r4,  c1);
        assertThat(c, notNullValue());
        assertThat(c.isNumericValue(), is(true));
        assertThat(closeTo(c.getCellValue(), 1.7653, 0.0001), is(true));
        
        // negative test; can't transform a subset        
        try {
            c.setDerivation("normalize(set 'rng1')");
            fail("Derivation succeeded");
        }
        catch (InvalidExpressionException e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentTypeMismatch));
        }
        
        // negative test; can't transform a cell        
        try {
            c.setDerivation("normalize(col 8)");
            fail("Derivation succeeded");
        }
        catch (InvalidExpressionException e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.InvalidFunctionTarget));
        }
    }
}
