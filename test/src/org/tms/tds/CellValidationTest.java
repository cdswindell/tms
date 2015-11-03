package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.utils.NumericRange;
import org.tms.api.utils.NumericRangeRequired;
import org.tms.api.utils.TableCellTransformer;

public class CellValidationTest
{
    @Test
    public void testCellValidation()
    {
        TableImpl t = new TableImpl();
        t.setLabel("testCellValidation");
        
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
        catch (ConstraintViolationException e)
        {
            assertThat(e, notNullValue());
        }       
        
        try {
            cR4C1.setCellValue(null);
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
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
        catch (ConstraintViolationException e)
        {
            assertThat(e, notNullValue());
        }      
        
        r1.setValidator(null);
        cR1C2.setCellValue(50);
        cR1C2.setCellValue(0);
    }

    @Test
    public void testLamdaExpressionCellValidator()
    {
        TableImpl t = new TableImpl();
        t.setLabel("testLamdaExpressionCellValidator");
        
        RowImpl r1 = t.addRow(Access.Next);
        RowImpl r2 = t.addRow(Access.Next);
        RowImpl r4 = t.addRow(Access.Next);
        ColumnImpl c1 = t.addColumn(Access.Next);
        
        r1.setValidator(new NumericRange(30.0, 40.0));
        c1.setValidator(new NumericRange(1.0, 10.0));
        
        Cell cR1C1 = t.getCell(r1, c1);
        Cell cR2C1 = t.getCell(r2, c1);
        Cell cR4C1 = t.getCell(r4, c1);
        
        // test usage of functional interface
        cR4C1.setValidator(v -> { if (v == null || !(v instanceof Number)) throw new ConstraintViolationException("Number Required"); });
        
        cR1C1.setCellValue(2.0);
        
        cR2C1.setCellValue(null);
        cR4C1.setCellValue(-5.0);
        assertThat(cR4C1.getCellValue(), is(-5.0));
        
        try {
            cR4C1.setCellValue(null);
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.ConstraintViolation));
        }       
        
        try {
            cR4C1.setCellValue("abc");
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.ConstraintViolation));
        }       
                
        cR4C1.setValidator(null);
        c1.setValidator(null);
        cR4C1.setCellValue("abc");
        
        // test usage of functional interface transformer
        cR4C1.setTransformer(v -> { if (v != null && v instanceof Number) return ((Number)v).doubleValue() * 2; return v;});
        cR4C1.setCellValue("def");
        assertThat(cR4C1.getCellValue(), is("def"));
        
        cR4C1.setCellValue(7);
        assertThat(cR4C1.getCellValue(), is(14.0));        
    }
    
    @Test
    public void testGroovyCellValidator()
    {
        TableImpl t = new TableImpl();
        t.setLabel("testGroovyCellValidator");
        
        RowImpl r1 = t.addRow(Access.Next);
        RowImpl r2 = t.addRow(Access.Next);
        RowImpl r4 = t.addRow(Access.Next);
        ColumnImpl c1 = t.addColumn(Access.Next);
        ColumnImpl c2 = t.addColumn(Access.Next);
        
        TableCellTransformer tct = 
                TableCellTransformer.fromGroovy("class valIt {void validate(Object x){assert x instanceof Number : \"number required\"\n def xd = ((Number)x).doubleValue()\n assert xd >= 30 && xd <= 40}}");
        r1.setValidator(tct);
        c1.setValidator(new NumericRange(1.0, 10.0));
        
        Cell cR1C1 = t.getCell(r1, c1);
        Cell cR2C1 = t.getCell(r2, c1);
        Cell cR4C1 = t.getCell(r4, c1);
        
        Cell cR1C2 = t.getCell(r1, c2);
        cR1C2.setCellValue(35);
        try {
            cR1C2.setCellValue("abc");
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.ConstraintViolation));
        }       
                
        // test usage of groovy
        tct = TableCellTransformer.fromGroovy("class valIt {void validate(Object x){assert x != null && x instanceof Number : \"number required\"}}");
        cR4C1.setValidator(tct);
        
        cR1C1.setCellValue(2.0);
        
        cR2C1.setCellValue(null);
        cR4C1.setCellValue(-5.0);
        assertThat(cR4C1.getCellValue(), is(-5.0));
        
        try {
            cR4C1.setCellValue(null);
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.ConstraintViolation));
        }       
        
        try {
            cR4C1.setCellValue("abc");
            fail("set cell value");
        }
        catch (ConstraintViolationException e)
        {
            assertThat(e.getTableErrorClass(), is(TableErrorClass.ConstraintViolation));
        }       
                
        cR4C1.setValidator(null);
        c1.setValidator(null);
        cR4C1.setCellValue("abc");
        
        // test usage of functional interface transformer
        cR4C1.setTransformer(v -> { if (v != null && v instanceof Number) return ((Number)v).doubleValue() * 2; return v;});
        cR4C1.setCellValue("def");
        assertThat(cR4C1.getCellValue(), is("def"));
        
        cR4C1.setCellValue(7);
        assertThat(cR4C1.getCellValue(), is(14.0));        
    }
}
