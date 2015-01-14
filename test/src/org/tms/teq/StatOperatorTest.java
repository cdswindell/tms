package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableFactory;
import org.tms.api.TableProperty;

public class StatOperatorTest
{
    @Test
    public void testSingleVariableStats()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);
        Row r3 = tbl.addRow(Access.ByIndex, 3);
        Row r4 = tbl.addRow(Access.ByIndex, 4);
        Row r5 = tbl.addRow(Access.ByIndex, 5);
        Column c1 = tbl.addColumn(Access.ByIndex, 1);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r15 = tbl.addRow(Access.ByIndex, 15);
        Column c8 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        c8.fill(42);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (tbl.getNumRows()));
        
        // mean oper
        Cell c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("mean(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // max oper
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("max(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // min oper
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("min(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(42.0));
        
        // stdev oper
        c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        c.setDerivation("StDev(col 8)");
        assertThat(c.isNumericValue(), is(true));
        assertThat(c.getCellValue(), is(0.0));
    }    

}
