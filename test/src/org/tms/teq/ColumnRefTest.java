package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;

public class ColumnRefTest extends BaseTest
{
    @Test
    public void rowReferenceTest()
    {
        TableContext c = TableContextFactory.fetchDefaultTableContext();
        
        Table t1 = TableFactory.createTable(10, 12, c);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        t1.setLabel("t1");
        
        t1.addRow(Access.ByIndex, 10);
        assertThat(t1.getPropertyInt(TableProperty.numRows), is (10));
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c2 = t1.addColumn(Access.ByIndex, 2);
        assertThat(c2, notNullValue());
        
        c2.setLabel("c2");        
        c2.fill(30);
        
        // create new table and reference r1 in stat calculations
        Table t2 = TableFactory.createTable(10, 12, c);
        assertThat(t2, notNullValue());
        
        Column c1 = t2.addColumn();
        assertThat(c1, notNullValue());
        
        Cell cR1C1 = t2.getCell(t2.addRow(), c1);
        assertThat(cR1C1, notNullValue());
        cR1C1.setDerivation("mean(Column t1::c2)");
        assertThat(cR1C1.getCellValue(), is(30.0));
        
        Cell cR2C1 = t2.getCell(t2.addRow(), c1);
        assertThat(cR2C1, notNullValue());
        cR2C1.setDerivation("count(Col 't1::c2')");
        assertThat(cR2C1.getCellValue(), is(10.0));
        
        Cell cR3C1 = t2.getCell(t2.addRow(), c1);
        assertThat(cR3C1, notNullValue());
        cR3C1.setDerivation("range(C \"t1::c2\")");
        assertThat(cR3C1.getCellValue(), is(0.0));
    }
}
