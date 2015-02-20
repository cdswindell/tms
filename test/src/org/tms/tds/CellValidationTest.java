package org.tms.tds;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.exceptions.TableCellValidationException;
import org.tms.api.utils.NumericRange;
import org.tms.api.utils.NumericRangeRequired;

public class CellValidationTest
{
    @Test
    public void testCellValidation()
    {
        TableImpl t = new TableImpl();
        RowImpl r1 = t.addRow(Access.Next);
        RowImpl r2 = t.addRow(Access.Next);
        RowImpl r4 = t.addRow(Access.Next);
        ColumnImpl c1 = t.addColumn(Access.Next);
        ColumnImpl c2 = t.addColumn(Access.Next);
        
        r1.setValidator(new NumericRange(30.0, 40.0));
        c1.setValidator(new NumericRange(1.0, 10.0));
        
        Cell cR1C1 = t.getCell(r1, c1);
        Cell cR2C1 = t.getCell(r2, c1);
        Cell cR4C1 = t.getCell(r4, c1);
        cR4C1.setValidator(new NumericRangeRequired(Double.MIN_VALUE, 20.0));
        
        cR1C1.setCellValue(2.0);
        cR2C1.setCellValue(null);
        cR4C1.setCellValue(-5.0);
        cR4C1.setCellValue(19.0);
        
        try {
            cR2C1.setCellValue(-5.0);
            fail("set cell value");
        }
        catch (TableCellValidationException e)
        {
            assertThat(e, notNullValue());
        }       
        
        try {
            cR4C1.setCellValue(null);
            fail("set cell value");
        }
        catch (TableCellValidationException e)
        {
            assertThat(e, notNullValue());
        }   
        
        Cell cR1C2 = t.getCell(r1, c2);
        Cell cR2C2 = t.getCell(r2, c2);
        Cell cR4C2 = t.getCell(r4, c2);
        
        cR1C2.setCellValue(35);
        cR2C2.setCellValue(200);
        cR4C2.setCellValue(null);
        
        try {
            cR1C2.setCellValue(50);
            fail("set cell value");
        }
        catch (TableCellValidationException e)
        {
            assertThat(e, notNullValue());
        }      
        
        r1.setValidator(null);
        cR1C2.setCellValue(50);
        cR1C2.setCellValue(0);
    }
}
