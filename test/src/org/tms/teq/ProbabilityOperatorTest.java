package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableFactory;

public class ProbabilityOperatorTest extends BaseTest
{
    @Test
    public void testPermCombStats()
    {
        Table t = TableFactory.createTable();        
        assert (t != null);
        assertThat(t.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = t.addRow(Access.ByIndex, 1);
        Row r2 = t.addRow(Access.ByIndex, 2);
        Row r3 = t.addRow(Access.ByIndex, 3);
        Row r4 = t.addRow(Access.ByIndex, 4);

        Column c1 = t.addColumn(Access.ByIndex, 1);
        assertThat(t.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c2 = t.addColumn(Access.ByIndex, 2);
        assertThat(t.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c3 = t.addColumn(Access.ByIndex, 3);
        c3.setDerivation("perm(col 1, col 2)");
        
        Column c4 = t.addColumn(Access.ByIndex, 4);
        c4.setDerivation("comb(col 1, col 2)");

        t.setCellValue(r1, c1, 1);
        assertThat(t.getCellValue(r1,  c3), nullValue());
        assertThat(t.getCellValue(r1,  c4), nullValue());
        
        t.setCellValue(r1, c2, 1);
        assertThat(t.getCellValue(r1,  c3), is(1.0));
        assertThat(t.getCellValue(r1,  c4), is(1.0));
        
        t.setCellValue(r2, c1, 15);
        t.setCellValue(r2, c2, 4);
        assertThat(t.getCellValue(r2,  c3), is(32760.0));
        assertThat(t.getCellValue(r2,  c4), is(1365.0));
        
        t.setCellValue(r3, c1, 4);
        t.setCellValue(r3, c2, 2);
        assertThat(t.getCellValue(r3,  c3), is(12.0));
        assertThat(t.getCellValue(r3,  c4), is(6.0));
        
        t.setCellValue(r4, c1, 2);
        t.setCellValue(r4, c2, 4);
        assertThat(t.getCellValue(r4,  c3), is(ErrorCode.NaN));
        assertThat(t.getCellValue(r4,  c4), is(ErrorCode.NaN));
    }    
}
