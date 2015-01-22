package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
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
import org.tms.tds.TableImpl;

public class MathOperatorTest extends BaseTest
{
    @Test
    public void testSingleVariableStats()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r10 = tbl.addRow(Access.ByIndex, 10);
        
        // extend table to 1000 rows
        tbl.addRow(Access.ByIndex, 1000);
        assertThat(tbl.getNumRows(), is(1000));
        
        Column c2 = tbl.addColumn(Access.ByIndex, 2);
        c2.fill(50);
        
        Column c3 = tbl.addColumn(Access.ByIndex, 3);
        c3.setDerivation("randomInt(col 2)");
        
        Cell cR1C3 = tbl.getCell(r10,  c3);
        assertThat(cR1C3, notNullValue());
        assertThat(cR1C3.isNumericValue(), is(true));
    }    
}
