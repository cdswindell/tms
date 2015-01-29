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
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableContextFactory;
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
    
    @Test
    public void testForiegnCellReferences()
    {
        TableContext tc = TableContextFactory.createTableContext();
        Table t1 = TableFactory.createTable(12, 10, tc);        
        assert (t1 != null);
        t1.setLabel("t1");
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = t1.addRow(Access.ByIndex, 1);
        
        // extend table to 5000 rows
        t1.addRow(Access.ByIndex, 5000);
        assertThat(t1.getNumRows(), is(5000));
        
        Column c1 = t1.getColumn(Access.ByIndex, 1);
        assertThat(c1, nullValue());
        
        Column c2 = t1.addColumn(Access.ByIndex, 2);
        c2.fill(50);
        
        c1 = t1.getColumn(Access.ByIndex, 1);
        assertThat(c1, notNullValue());
        
        Column c3 = t1.addColumn(Access.ByIndex, 3);
        c3.setDerivation("randomInt(col 2)");
        
        Cell cR1C3 = t1.getCell(r1,  c3);
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
        cR1C3.setLabel("cR1C3");
        assertThat(t1.getCell(Access.ByLabel, "cR1C3"), notNullValue());
        
        Cell cR1C2 = t1.getCell(r1,  c2);
        assertThat(cR1C2, notNullValue());
        assertThat(cR1C2.isNumericValue(), is(true));
        cR1C2.setLabel("cR1C2");
        assertThat(t1.getCell(Access.ByLabel, "cR1C2"), notNullValue());
        assertThat(t1.getCell(Access.ByLabel, "cR1C2").getLabel(), is("cR1C2"));
        
        // create a new table and try to reference cells in t1
        Table t2 = TableFactory.createTable(12, 10, tc);        
        assert (t2 != null);

        Row t2R1 = t2.addRow();
        assertThat(t2R1, notNullValue());
        
        Column t2C1 = t2.addColumn();
        assertThat(t2C1, notNullValue());
        
        Cell cR1C1 = t2.getCell(t2R1,  t2C1);
        assertThat(cR1C1, notNullValue());
        assertThat(cR1C1.isNull(), is(true));
        
        cR1C1.setDerivation("Cell 't1::cR1C2' + t1::cR1C3");
        assertThat(cR1C1.isNull(), is(false));
        assertThat(cR1C1.isNumericValue(), is(true));
        assertThat(cR1C1.isDerived(), is(true));
        assertThat((double)cR1C1.getCellValue(), is((int)cR1C2.getCellValue() + (double)cR1C3.getCellValue()));
    }            
}
