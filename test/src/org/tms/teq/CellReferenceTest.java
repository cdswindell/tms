package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableFactory;

public class CellReferenceTest extends BaseTest
{
    @Test
    public void testCellReferences()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 1);
        Row r2 = tbl.addRow(Access.ByIndex, 2);
        
        // extend table to 5000 rows
        tbl.addRow(Access.ByIndex, 5000);
        assertThat(tbl.getNumRows(), is(5000));
        
        Column c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, nullValue());
        
        Column c2 = tbl.addColumn(Access.ByIndex, 2);
        c2.fill(50);
        
        c1 = tbl.getColumn(Access.ByIndex, 1);
        assertThat(c1, notNullValue());
        
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        c3.setDerivation("randomInt(col 2)");
        
        Cell cR1C3 = tbl.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
        cR1C3.setLabel("cR1C3");
        assertThat(tbl.getCell(Access.ByLabel, "cR1C3"), notNullValue());
        
        Cell cR1C2 = tbl.getCell(r1,  c2);
        assertThat(cR1C2, notNullValue());
        assertThat(cR1C2.isNumericValue(), is(true));
        cR1C2.setLabel("cR1C2");
        assertThat(tbl.getCell(Access.ByLabel, "cR1C2"), notNullValue());
        assertThat(tbl.getCell(Access.ByLabel, "cR1C2").getLabel(), is("cR1C2"));
        
        Cell cR1C1 = tbl.getCell(r1,  c1);
        assertThat(cR1C1, notNullValue());
        assertThat(cR1C1.isNull(), is(true));
        
        cR1C1.setDerivation("Cell 'cR1C2' + cR1C3");
        assertThat(cR1C1.isNull(), is(false));
        assertThat(cR1C1.isNumericValue(), is(true));
        assertThat(cR1C1.isDerived(), is(true));
    }        
}
